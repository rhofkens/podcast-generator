package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.config.AppProperties;
import ai.bluefields.podcastgen.config.PodcastGenerationWebSocketHandler;
import ai.bluefields.podcastgen.model.*;
import ai.bluefields.podcastgen.repository.PodcastRepository;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.PodcastGenerationService;
import ai.bluefields.podcastgen.util.AudioUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import ai.bluefields.podcastgen.model.Voice;
import ai.bluefields.podcastgen.service.VoiceService;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PodcastGenerationServiceImpl implements PodcastGenerationService {
    private static final Logger log = LoggerFactory.getLogger(PodcastGenerationServiceImpl.class);
    
    private final PodcastRepository podcastRepository;
    private final AIService aiService;
    private final VoiceService voiceService;
    private final PodcastGenerationWebSocketHandler webSocketHandler;
    private final AppProperties appProperties;
    private final Executor executor = Executors.newFixedThreadPool(5);

    private String formatVoiceName(String fullName) {
        String[] nameParts = fullName.trim().split("\\s+");
        if (nameParts.length > 1) {
            // Get first name and first letter of last name
            return String.format("%s %s.", 
                nameParts[0], 
                nameParts[nameParts.length - 1].substring(0, 1));
        }
        // If only one name, return it as is
        return nameParts[0];
    }

    @Override
    @Transactional
    public CompletableFuture<Void> generatePodcast(Long podcastId) {
        log.info("Starting podcast generation for podcast id: {}", podcastId);
        
        // Fetch podcast with collections eagerly before async execution
        Podcast podcast = podcastRepository.findById(podcastId)
            .orElseThrow(() -> new RuntimeException("Podcast not found"));
        
        // Force initialization of all lazy collections
        podcast.getParticipants().size();
        podcast.getAudioOutputs().size();
        
        // Set initial status without sending WebSocket update
        podcast.setGenerationStatus(PodcastGenerationStatus.QUEUED);
        podcast.setGenerationProgress(0);
        podcast.setGenerationMessage("Starting podcast generation...");
        podcastRepository.save(podcast);
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Small delay to ensure WebSocket connection is established
                Thread.sleep(1000);
                
                // Now send the QUEUED status via WebSocket
                updateGenerationStatus(podcast, PodcastGenerationStatus.QUEUED, 0, 
                    "Starting podcast generation...");

                // Generate voices for participants
                updateGenerationStatus(podcast, PodcastGenerationStatus.GENERATING_VOICES, 20, 
                    "Generating voices for participants...");
                generateVoicesForParticipants(podcast);

                // Generate audio segments
                updateGenerationStatus(podcast, PodcastGenerationStatus.GENERATING_SEGMENTS, 40, 
                    "Generating audio segments...");
                generateAudioSegments(podcast);

                // Stitch segments together
                updateGenerationStatus(podcast, PodcastGenerationStatus.STITCHING, 80, 
                    "Combining audio segments...");
                stitchAudioSegments(podcast);

                // Mark as completed
                updateGenerationStatus(podcast, PodcastGenerationStatus.COMPLETED, 100, 
                    "Podcast generation completed successfully!");

            } catch (Exception e) {
                log.error("Error generating podcast {}: {}", podcastId, e.getMessage(), e);
                updateGenerationStatus(podcastId, PodcastGenerationStatus.ERROR, 0, 
                    "Error generating podcast: " + e.getMessage());
                throw new RuntimeException("Failed to generate podcast", e);
            }
        }, executor);
    }

    @Override
    @Transactional(readOnly = true)
    public Podcast getGenerationStatus(Long podcastId) {
        return podcastRepository.findById(podcastId)
            .orElseThrow(() -> new RuntimeException("Podcast not found"));
    }

    private void updateGenerationStatus(Podcast podcast, PodcastGenerationStatus status, 
            int progress, String message) {
        podcast.setGenerationStatus(status);
        podcast.setGenerationProgress(progress);
        podcast.setGenerationMessage(message);
        podcastRepository.save(podcast);

        GenerationStatus update = new GenerationStatus(
            status.toString(), 
            progress, 
            message,
            status == PodcastGenerationStatus.COMPLETED ? podcast.getAudioUrl() : null
        );
        webSocketHandler.sendUpdate(podcast.getId().toString(), update);
    }

    private void updateGenerationStatus(Long podcastId, PodcastGenerationStatus status, 
            int progress, String message) {
        Podcast podcast = podcastRepository.findById(podcastId)
            .orElseThrow(() -> new RuntimeException("Podcast not found"));
        updateGenerationStatus(podcast, status, progress, message);
    }

    @Override
    public void cancelGeneration(Long podcastId) {
        updateGenerationStatus(podcastId, PodcastGenerationStatus.CANCELLED, 0, 
            "Generation cancelled by user");
    }

    private void generateVoicesForParticipants(Podcast podcast) {
        log.info("Processing voices for {} participants in podcast {}", 
            podcast.getParticipants().size(), podcast.getId());
        
        for (Participant participant : podcast.getParticipants()) {
            try {
                // Case 1: Voice already selected from library or previously generated
                if (participant.getSyntheticVoiceId() != null && participant.getVoicePreviewId() == null) {
                    log.debug("Participant {} already has synthetic voice {} from library", 
                        participant.getName(), participant.getSyntheticVoiceId());
                    continue;
                }

                // Case 2: Voice needs to be generated from preview
                if (participant.getVoicePreviewId() != null) {
                    log.info("Generating persistent voice from preview for participant {}", 
                        participant.getName());

                    // Create persistent voice from preview
                    JsonNode voiceResponse = aiService.createVoiceFromPreview(
                        participant.getName(), 
                        participant.getVoicePreviewId()
                    );

                    // Extract voice ID from response
                    String voiceId = voiceResponse.get("voice_id").asText();
                    
                    // Create a new Voice entity
                    Voice newVoice = new Voice();
                    newVoice.setName(formatVoiceName(participant.getName()));
                    newVoice.setExternalVoiceId(voiceId);
                    newVoice.setVoiceType(Voice.VoiceType.GENERATED);
                    newVoice.setGender(Voice.Gender.valueOf(participant.getGender().toLowerCase()));
                    newVoice.setDefault(false);
                    newVoice.setUserId(podcast.getUserId()); // Associate with the podcast creator
                    
                    // Generate AI tags for the voice
                    if (participant.getVoiceCharacteristics() != null && !participant.getVoiceCharacteristics().isEmpty()) {
                        try {
                            String[] aiGeneratedTags = aiService.generateVoiceTags(participant);
                            newVoice.setTags(aiGeneratedTags);
                            log.debug("Generated AI tags for voice {}: {}", 
                                participant.getName(), 
                                String.join(", ", aiGeneratedTags));
                        } catch (Exception e) {
                            log.warn("Failed to generate AI tags for voice {}, using basic tags: {}", 
                                participant.getName(), 
                                e.getMessage());
                            // Fallback to basic tags
                            newVoice.setTags(new String[]{
                                participant.getGender().toLowerCase(),
                                "age-" + participant.getAge(),
                                "role-" + participant.getRole().toLowerCase().replaceAll("\\s+", "-")
                            });
                        }
                    }
                    
                    // Set the audio preview path from the preview
                    newVoice.setAudioPreviewPath(participant.getVoicePreviewUrl());

                    try {
                        // Save the voice to the library
                        Voice savedVoice = voiceService.createVoice(newVoice);
                        log.info("Saved generated voice to library with ID: {}", savedVoice.getId());
                        
                        // Update participant with synthetic voice ID
                        participant.setSyntheticVoiceId(voiceId);
                        
                        log.info("Generated and saved synthetic voice {} for participant {}", 
                            voiceId, participant.getName());
                    } catch (Exception e) {
                        log.error("Failed to save voice to library: {}", e.getMessage());
                        // Continue with generation even if saving to library fails
                        participant.setSyntheticVoiceId(voiceId);
                    }
                    
                    continue;
                }

                // If we get here, we have neither a synthetic voice ID nor a preview ID
                log.error("Participant {} has no voice selection or preview", participant.getName());
                throw new RuntimeException("No voice information available for participant " + 
                    participant.getName());

            } catch (Exception e) {
                log.error("Failed to process voice for participant {}: {}", 
                    participant.getName(), e.getMessage(), e);
                throw new RuntimeException("Voice processing failed for participant " + 
                    participant.getName(), e);
            }
        }

        // Save updated participants
        podcastRepository.save(podcast);
    }

    private void generateAudioSegments(Podcast podcast) {
        log.info("Generating audio segments for podcast {}", podcast.getId());
        
        List<String> previousRequestIds = new ArrayList<>();
        ArrayList<String> segmentPaths = new ArrayList<>();  // Changed to ArrayList explicitly
        JsonNode transcript = podcast.getTranscript().getContent();
        JsonNode messages = transcript.get("messages");

        // Create directory for segments
        String segmentsDir = String.format("%s/podcasts/%d/segments", 
            appProperties.getBasePath(), 
            podcast.getId());
        try {
            Files.createDirectories(Paths.get(segmentsDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create segments directory", e);
        }

        for (int i = 0; i < messages.size(); i++) {
            JsonNode message = messages.get(i);
            Long participantId = message.get("participantId").asLong(); // Get participantId instead of speakerName
            String content = message.get("content").asText();          // Get content instead of text
            
            // Find participant for this ID
            Participant speaker = podcast.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Participant not found with ID: " + participantId));
            
            // Get previous and next text for better prosody
            String previousText = i > 0 ? messages.get(i-1).get("content").asText() : null;
            String nextText = i < messages.size()-1 ? messages.get(i+1).get("content").asText() : null;
            
            try {
                JsonNode response = aiService.generateAudioSegment(
                    content,  // Use content instead of text
                    speaker.getSyntheticVoiceId(),
                    previousRequestIds,
                    previousText,
                    nextText
                );
                
                // Store request ID for next iteration
                previousRequestIds.add(response.get("request_id").asText());
                
                // Save audio segment to file
                String segmentFileName = String.format("segment_%03d.mp3", i);
                Path segmentPath = Paths.get(segmentsDir, segmentFileName);
                byte[] audioData = Base64.getDecoder().decode(response.get("audio_data").asText());
                Files.write(segmentPath, audioData);
                
                // Store relative path
                segmentPaths.add(String.format("podcasts/%d/segments/%s", 
                    podcast.getId(), 
                    segmentFileName));
                
                // Update progress
                updateGenerationStatus(podcast, PodcastGenerationStatus.GENERATING_SEGMENTS,
                    40 + (40 * i / messages.size()),
                    String.format("Generated audio for segment %d of %d", i + 1, messages.size()));
                
            } catch (Exception e) {
                log.error("Failed to generate audio for segment {}: {}", i, e.getMessage(), e);
                throw new RuntimeException("Failed to generate audio segments", e);
            }
        }
        
        // Store the segment paths in order
        podcast.setAudioSegmentPaths(new ArrayList<>(segmentPaths));  // Ensure we're using ArrayList
        podcastRepository.save(podcast);
    }

    private void stitchAudioSegments(Podcast podcast) {
        log.info("Starting audio segment stitching for podcast {}", podcast.getId());
        
        try {
            // Create directory for final output
            String outputDir = String.format("%s/podcasts/%d/output", 
                appProperties.getBasePath(), 
                podcast.getId());
            Files.createDirectories(Paths.get(outputDir));
            
            // Generate unique filename
            String outputFileName = String.format("podcast_%d_%s.mp3", 
                podcast.getId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            Path outputPath = Paths.get(outputDir, outputFileName);
            
            // Collect all segment paths
            List<Path> segmentPaths = podcast.getAudioSegmentPaths().stream()
                .map(segmentPath -> Paths.get(appProperties.getBasePath(), segmentPath))
                .collect(Collectors.toList());
            
            // Update status
            updateGenerationStatus(podcast, PodcastGenerationStatus.STITCHING, 80,
                "Stitching audio segments...");
                
            // Perform the stitching
            byte[] stitchedAudio = AudioUtils.concatenateMP3Files(segmentPaths);
            
            // Write the final file
            Files.write(outputPath, stitchedAudio);
            
            // Create new Audio entity
            Audio audio = new Audio();
            audio.setFilename(outputFileName);
            audio.setFormat("mp3");
            audio.setPodcast(podcast);
            audio.setFilePath(String.format("podcasts/%d/output/%s", podcast.getId(), outputFileName));
            audio.setFileSize((long) stitchedAudio.length);
            audio.setCreatedAt(LocalDateTime.now());
            audio.setUpdatedAt(LocalDateTime.now());
            
            // Add quality metrics
            ObjectNode metrics = new ObjectMapper().createObjectNode();
            metrics.put("segmentCount", segmentPaths.size());
            metrics.put("totalSize", stitchedAudio.length);
            metrics.put("format", "mp3");
            audio.setQualityMetrics(metrics);
            
            // Add to podcast's audio outputs
            if (podcast.getAudioOutputs() == null) {
                podcast.setAudioOutputs(new ArrayList<>());
            }
            podcast.getAudioOutputs().add(audio);
            
            // Save podcast
            podcastRepository.save(podcast);
            
            // Final success status
            updateGenerationStatus(podcast, PodcastGenerationStatus.COMPLETED, 100,
                "Podcast generation completed successfully!");
            
        } catch (Exception e) {
            log.error("Failed to stitch audio segments for podcast {}: {}", 
                podcast.getId(), e.getMessage(), e);
            updateGenerationStatus(podcast, PodcastGenerationStatus.ERROR, 0,
                "Failed to stitch audio segments: " + e.getMessage());
            throw new RuntimeException("Failed to stitch audio segments", e);
        }
    }

    private int calculateDuration(Path audioFile) throws Exception {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile.toFile())) {
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            return (int) (frames / format.getFrameRate());
        }
    }
}
