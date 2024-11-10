package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Podcast;
import ai.bluefields.podcastgen.service.PodcastService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
@RequiredArgsConstructor
@Validated
public class PodcastController {

    private static final Logger log = LoggerFactory.getLogger(PodcastController.class);
    private final PodcastService podcastService;

    @GetMapping
    public ResponseEntity<List<Podcast>> getAllPodcasts() {
        log.info("REST request to get all podcasts");
        try {
            List<Podcast> podcasts = podcastService.getAllPodcasts();
            log.info("Successfully retrieved {} podcasts", podcasts.size());
            return ResponseEntity.ok(podcasts);
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
}
package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.dto.PodcastDTO;
import ai.bluefields.podcastgen.dto.PageResponseDTO;
import ai.bluefields.podcastgen.model.Podcast;
import ai.bluefields.podcastgen.service.PodcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/podcasts")
@RequiredArgsConstructor
public class PodcastController {
    private final PodcastService podcastService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<PodcastDTO>> getAllPodcasts(Pageable pageable) {
        Page<Podcast> podcastPage = podcastService.getAllPodcasts(pageable);
        
        PageResponseDTO<PodcastDTO> response = new PageResponseDTO<>();
        response.setContent(podcastPage.getContent().stream()
            .map(this::convertToDTO)
            .toList());
        response.setTotalPages(podcastPage.getTotalPages());
        response.setTotalElements(podcastPage.getTotalElements());
        response.setSize(podcastPage.getSize());
        response.setNumber(podcastPage.getNumber());
        
        return ResponseEntity.ok(response);
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
        return dto;
    }
}
