package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Voice;
import ai.bluefields.podcastgen.service.VoiceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing voice profiles.
 * Provides endpoints for creating, retrieving, updating, and deleting voices.
 */
@RestController
@RequestMapping("/api/voices")
@RequiredArgsConstructor
public class VoiceController {
    private static final Logger log = LoggerFactory.getLogger(VoiceController.class);
    
    private final VoiceService voiceService;

    /**
     * Creates a new voice profile.
     *
     * @param voice The voice to create
     * @return The created voice
     */
    @PostMapping
    public ResponseEntity<Voice> createVoice(@RequestBody Voice voice) {
        log.debug("REST request to create Voice: {}", voice);
        Voice result = voiceService.createVoice(voice);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Updates an existing voice profile.
     *
     * @param id The ID of the voice to update
     * @param voice The updated voice data
     * @return The updated voice
     */
    @PutMapping("/{id}")
    public ResponseEntity<Voice> updateVoice(@PathVariable Long id, @RequestBody Voice voice) {
        log.debug("REST request to update Voice: {}", id);
        voice.setId(id);
        Voice result = voiceService.updateVoice(voice);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves a voice by its ID.
     *
     * @param id The ID of the voice
     * @return The voice if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Voice> getVoice(@PathVariable Long id) {
        log.debug("REST request to get Voice: {}", id);
        return voiceService.getVoiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all voices.
     *
     * @return List of all voices
     */
    @GetMapping
    public ResponseEntity<List<Voice>> getAllVoices() {
        log.debug("REST request to get all Voices");
        List<Voice> voices = voiceService.getAllVoices();
        return ResponseEntity.ok(voices);
    }

    /**
     * Retrieves voices by type.
     *
     * @param voiceType The type of voices to retrieve
     * @return List of voices matching the specified type
     */
    @GetMapping("/type/{voiceType}")
    public ResponseEntity<List<Voice>> getVoicesByType(@PathVariable Voice.VoiceType voiceType) {
        log.debug("REST request to get Voices by type: {}", voiceType);
        List<Voice> voices = voiceService.getVoicesByType(voiceType);
        return ResponseEntity.ok(voices);
    }

    /**
     * Retrieves voices by gender.
     *
     * @param gender The gender to filter by
     * @return List of voices matching the specified gender
     */
    @GetMapping("/gender/{gender}")
    public ResponseEntity<List<Voice>> getVoicesByGender(@PathVariable Voice.Gender gender) {
        log.debug("REST request to get Voices by gender: {}", gender);
        List<Voice> voices = voiceService.getVoicesByGender(gender);
        return ResponseEntity.ok(voices);
    }

    /**
     * Retrieves voices by tag.
     *
     * @param tag The tag to search for
     * @return List of voices containing the specified tag
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Voice>> getVoicesByTag(@PathVariable String tag) {
        log.debug("REST request to get Voices by tag: {}", tag);
        List<Voice> voices = voiceService.getVoicesByTag(tag);
        return ResponseEntity.ok(voices);
    }

    /**
     * Retrieves default voices.
     *
     * @return List of default voices
     */
    @GetMapping("/default")
    public ResponseEntity<List<Voice>> getDefaultVoices() {
        log.debug("REST request to get default Voices");
        List<Voice> voices = voiceService.getDefaultVoices();
        return ResponseEntity.ok(voices);
    }

    /**
     * Retrieves voices by user ID.
     *
     * @param userId The ID of the user
     * @return List of voices belonging to the specified user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Voice>> getVoicesByUserId(@PathVariable String userId) {
        log.debug("REST request to get Voices by user ID: {}", userId);
        List<Voice> voices = voiceService.getVoicesByUserId(userId);
        return ResponseEntity.ok(voices);
    }

    /**
     * Retrieves voices by user ID and voice type.
     *
     * @param userId The ID of the user
     * @param voiceType The type of voices to retrieve
     * @return List of voices matching both criteria
     */
    @GetMapping("/user/{userId}/type/{voiceType}")
    public ResponseEntity<List<Voice>> getVoicesByUserIdAndType(
            @PathVariable String userId,
            @PathVariable Voice.VoiceType voiceType) {
        log.debug("REST request to get Voices by user ID: {} and type: {}", userId, voiceType);
        List<Voice> voices = voiceService.getVoicesByUserIdAndType(userId, voiceType);
        return ResponseEntity.ok(voices);
    }

    /**
     * Retrieves voices by type and gender.
     *
     * @param voiceType The type of voices to retrieve
     * @param gender The gender to filter by
     * @return List of voices matching both criteria
     */
    @GetMapping("/type/{voiceType}/gender/{gender}")
    public ResponseEntity<List<Voice>> getVoicesByTypeAndGender(
            @PathVariable Voice.VoiceType voiceType,
            @PathVariable Voice.Gender gender) {
        log.debug("REST request to get Voices by type: {} and gender: {}", voiceType, gender);
        List<Voice> voices = voiceService.getVoicesByTypeAndGender(voiceType, gender);
        return ResponseEntity.ok(voices);
    }

    /**
     * Deletes a voice by its ID.
     *
     * @param id The ID of the voice to delete
     * @return No content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoice(@PathVariable Long id) {
        log.debug("REST request to delete Voice: {}", id);
        voiceService.deleteVoice(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exception handler for EntityNotFoundException.
     *
     * @param ex The exception
     * @return Not Found response
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleEntityNotFound(EntityNotFoundException ex) {
        log.debug("Handling EntityNotFoundException: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    /**
     * Exception handler for IllegalArgumentException.
     *
     * @param ex The exception
     * @return Bad Request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.debug("Handling IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * Sets a voice as the default for a specific gender.
     *
     * @param id The ID of the voice to set as default
     * @param gender The gender for which this voice should be default
     * @return The updated voice
     */
    @PutMapping("/{id}/default/{gender}")
    public ResponseEntity<Voice> setDefaultVoice(
            @PathVariable Long id,
            @PathVariable Voice.Gender gender) {
        log.debug("REST request to set Voice {} as default {} voice", id, gender);
        
        Voice voice = voiceService.getVoiceById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voice not found with ID: " + id));
                
        if (voice.getGender() != gender) {
            throw new IllegalArgumentException(
                "Cannot set voice as default for different gender. Voice gender: " 
                + voice.getGender() + ", requested gender: " + gender);
        }
        
        Voice updatedVoice = voiceService.setDefaultVoice(voice);
        return ResponseEntity.ok(updatedVoice);
    }
}
