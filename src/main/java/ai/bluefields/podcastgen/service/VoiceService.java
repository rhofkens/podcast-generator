package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.model.Voice;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing voice profiles in the podcast generation system.
 * Provides methods for creating, retrieving, updating, and deleting voice profiles,
 * as well as various search and filtering capabilities.
 */
public interface VoiceService {

    /**
     * Creates a new voice profile.
     *
     * @param voice The voice entity to create
     * @return The created voice with generated ID and timestamps
     * @throws IllegalArgumentException if voice data is invalid
     */
    Voice createVoice(Voice voice);

    /**
     * Retrieves a voice by its internal ID.
     *
     * @param id The internal ID of the voice
     * @return Optional containing the voice if found, empty otherwise
     * @throws IllegalArgumentException if id is null
     */
    Optional<Voice> getVoiceById(Long id);

    /**
     * Retrieves a voice by its external voice ID (e.g., ElevenLabs voice ID).
     *
     * @param externalVoiceId The external system's voice ID
     * @return Optional containing the voice if found, empty otherwise
     * @throws IllegalArgumentException if externalVoiceId is null or empty
     */
    Optional<Voice> getVoiceByExternalId(String externalVoiceId);

    /**
     * Retrieves all voices in the system.
     *
     * @return List of all voices
     */
    List<Voice> getAllVoices();

    /**
     * Retrieves voices by their type (STANDARD or GENERATED).
     *
     * @param voiceType The type of voices to retrieve
     * @return List of voices matching the specified type
     * @throws IllegalArgumentException if voiceType is null
     */
    List<Voice> getVoicesByType(Voice.VoiceType voiceType);

    /**
     * Retrieves voices by gender.
     *
     * @param gender The gender to filter by
     * @return List of voices matching the specified gender
     * @throws IllegalArgumentException if gender is null
     */
    List<Voice> getVoicesByGender(Voice.Gender gender);

    /**
     * Retrieves voices that have a specific tag.
     *
     * @param tag The tag to search for
     * @return List of voices containing the specified tag
     * @throws IllegalArgumentException if tag is null or empty
     */
    List<Voice> getVoicesByTag(String tag);

    /**
     * Retrieves all default voices in the system.
     *
     * @return List of default voices
     */
    List<Voice> getDefaultVoices();

    /**
     * Retrieves voices associated with a specific user.
     *
     * @param userId The ID of the user
     * @return List of voices belonging to the specified user
     * @throws IllegalArgumentException if userId is null or empty
     */
    List<Voice> getVoicesByUserId(String userId);

    /**
     * Updates an existing voice profile.
     *
     * @param voice The voice entity with updated data
     * @return The updated voice
     * @throws IllegalArgumentException if voice data is invalid
     * @throws jakarta.persistence.EntityNotFoundException if voice doesn't exist
     */
    Voice updateVoice(Voice voice);

    /**
     * Deletes a voice profile by its ID.
     *
     * @param id The ID of the voice to delete
     * @throws IllegalArgumentException if id is null
     * @throws jakarta.persistence.EntityNotFoundException if voice doesn't exist
     */
    void deleteVoice(Long id);

    /**
     * Retrieves voices by both type and gender.
     *
     * @param voiceType The type of voices to retrieve
     * @param gender The gender to filter by
     * @return List of voices matching both criteria
     * @throws IllegalArgumentException if either parameter is null
     */
    List<Voice> getVoicesByTypeAndGender(Voice.VoiceType voiceType, Voice.Gender gender);
}
