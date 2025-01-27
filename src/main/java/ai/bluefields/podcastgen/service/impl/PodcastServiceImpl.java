package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Context;
import ai.bluefields.podcastgen.model.Podcast;
import ai.bluefields.podcastgen.model.PodcastStatus;
import ai.bluefields.podcastgen.repository.PodcastRepository;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.PodcastService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PodcastServiceImpl implements PodcastService {
    
    private static final Logger log = LoggerFactory.getLogger(PodcastServiceImpl.class);
    private final PodcastRepository podcastRepository;
    private final AIService aiService;

    @Override
    @Transactional(readOnly = true)
    public Page<Podcast> getAllPodcasts(String userId, Pageable pageable) {
        log.info("Fetching all podcasts for user {} with pagination", userId);
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID cannot be null or empty");
            }
            Page<Podcast> podcasts = podcastRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            log.info("Successfully retrieved {} podcasts for user {}", 
                podcasts.getContent().size(), userId);
            return podcasts;
        } catch (DataAccessException e) {
            log.error("Database error while fetching podcasts for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch podcasts", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Podcast> getPodcastById(Long id) {
        log.info("Fetching podcast with id: {}", id);
        try {
            Optional<Podcast> podcast = podcastRepository.findById(id);
            if (podcast.isPresent()) {
                log.info("Found podcast with id: {}", id);
            } else {
                log.warn("No podcast found with id: {}", id);
            }
            return podcast;
        } catch (DataAccessException e) {
            log.error("Database error while fetching podcast id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch podcast", e);
        }
    }

    @Override
    public Podcast createPodcast(Podcast podcast) {
        log.info("Creating new podcast with title: {}", podcast.getTitle());
        try {
            validatePodcast(podcast);
            Podcast saved = podcastRepository.save(podcast);
            log.info("Successfully created podcast with id: {}", saved.getId());
            return saved;
        } catch (DataAccessException e) {
            log.error("Database error while creating podcast: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create podcast", e);
        } catch (IllegalArgumentException e) {
            log.error("Validation error while creating podcast: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Podcast updatePodcast(Long id, Podcast podcast) {
        log.info("Updating podcast with id: {} for user: {}", id, podcast.getUserId());
        try {
            validatePodcast(podcast);
            
            return podcastRepository.findByIdAndUserId(id, podcast.getUserId())
                .map(existingPodcast -> {
                    updatePodcastFields(existingPodcast, podcast);
                    Podcast updated = podcastRepository.save(existingPodcast);
                    log.info("Successfully updated podcast with id: {} for user: {}", 
                        id, podcast.getUserId());
                    return updated;
                })
                .orElseThrow(() -> {
                    log.warn("Failed to update - podcast not found with id: {} for user: {}", 
                        id, podcast.getUserId());
                    return new ResourceNotFoundException("Podcast", "id", id);
                });
        } catch (DataAccessException e) {
            log.error("Database error while updating podcast {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update podcast", e);
        }
    }

    @Override
    public void deletePodcast(Long id) {
        log.info("Deleting podcast with id: {}", id);
        try {
            if (!podcastRepository.existsById(id)) {
                log.warn("Failed to delete - podcast not found with id: {}", id);
                throw new ResourceNotFoundException("Podcast", "id", id);
            }
            podcastRepository.deleteById(id);
            log.info("Successfully deleted podcast with id: {}", id);
        } catch (DataAccessException e) {
            log.error("Database error while deleting podcast {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete podcast", e);
        }
    }

    private void validatePodcast(Podcast podcast) {
        if (podcast == null) {
            throw new IllegalArgumentException("Podcast cannot be null");
        }
        if (podcast.getTitle() == null || podcast.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Podcast title cannot be empty");
        }
        if (podcast.getUserId() == null || podcast.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("Podcast userId cannot be empty");
        }
        // Add any additional validation rules here
    }

    private void updatePodcastFields(Podcast existing, Podcast updated) {
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setIcon(updated.getIcon());
        existing.setLength(updated.getLength());
        existing.setStatus(updated.getStatus());
        // Don't update userId as it should remain the same
    }

    @Override
    public Podcast generateSamplePodcast(String userId) {
        log.info("Generating sample podcast for user: {}", userId);
        try {
            JsonNode suggestion = aiService.generatePodcastSuggestion();
            
            Podcast podcast = new Podcast();
            podcast.setTitle(suggestion.get("title").asText());
            podcast.setDescription(suggestion.get("description").asText());
            podcast.setLength(suggestion.get("length").asInt());
            podcast.setStatus(PodcastStatus.DRAFT);
            podcast.setUserId(userId);

            Context context = new Context();
            context.setDescriptionText(suggestion.get("contextDescription").asText());
            context.setSourceUrl(suggestion.get("sourceUrl").asText());
            context.setPodcast(podcast);
            podcast.setContext(context);

            log.info("Successfully generated sample podcast with title: {}", podcast.getTitle());
            return podcast;
        } catch (Exception e) {
            log.error("Error generating sample podcast: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate sample podcast", e);
        }
    }
}
