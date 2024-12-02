package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.config.PodcastGenerationWebSocketHandler;
import ai.bluefields.podcastgen.model.*;
import ai.bluefields.podcastgen.repository.PodcastRepository;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.PodcastGenerationService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class PodcastGenerationServiceImpl implements PodcastGenerationService {
    private static final Logger log = LoggerFactory.getLogger(PodcastGenerationServiceImpl.class);
    
    private final PodcastRepository podcastRepository;
    private final AIService aiService;
    private final PodcastGenerationWebSocketHandler webSocketHandler;
    private final Executor executor = Executors.newFixedThreadPool(5);

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
        log.info("Generating voices for {} participants in podcast {}", 
            podcast.getParticipants().size(), podcast.getId());
        
        for (Participant participant : podcast.getParticipants()) {
            try {
                // Skip if voice already generated
                if (participant.getSyntheticVoiceId() != null) {
                    log.debug("Participant {} already has synthetic voice {}", 
                        participant.getName(), participant.getSyntheticVoiceId());
                    continue;
                }

                // Check if we have a preview ID
                if (participant.getVoicePreviewId() == null) {
                    log.error("Participant {} has no voice preview ID", participant.getName());
                    throw new RuntimeException("Voice preview ID missing for participant " + 
                        participant.getName());
                }

                // Create persistent voice from preview
                JsonNode voiceResponse = aiService.createVoiceFromPreview(
                    participant.getName(), 
                    participant.getVoicePreviewId()
                );

                // Extract voice ID from response
                String voiceId = voiceResponse.get("voice_id").asText();
                
                // Update participant with synthetic voice ID
                participant.setSyntheticVoiceId(voiceId);
                
                log.info("Generated synthetic voice {} for participant {}", 
                    voiceId, participant.getName());

            } catch (Exception e) {
                log.error("Failed to generate voice for participant {}: {}", 
                    participant.getName(), e.getMessage(), e);
                throw new RuntimeException("Voice generation failed for participant " + 
                    participant.getName(), e);
            }
        }

        // Save updated participants
        podcastRepository.save(podcast);
    }

    private void generateAudioSegments(Podcast podcast) {
        log.info("Generating audio segments for podcast {}", podcast.getId());
        
        List<String> previousRequestIds = new ArrayList<>();
        List<byte[]> audioSegments = new ArrayList<>();
        JsonNode transcript = podcast.getTranscript().getContent();
        JsonNode segments = transcript.get("transcript");

        for (int i = 0; i < segments.size(); i++) {
            JsonNode segment = segments.get(i);
            String speakerName = segment.get("speakerName").asText();
            String text = segment.get("text").asText();
            
            // Find participant for this speaker
            Participant speaker = podcast.getParticipants().stream()
                .filter(p -> p.getName().equals(speakerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Speaker not found: " + speakerName));
            
            // Get previous and next text for better prosody
            String previousText = i > 0 ? segments.get(i-1).get("text").asText() : null;
            String nextText = i < segments.size()-1 ? segments.get(i+1).get("text").asText() : null;
            
            try {
                JsonNode response = aiService.generateAudioSegment(
                    text,
                    speaker.getSyntheticVoiceId(),
                    previousRequestIds,
                    previousText,
                    nextText
                );
                
                // Store request ID for next iteration
                previousRequestIds.add(response.get("request_id").asText());
                
                // Store audio data
                byte[] audioData = Base64.getDecoder().decode(response.get("audio_data").asText());
                audioSegments.add(audioData);
                
                // Update progress
                updateGenerationStatus(podcast, PodcastGenerationStatus.GENERATING_SEGMENTS,
                    40 + (40 * i / segments.size()),
                    String.format("Generated audio for segment %d of %d", i + 1, segments.size()));
                
            } catch (Exception e) {
                log.error("Failed to generate audio for segment {}: {}", i, e.getMessage(), e);
                throw new RuntimeException("Failed to generate audio segments", e);
            }
        }
        
        // Store the segments for later stitching
        podcast.setAudioSegments(audioSegments);
        podcastRepository.save(podcast);
    }

    private void stitchAudioSegments(Podcast podcast) {
        // TODO: This is a temporary implementation for testing purposes only.
        // TODO: Implement actual audio stitching to combine all segments into final audio file
        try {
            // Simulate processing time between 5-8 seconds
            long sleepTime = 5000 + (long)(Math.random() * 3000);
            Thread.sleep(sleepTime);
            log.debug("Stitched audio segments in {} ms", sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Audio stitching was interrupted", e);
        }
    }
}
