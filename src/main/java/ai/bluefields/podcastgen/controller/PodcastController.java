package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.dto.PodcastDTO;
import ai.bluefields.podcastgen.model.Participant;
import ai.bluefields.podcastgen.model.PodcastStatus;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.PodcastGenerationService;
import com.fasterxml.jackson.databind.JsonNode;
import ai.bluefields.podcastgen.dto.TranscriptGenerationRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.bluefields.podcastgen.dto.PageResponseDTO;
import ai.bluefields.podcastgen.model.Podcast;
import ai.bluefields.podcastgen.service.PodcastService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/podcasts")
@RequiredArgsConstructor
@Validated
public class PodcastController {
    private static final Logger log = LoggerFactory.getLogger(PodcastController.class);
    private final PodcastService podcastService;
    private final AIService aiService;
    private final PodcastGenerationService podcastGenerationService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<PodcastDTO>> getAllPodcasts(Pageable pageable) {
        log.info("REST request to get all podcasts with pagination");
        try {
            Page<Podcast> podcastPage = podcastService.getAllPodcasts(pageable);
            
            PageResponseDTO<PodcastDTO> response = new PageResponseDTO<>();
            response.setContent(podcastPage.getContent().stream()
                .map(this::convertToDTO)
                .toList());
            response.setTotalPages(podcastPage.getTotalPages());
            response.setTotalElements(podcastPage.getTotalElements());
            response.setSize(podcastPage.getSize());
            response.setNumber(podcastPage.getNumber());
            
            log.info("Successfully retrieved {} podcasts", podcastPage.getContent().size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving all podcasts: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Podcast> getPodcastById(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to get podcast by id: {}", id);
        try {
            return podcastService.getPodcastById(id)
                    .map(podcast -> {
                        log.info("Successfully retrieved podcast with id: {}", id);
                        return ResponseEntity.ok(podcast);
                    })
                    .orElseGet(() -> {
                        log.warn("Podcast not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error retrieving podcast with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Podcast> createPodcast(@Valid @RequestBody Podcast podcast) {
        log.info("REST request to create new podcast with title: {}", podcast.getTitle());
        try {
            Podcast result = podcastService.createPodcast(podcast);
            log.info("Successfully created podcast with id: {}", result.getId());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid podcast data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating podcast: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Podcast> updatePodcast(
            @PathVariable @Positive(message = "ID must be positive") Long id,
            @Valid @RequestBody Podcast podcast) {
        log.info("REST request to update podcast with id: {}", id);
        try {
            Podcast result = podcastService.updatePodcast(id, podcast);
            log.info("Successfully updated podcast with id: {}", id);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid podcast data for update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating podcast {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/sample")
    public ResponseEntity<Podcast> getSamplePodcast() {
        log.info("REST request to get sample podcast data");
        try {
            Podcast samplePodcast = podcastService.generateSamplePodcast();
            log.info("Successfully generated sample podcast with title: {}", samplePodcast.getTitle());
            return ResponseEntity.ok(samplePodcast);
        } catch (Exception e) {
            log.error("Error generating sample podcast: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/sample-participants")
    public ResponseEntity<JsonNode> getSampleParticipants(@PathVariable Long id) {
        log.info("REST request to get sample participants for podcast id: {}", id);
        try {
            return podcastService.getPodcastById(id)
                .map(podcast -> {
                    JsonNode participants = aiService.generateParticipantSuggestions(
                        podcast.getTitle(),
                        podcast.getDescription(),
                        podcast.getContext() != null ? podcast.getContext().getDescriptionText() : ""
                    );
                    log.info("Successfully generated sample participants for podcast id: {}", id);
                    return ResponseEntity.ok(participants);
                })
                .orElseGet(() -> {
                    log.warn("Podcast not found with id: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            log.error("Error generating sample participants: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{id}/generate-transcript")
    public ResponseEntity<JsonNode> generateTranscript(
            @PathVariable Long id,
            @RequestBody TranscriptGenerationRequest request) {
        log.info("REST request to generate transcript for podcast id: {}", id);
        try {
            return podcastService.getPodcastById(id)
                .map(podcast -> {
                    JsonNode transcript = aiService.generateTranscript(
                        podcast.getTitle(),
                        podcast.getDescription(),
                        podcast.getContext() != null ? podcast.getContext().getDescriptionText() : "",
                        request.getParticipants(),
                        podcast.getLength()
                    );
                    
                    log.info("Successfully generated transcript for podcast id: {}", id);
                    return ResponseEntity.ok(transcript);
                })
                .orElseGet(() -> {
                    log.warn("Podcast not found with id: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            log.error("Error generating transcript: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePodcast(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to delete podcast with id: {}", id);
        try {
            podcastService.deletePodcast(id);
            log.info("Successfully deleted podcast with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting podcast {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<Void> generatePodcast(@PathVariable Long id) {
        log.info("REST request to generate podcast with id: {}", id);
        try {
            podcastGenerationService.generatePodcast(id);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error starting podcast generation for {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/generation-status")
    public ResponseEntity<Podcast> getGenerationStatus(@PathVariable Long id) {
        log.info("REST request to get generation status for podcast id: {}", id);
        try {
            Podcast podcast = podcastGenerationService.getGenerationStatus(id);
            return ResponseEntity.ok(podcast);
        } catch (Exception e) {
            log.error("Error getting generation status for podcast {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/audio-status")
    public ResponseEntity<Map<String, Object>> getAudioStatus(@PathVariable Long id) {
        log.info("REST request to check audio status for podcast id: {}", id);
        try {
            return podcastService.getPodcastById(id)
                .map(podcast -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("hasAudio", podcast.getAudioUrl() != null);
                    status.put("status", podcast.getStatus());
                    status.put("generationStatus", podcast.getGenerationStatus());
                    status.put("generationProgress", podcast.getGenerationProgress());
                    return ResponseEntity.ok(status);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error checking audio status for podcast {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    private PodcastDTO convertToDTO(Podcast podcast) {
        PodcastDTO dto = new PodcastDTO();
        dto.setId(podcast.getId());
        dto.setTitle(podcast.getTitle());
        dto.setDescription(podcast.getDescription());
        dto.setLength(podcast.getLength());
        dto.setStatus(podcast.getStatus());
        dto.setCreatedAt(podcast.getCreatedAt());
        dto.setUpdatedAt(podcast.getUpdatedAt());
        dto.setUserId(podcast.getUserId());
        
        // Add new fields
        dto.setGenerationStatus(podcast.getGenerationStatus());
        dto.setGenerationProgress(podcast.getGenerationProgress());
        dto.setGenerationMessage(podcast.getGenerationMessage());
        dto.setHasAudio(podcast.getAudioUrl() != null);
        
        // Only set audioUrl if it actually exists and podcast is completed
        if (podcast.getAudioUrl() != null && podcast.getStatus() == PodcastStatus.COMPLETED) {
            dto.setAudioUrl(podcast.getAudioUrl());
        }

        return dto;
    }
}
