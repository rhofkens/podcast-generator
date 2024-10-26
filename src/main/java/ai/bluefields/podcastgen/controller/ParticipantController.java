package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Participant;
import ai.bluefields.podcastgen.service.ParticipantService;
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
@RequestMapping("/api/participants")
@RequiredArgsConstructor
@Validated
public class ParticipantController {

    private static final Logger log = LoggerFactory.getLogger(ParticipantController.class);
    private final ParticipantService participantService;

    @GetMapping
    public ResponseEntity<List<Participant>> getAllParticipants() {
        log.info("REST request to get all participants");
        try {
            List<Participant> participants = participantService.getAllParticipants();
            log.info("Successfully retrieved {} participants", participants.size());
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            log.error("Error retrieving all participants: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Participant> getParticipantById(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to get participant by id: {}", id);
        try {
            return participantService.getParticipantById(id)
                    .map(participant -> {
                        log.info("Successfully retrieved participant with id: {}", id);
                        return ResponseEntity.ok(participant);
                    })
                    .orElseGet(() -> {
                        log.warn("Participant not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error retrieving participant with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/podcast/{podcastId}")
    public ResponseEntity<List<Participant>> getParticipantsByPodcastId(
            @PathVariable @Positive(message = "Podcast ID must be positive") Long podcastId) {
        log.info("REST request to get participants for podcast id: {}", podcastId);
        try {
            List<Participant> participants = participantService.getParticipantsByPodcastId(podcastId);
            log.info("Successfully retrieved {} participants for podcast id: {}", participants.size(), podcastId);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            log.error("Error retrieving participants for podcast {}: {}", podcastId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Participant> createParticipant(@Valid @RequestBody Participant participant) {
        log.info("REST request to create new participant with name: {}", participant.getName());
        try {
            Participant result = participantService.createParticipant(participant);
            log.info("Successfully created participant with id: {}", result.getId());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid participant data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating participant: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Participant> updateParticipant(
            @PathVariable @Positive(message = "ID must be positive") Long id,
            @Valid @RequestBody Participant participant) {
        log.info("REST request to update participant with id: {}", id);
        try {
            Participant result = participantService.updateParticipant(id, participant);
            log.info("Successfully updated participant with id: {}", id);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid participant data for update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating participant {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to delete participant with id: {}", id);
        try {
            participantService.deleteParticipant(id);
            log.info("Successfully deleted participant with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting participant {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
