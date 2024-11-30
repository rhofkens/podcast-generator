package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Participant;
import ai.bluefields.podcastgen.repository.ParticipantRepository;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.ParticipantService;
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
public class ParticipantServiceImpl implements ParticipantService {

    private static final Logger log = LoggerFactory.getLogger(ParticipantServiceImpl.class);
    private final ParticipantRepository participantRepository;
    private final AIService aiService;

    @Override
    @Transactional(readOnly = true)
    public List<Participant> getAllParticipants() {
        log.info("Fetching all participants");
        try {
            List<Participant> participants = participantRepository.findAll();
            log.info("Successfully retrieved {} participants", participants.size());
            return participants;
        } catch (DataAccessException e) {
            log.error("Database error while fetching all participants: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch participants", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Participant> getParticipantById(Long id) {
        log.info("Fetching participant with id: {}", id);
        try {
            Optional<Participant> participant = participantRepository.findById(id);
            if (participant.isPresent()) {
                log.info("Found participant with id: {}", id);
            } else {
                log.warn("No participant found with id: {}", id);
            }
            return participant;
        } catch (DataAccessException e) {
            log.error("Database error while fetching participant id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch participant", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participant> getParticipantsByPodcastId(Long podcastId) {
        log.info("Fetching participants for podcast id: {}", podcastId);
        try {
            List<Participant> participants = participantRepository.findByPodcastId(podcastId);
            log.info("Found {} participants for podcast id: {}", participants.size(), podcastId);
            return participants;
        } catch (DataAccessException e) {
            log.error("Database error while fetching participants for podcast {}: {}", podcastId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch participants for podcast", e);
        }
    }

    @Override
    public Participant createParticipant(Participant participant) {
        log.info("Creating new participant with name: {}", participant.getName());
        try {
            validateParticipant(participant);
            Participant saved = participantRepository.save(participant);
            log.info("Successfully created participant with id: {}", saved.getId());
            return saved;
        } catch (DataAccessException e) {
            log.error("Database error while creating participant: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create participant", e);
        }
    }

    @Override
    public Participant updateParticipant(Long id, Participant participant) {
        log.info("Updating participant with id: {}", id);
        try {
            validateParticipant(participant);
            return participantRepository.findById(id)
                .map(existingParticipant -> {
                    updateParticipantFields(existingParticipant, participant);
                    Participant updated = participantRepository.save(existingParticipant);
                    log.info("Successfully updated participant with id: {}", id);
                    return updated;
                })
                .orElseThrow(() -> {
                    log.warn("Failed to update - participant not found with id: {}", id);
                    return new ResourceNotFoundException("Participant", "id", id);
                });
        } catch (DataAccessException e) {
            log.error("Database error while updating participant {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update participant", e);
        }
    }

    @Override
    public void deleteParticipant(Long id) {
        log.info("Deleting participant with id: {}", id);
        try {
            if (!participantRepository.existsById(id)) {
                log.warn("Failed to delete - participant not found with id: {}", id);
                throw new ResourceNotFoundException("Participant", "id", id);
            }
            participantRepository.deleteById(id);
            log.info("Successfully deleted participant with id: {}", id);
        } catch (DataAccessException e) {
            log.error("Database error while deleting participant {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete participant", e);
        }
    }

    private void validateParticipant(Participant participant) {
        if (participant == null) {
            throw new IllegalArgumentException("Participant cannot be null");
        }
        if (participant.getName() == null || participant.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Participant name cannot be empty");
        }
        // Add more validation as needed
    }

    private void updateParticipantFields(Participant existing, Participant updated) {
        existing.setName(updated.getName());
        existing.setGender(updated.getGender());
        existing.setAge(updated.getAge());
        existing.setRole(updated.getRole());
        existing.setRoleDescription(updated.getRoleDescription());
        existing.setVoiceCharacteristics(updated.getVoiceCharacteristics());
        existing.setSyntheticVoiceId(updated.getSyntheticVoiceId());
        existing.setPodcast(updated.getPodcast());
    }

    @Override
    public Participant generateVoicePreview(Long id) {
        log.info("Generating voice preview for participant id: {}", id);
        try {
            return participantRepository.findById(id)
                .map(participant -> {
                    JsonNode previewResult = aiService.generateVoicePreview(
                        participant.getGender(),
                        participant.getAge(),
                        participant.getVoiceCharacteristics()
                    );
                    
                    participant.setVoicePreviewId(previewResult.get("preview_id").asText());
                    participant.setVoicePreviewUrl(previewResult.get("preview_url").asText());
                    
                    return participantRepository.save(participant);
                })
                .orElseThrow(() -> {
                    log.warn("Participant not found with id: {}", id);
                    return new ResourceNotFoundException("Participant", "id", id);
                });
        } catch (Exception e) {
            log.error("Error generating voice preview for participant {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to generate voice preview", e);
        }
    }

    @Override
    public Participant createVoiceFromPreview(Long id) {
        log.info("Creating voice from preview for participant id: {}", id);
        try {
            return participantRepository.findById(id)
                .map(participant -> {
                    if (participant.getVoicePreviewId() == null) {
                        throw new IllegalStateException("No voice preview exists for this participant");
                    }
                    
                    JsonNode voiceResult = aiService.createVoiceFromPreview(
                        participant.getName(),
                        participant.getVoicePreviewId()
                    );
                    
                    participant.setSyntheticVoiceId(voiceResult.get("voice_id").asText());
                    
                    return participantRepository.save(participant);
                })
                .orElseThrow(() -> {
                    log.warn("Participant not found with id: {}", id);
                    return new ResourceNotFoundException("Participant", "id", id);
                });
        } catch (Exception e) {
            log.error("Error creating voice from preview for participant {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to create voice from preview", e);
        }
    }
}
