package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.dto.PodcastDTO;
import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import ai.bluefields.podcastgen.model.PodcastGenerationStatus;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.PodcastGenerationService;
import com.fasterxml.jackson.databind.JsonNode;
import ai.bluefields.podcastgen.dto.TranscriptGenerationRequest;

import java.util.HashMap;
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
    
    @Value("${app.features.load-sample-data:false}")
    private boolean loadSampleDataEnabled;

    @GetMapping
    public ResponseEntity<PageResponseDTO<PodcastDTO>> getAllPodcasts(
            Pageable pageable,
            @AuthenticationPrincipal OidcUser oidcUser) {
        log.info("REST request to get all podcasts with pagination for user: {}", oidcUser.getSubject());
        try {
            Page<Podcast> podcastPage = podcastService.getAllPodcasts(oidcUser.getSubject(), pageable);
            
            PageResponseDTO<PodcastDTO> response = new PageResponseDTO<>();
            response.setContent(podcastPage.getContent().stream()
                .map(this::convertToDTO)
                .toList());
            response.setTotalPages(podcastPage.getTotalPages());
            response.setTotalElements(podcastPage.getTotalElements());
            response.setSize(podcastPage.getSize());
            response.setNumber(podcastPage.getNumber());
            
            log.info("Successfully retrieved {} podcasts for user {}", podcastPage.getContent().size(), oidcUser.getSubject());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving all podcasts: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Podcast> getPodcastById(
            @PathVariable @Positive(message = "ID must be positive") Long id,
            @AuthenticationPrincipal OidcUser oidcUser) {
        log.info("REST request to get podcast by id: {} for user: {}", id, oidcUser.getSubject());
        try {
            return podcastService.getPodcastById(id)
                    .filter(podcast -> podcast.getUserId().equals(oidcUser.getSubject()))
                    .map(podcast -> {
                        log.info("Successfully retrieved podcast with id: {} for user: {}", id, oidcUser.getSubject());
                        return ResponseEntity.ok(podcast);
                    })
                    .orElseGet(() -> {
                        log.warn("Podcast not found with id: {} for user: {}", id, oidcUser.getSubject());
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error retrieving podcast with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Podcast> createPodcast(
            @Valid @RequestBody Podcast podcast,
            @AuthenticationPrincipal OidcUser oidcUser) {
        log.info("REST request to create new podcast with title: {} for user: {}", podcast.getTitle(), oidcUser.getSubject());
        try {
            podcast.setUserId(oidcUser.getSubject());
            Podcast result = podcastService.createPodcast(podcast);
            log.info("Successfully created podcast with id: {} for user: {}", result.getId(), oidcUser.getSubject());
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
            @Valid @RequestBody Podcast podcast,
            @AuthenticationPrincipal OidcUser oidcUser) {
        log.info("REST request to update podcast with id: {} for user: {}", id, oidcUser.getSubject());
        try {
            podcast.setUserId(oidcUser.getSubject());
            Podcast result = podcastService.updatePodcast(id, podcast);
            log.info("Successfully updated podcast with id: {} for user: {}", id, oidcUser.getSubject());
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
    public ResponseEntity<Podcast> getSamplePodcast(@AuthenticationPrincipal OidcUser oidcUser) {
        if (!loadSampleDataEnabled) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }

        log.info("REST request to get sample podcast data for user: {}", oidcUser.getSubject());
        try {
            Podcast samplePodcast = podcastService.generateSamplePodcast(oidcUser.getSubject());
            log.info("Successfully generated sample podcast with title: {} for user: {}", 
                    samplePodcast.getTitle(), oidcUser.getSubject());
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
            @PathVariable @Positive(message = "ID must be positive") Long id,
            @AuthenticationPrincipal OidcUser oidcUser) {
        log.info("REST request to delete podcast with id: {} for user: {}", id, oidcUser.getSubject());
        try {
            // Verify the podcast belongs to the user before deletion
            podcastService.getPodcastById(id)
                .filter(podcast -> podcast.getUserId().equals(oidcUser.getSubject()))
                .ifPresentOrElse(
                    podcast -> {
                        podcastService.deletePodcast(id);
                        log.info("Successfully deleted podcast with id: {} for user: {}", id, oidcUser.getSubject());
                    },
                    () -> {
                        log.warn("Podcast not found or unauthorized for deletion - id: {} user: {}", id, oidcUser.getSubject());
                        throw new ResourceNotFoundException("Podcast", "id", id);
                    }
                );
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

    @GetMapping("/validate-audio/{id}")
    public ResponseEntity<Map<String, Object>> validateAudioUrl(@PathVariable Long id) {
        log.debug("Validating audio URL for podcast {}", id);
        try {
            return podcastService.getPodcastById(id)
                .map(podcast -> {
                    String audioUrl = podcast.getAudioUrl();
                    boolean isValid = audioUrl != null && 
                                    !audioUrl.isEmpty() && 
                                    podcast.getGenerationStatus() == PodcastGenerationStatus.COMPLETED &&
                                    podcast.getAudioOutputs() != null &&
                                    !podcast.getAudioOutputs().isEmpty();
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", isValid);
                    response.put("url", isValid ? audioUrl : null);
                    
                    log.debug("Audio URL validation result for podcast {}: {}", id, isValid);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("Podcast not found during audio validation: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            log.error("Error checking audio status for podcast {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    private PodcastDTO convertToDTO(Podcast podcast) {
        log.debug("Converting podcast to DTO - id: {}, status: {}", podcast.getId(), podcast.getStatus());
        
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
        
        // Add debug logging for audio status
        String audioUrl = podcast.getAudioUrl();
        log.debug("Podcast {} - Raw audioUrl: {}", podcast.getId(), audioUrl);
        
        boolean hasAudio = audioUrl != null && 
                          !audioUrl.isEmpty() && 
                          podcast.getGenerationStatus() == PodcastGenerationStatus.COMPLETED;
        
        log.debug("Podcast {} - hasAudio: {}, generationStatus: {}, audioUrl: {}", 
            podcast.getId(), 
            hasAudio, 
            podcast.getGenerationStatus(),
            audioUrl
        );
        
        dto.setHasAudio(hasAudio);
        dto.setAudioUrl(hasAudio ? audioUrl : null);

        return dto;
    }
}
