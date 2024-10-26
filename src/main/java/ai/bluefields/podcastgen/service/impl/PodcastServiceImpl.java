package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Podcast;
import ai.bluefields.podcastgen.repository.PodcastRepository;
import ai.bluefields.podcastgen.service.PodcastService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PodcastServiceImpl implements PodcastService {
    
    private static final Logger log = LoggerFactory.getLogger(PodcastServiceImpl.class);
    private final PodcastRepository podcastRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Podcast> getAllPodcasts() {
        log.info("Fetching all podcasts");
        try {
            List<Podcast> podcasts = podcastRepository.findAll();
            log.info("Successfully retrieved {} podcasts", podcasts.size());
            return podcasts;
        } catch (DataAccessException e) {
            log.error("Database error while fetching all podcasts: {}", e.getMessage(), e);
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
        log.info("Updating podcast with id: {}", id);
        try {
            validatePodcast(podcast);
            return podcastRepository.findById(id)
                .map(existingPodcast -> {
                    updatePodcastFields(existingPodcast, podcast);
                    Podcast updated = podcastRepository.save(existingPodcast);
                    log.info("Successfully updated podcast with id: {}", id);
                    return updated;
                })
                .orElseThrow(() -> {
                    log.warn("Failed to update - podcast not found with id: {}", id);
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
        // Add more validation as needed
    }

    private void updatePodcastFields(Podcast existing, Podcast updated) {
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setIcon(updated.getIcon());
        existing.setLength(updated.getLength());
        existing.setStatus(updated.getStatus());
        existing.setUserId(updated.getUserId());
        // Don't update createdAt
        // modifiedAt is updated via @PreUpdate
    }
}
