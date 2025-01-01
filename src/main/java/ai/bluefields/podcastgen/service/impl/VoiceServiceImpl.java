package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.model.Voice;
import ai.bluefields.podcastgen.repository.VoiceRepository;
import ai.bluefields.podcastgen.service.VoiceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the VoiceService interface that handles voice-related operations.
 * This service manages the creation, retrieval, update, and deletion of voice profiles
 * used in podcast generation.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VoiceServiceImpl implements VoiceService {
    private static final Logger log = LoggerFactory.getLogger(VoiceServiceImpl.class);
    
    private final VoiceRepository voiceRepository;

    /**
     * Creates a new voice profile in the system.
     *
     * @param voice The voice entity to be created
     * @return The created voice with generated ID and timestamps
     * @throws IllegalArgumentException if the voice data is invalid
     * @throws DataIntegrityViolationException if there's a database constraint violation
     */
    @Override
    public Voice createVoice(Voice voice) {
        log.debug("Creating new voice: {}", voice.getName());
        
        validateVoiceData(voice);
        
        try {
            voice.setCreatedAt(ZonedDateTime.now());
            voice.setUpdatedAt(ZonedDateTime.now());
            return voiceRepository.save(voice);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to create voice due to data integrity violation", e);
            throw new DataIntegrityViolationException("Failed to create voice. Possible duplicate external voice ID: " 
                + voice.getExternalVoiceId(), e);
        } catch (Exception e) {
            log.error("Unexpected error while creating voice", e);
            throw new RuntimeException("Failed to create voice: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a voice by its internal ID.
     *
     * @param id The internal ID of the voice
     * @return Optional containing the voice if found, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Voice> getVoiceById(Long id) {
        log.debug("Fetching voice by ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("Voice ID cannot be null");
        }
        
        return voiceRepository.findById(id);
    }

    /**
     * Retrieves a voice by its external voice ID (e.g., ElevenLabs voice ID).
     *
     * @param externalVoiceId The external system's voice ID
     * @return Optional containing the voice if found, empty otherwise
     * @throws IllegalArgumentException if externalVoiceId is null or empty
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Voice> getVoiceByExternalId(String externalVoiceId) {
        log.debug("Fetching voice by external ID: {}", externalVoiceId);
        
        if (!StringUtils.hasText(externalVoiceId)) {
            throw new IllegalArgumentException("External voice ID cannot be null or empty");
        }
        
        return voiceRepository.findByExternalVoiceId(externalVoiceId);
    }

    /**
     * Retrieves all voices in the system.
     *
     * @return List of all voices
     */
    @Override
    @Transactional(readOnly = true)
    public List<Voice> getAllVoices() {
        log.debug("Fetching all voices");
        return voiceRepository.findAll();
    }

    /**
     * Retrieves voices by their type (STANDARD or GENERATED).
     *
     * @param voiceType The type of voices to retrieve
     * @return List of voices matching the specified type
     * @throws IllegalArgumentException if voiceType is null
     */
    @Override
    @Transactional(readOnly = true)
    public List<Voice> getVoicesByType(Voice.VoiceType voiceType) {
        log.debug("Fetching voices by type: {}", voiceType);
        
        if (voiceType == null) {
            throw new IllegalArgumentException("Voice type cannot be null");
        }
        
        return voiceRepository.findByVoiceType(voiceType);
    }

    /**
     * Retrieves voices by gender.
     *
     * @param gender The gender to filter by
     * @return List of voices matching the specified gender
     * @throws IllegalArgumentException if gender is null
     */
    @Override
    @Transactional(readOnly = true)
    public List<Voice> getVoicesByGender(Voice.Gender gender) {
        log.debug("Fetching voices by gender: {}", gender);
        
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        
        return voiceRepository.findByGender(gender);
    }

    /**
     * Retrieves voices that have a specific tag.
     *
     * @param tag The tag to search for
     * @return List of voices containing the specified tag
     * @throws IllegalArgumentException if tag is null or empty
     */
    @Override
    @Transactional(readOnly = true)
    public List<Voice> getVoicesByTag(String tag) {
        log.debug("Fetching voices by tag: {}", tag);
        
        if (!StringUtils.hasText(tag)) {
            throw new IllegalArgumentException("Tag cannot be null or empty");
        }
        
        return voiceRepository.findByTag(tag);
    }

    /**
     * Retrieves all default voices in the system.
     *
     * @return List of default voices
     */
    @Override
    @Transactional(readOnly = true)
    public List<Voice> getDefaultVoices() {
        log.debug("Fetching default voices");
        return voiceRepository.findByIsDefaultTrue();
    }

    /**
     * Retrieves voices associated with a specific user.
     *
     * @param userId The ID of the user
     * @return List of voices belonging to the specified user
     * @throws IllegalArgumentException if userId is null or empty
     */
    @Override
    @Transactional(readOnly = true)
    public List<Voice> getVoicesByUserId(String userId) {
        log.debug("Fetching voices by user ID: {}", userId);
        
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        return voiceRepository.findByUserId(userId);
    }

    /**
     * Updates an existing voice profile.
     *
     * @param voice The voice entity with updated data
     * @return The updated voice
     * @throws IllegalArgumentException if voice data is invalid
     * @throws EntityNotFoundException if voice doesn't exist
     * @throws DataIntegrityViolationException if there's a database constraint violation
     */
    @Override
    public Voice updateVoice(Voice voice) {
        log.debug("Updating voice: {}", voice.getId());
        
        if (voice.getId() == null) {
            throw new IllegalArgumentException("Voice ID cannot be null for update operation");
        }
        
        validateVoiceData(voice);
        
        // Verify voice exists
        voiceRepository.findById(voice.getId())
            .orElseThrow(() -> new EntityNotFoundException("Voice not found with ID: " + voice.getId()));
        
        try {
            voice.setUpdatedAt(ZonedDateTime.now());
            return voiceRepository.save(voice);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to update voice due to data integrity violation", e);
            throw new DataIntegrityViolationException("Failed to update voice. Possible duplicate external voice ID: " 
                + voice.getExternalVoiceId(), e);
        } catch (Exception e) {
            log.error("Unexpected error while updating voice", e);
            throw new RuntimeException("Failed to update voice: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a voice profile by its ID.
     *
     * @param id The ID of the voice to delete
     * @throws IllegalArgumentException if id is null
     * @throws EntityNotFoundException if voice doesn't exist
     */
    @Override
    public void deleteVoice(Long id) {
        log.debug("Deleting voice: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("Voice ID cannot be null");
        }
        
        try {
            if (!voiceRepository.existsById(id)) {
                throw new EntityNotFoundException("Voice not found with ID: " + id);
            }
            voiceRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete voice with ID: {}", id, e);
            throw new RuntimeException("Failed to delete voice: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves voices by both type and gender.
     *
     * @param voiceType The type of voices to retrieve
     * @param gender The gender to filter by
     * @return List of voices matching both criteria
     * @throws IllegalArgumentException if either parameter is null
     */
    @Override
    @Transactional(readOnly = true)
    public List<Voice> getVoicesByTypeAndGender(Voice.VoiceType voiceType, Voice.Gender gender) {
        log.debug("Fetching voices by type: {} and gender: {}", voiceType, gender);
        
        if (voiceType == null) {
            throw new IllegalArgumentException("Voice type cannot be null");
        }
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        
        return voiceRepository.findByVoiceTypeAndGender(voiceType, gender);
    }

    /**
     * Validates voice data before creation or update.
     *
     * @param voice The voice to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateVoiceData(Voice voice) {
        if (voice == null) {
            throw new IllegalArgumentException("Voice cannot be null");
        }
        if (!StringUtils.hasText(voice.getName())) {
            throw new IllegalArgumentException("Voice name cannot be null or empty");
        }
        if (!StringUtils.hasText(voice.getExternalVoiceId())) {
            throw new IllegalArgumentException("External voice ID cannot be null or empty");
        }
        if (voice.getVoiceType() == null) {
            throw new IllegalArgumentException("Voice type cannot be null");
        }
        if (voice.getGender() == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Voice> getVoicesByUserIdAndType(String userId, Voice.VoiceType voiceType) {
        log.debug("Fetching voices by user ID: {} and type: {}", userId, voiceType);
        
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (voiceType == null) {
            throw new IllegalArgumentException("Voice type cannot be null");
        }
        
        return voiceRepository.findByUserIdAndVoiceType(userId, voiceType);
    }
}
