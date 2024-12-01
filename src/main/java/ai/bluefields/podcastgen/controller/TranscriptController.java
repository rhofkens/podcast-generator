package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Transcript;
import ai.bluefields.podcastgen.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ai.bluefields.podcastgen.dto.TranscriptCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/transcripts")
@RequiredArgsConstructor
@Validated
public class TranscriptController {

    private static final Logger log = LoggerFactory.getLogger(TranscriptController.class);
    private final TranscriptService transcriptService;

    @GetMapping
    public ResponseEntity<List<Transcript>> getAllTranscripts() {
        log.info("REST request to get all transcripts");
        try {
            List<Transcript> transcripts = transcriptService.getAllTranscripts();
            log.info("Successfully retrieved {} transcripts", transcripts.size());
            return ResponseEntity.ok(transcripts);
        } catch (Exception e) {
            log.error("Error retrieving all transcripts: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transcript> getTranscriptById(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to get transcript by id: {}", id);
        try {
            return transcriptService.getTranscriptById(id)
                    .map(transcript -> {
                        log.info("Successfully retrieved transcript with id: {}", id);
                        return ResponseEntity.ok(transcript);
                    })
                    .orElseGet(() -> {
                        log.warn("Transcript not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error retrieving transcript with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/podcast/{podcastId}")
    public ResponseEntity<List<Transcript>> getTranscriptsByPodcastId(
            @PathVariable @Positive(message = "Podcast ID must be positive") Long podcastId) {
        log.info("REST request to get transcripts for podcast id: {}", podcastId);
        try {
            List<Transcript> transcripts = transcriptService.getTranscriptsByPodcastId(podcastId);
            log.info("Successfully retrieved {} transcripts for podcast id: {}", transcripts.size(), podcastId);
            return ResponseEntity.ok(transcripts);
        } catch (Exception e) {
            log.error("Error retrieving transcripts for podcast {}: {}", podcastId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Transcript> createTranscript(@Valid @RequestBody TranscriptCreateRequest request) {
        log.info("REST request to create new transcript");
        try {
            Transcript transcript = new Transcript();
            transcript.setPodcast(request.getPodcast());
            transcript.setContent(request.getContent());
            
            Transcript result = transcriptService.createTranscript(transcript);
            log.info("Successfully created transcript with id: {}", result.getId());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid transcript data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating transcript: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transcript> updateTranscript(
            @PathVariable @Positive(message = "ID must be positive") Long id,
            @Valid @RequestBody Transcript transcript) {
        log.info("REST request to update transcript with id: {}", id);
        try {
            Transcript result = transcriptService.updateTranscript(id, transcript);
            log.info("Successfully updated transcript with id: {}", id);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid transcript data for update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating transcript {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTranscript(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to delete transcript with id: {}", id);
        try {
            transcriptService.deleteTranscript(id);
            log.info("Successfully deleted transcript with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting transcript {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
