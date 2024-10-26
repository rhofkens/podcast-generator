package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Audio;
import ai.bluefields.podcastgen.service.AudioService;
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
@RequestMapping("/api/audios")
@RequiredArgsConstructor
@Validated
public class AudioController {

    private static final Logger log = LoggerFactory.getLogger(AudioController.class);
    private final AudioService audioService;

    @GetMapping
    public ResponseEntity<List<Audio>> getAllAudios() {
        log.info("REST request to get all audios");
        try {
            List<Audio> audios = audioService.getAllAudios();
            log.info("Successfully retrieved {} audios", audios.size());
            return ResponseEntity.ok(audios);
        } catch (Exception e) {
            log.error("Error retrieving all audios: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Audio> getAudioById(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to get audio by id: {}", id);
        try {
            return audioService.getAudioById(id)
                    .map(audio -> {
                        log.info("Successfully retrieved audio with id: {}", id);
                        return ResponseEntity.ok(audio);
                    })
                    .orElseGet(() -> {
                        log.warn("Audio not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error retrieving audio with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/podcast/{podcastId}")
    public ResponseEntity<List<Audio>> getAudiosByPodcastId(
            @PathVariable @Positive(message = "Podcast ID must be positive") Long podcastId) {
        log.info("REST request to get audios for podcast id: {}", podcastId);
        try {
            List<Audio> audios = audioService.getAudiosByPodcastId(podcastId);
            log.info("Successfully retrieved {} audios for podcast id: {}", audios.size(), podcastId);
            return ResponseEntity.ok(audios);
        } catch (Exception e) {
            log.error("Error retrieving audios for podcast {}: {}", podcastId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Audio> createAudio(@Valid @RequestBody Audio audio) {
        log.info("REST request to create new audio with filename: {}", audio.getFilename());
        try {
            Audio result = audioService.createAudio(audio);
            log.info("Successfully created audio with id: {}", result.getId());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid audio data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating audio: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Audio> updateAudio(
            @PathVariable @Positive(message = "ID must be positive") Long id,
            @Valid @RequestBody Audio audio) {
        log.info("REST request to update audio with id: {}", id);
        try {
            Audio result = audioService.updateAudio(id, audio);
            log.info("Successfully updated audio with id: {}", id);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid audio data for update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating audio {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAudio(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to delete audio with id: {}", id);
        try {
            audioService.deleteAudio(id);
            log.info("Successfully deleted audio with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting audio {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
