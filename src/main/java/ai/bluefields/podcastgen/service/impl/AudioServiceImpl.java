package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Audio;
import ai.bluefields.podcastgen.repository.AudioRepository;
import ai.bluefields.podcastgen.service.AudioService;
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
public class AudioServiceImpl implements AudioService {

    private static final Logger log = LoggerFactory.getLogger(AudioServiceImpl.class);
    private final AudioRepository audioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Audio> getAllAudios() {
        log.info("Fetching all audios");
        try {
            List<Audio> audios = audioRepository.findAll();
            log.info("Successfully retrieved {} audios", audios.size());
            return audios;
        } catch (DataAccessException e) {
            log.error("Database error while fetching all audios: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch audios", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Audio> getAudioById(Long id) {
        log.info("Fetching audio with id: {}", id);
        try {
            Optional<Audio> audio = audioRepository.findById(id);
            if (audio.isPresent()) {
                log.info("Found audio with id: {}", id);
            } else {
                log.warn("No audio found with id: {}", id);
            }
            return audio;
        } catch (DataAccessException e) {
            log.error("Database error while fetching audio id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch audio", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Audio> getAudiosByPodcastId(Long podcastId) {
        log.info("Fetching audios for podcast id: {}", podcastId);
        try {
            List<Audio> audios = audioRepository.findByPodcastId(podcastId);
            log.info("Found {} audios for podcast id: {}", audios.size(), podcastId);
            return audios;
        } catch (DataAccessException e) {
            log.error("Database error while fetching audios for podcast {}: {}", podcastId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch audios for podcast", e);
        }
    }

    @Override
    public Audio createAudio(Audio audio) {
        log.info("Creating new audio with filename: {}", audio.getFilename());
        try {
            validateAudio(audio);
            Audio saved = audioRepository.save(audio);
            log.info("Successfully created audio with id: {}", saved.getId());
            return saved;
        } catch (DataAccessException e) {
            log.error("Database error while creating audio: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create audio", e);
        }
    }

    @Override
    public Audio updateAudio(Long id, Audio audio) {
        log.info("Updating audio with id: {}", id);
        try {
            validateAudio(audio);
            return audioRepository.findById(id)
                .map(existingAudio -> {
                    updateAudioFields(existingAudio, audio);
                    Audio updated = audioRepository.save(existingAudio);
                    log.info("Successfully updated audio with id: {}", id);
                    return updated;
                })
                .orElseThrow(() -> {
                    log.warn("Failed to update - audio not found with id: {}", id);
                    return new ResourceNotFoundException("Audio", "id", id);
                });
        } catch (DataAccessException e) {
            log.error("Database error while updating audio {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update audio", e);
        }
    }

    @Override
    public void deleteAudio(Long id) {
        log.info("Deleting audio with id: {}", id);
        try {
            if (!audioRepository.existsById(id)) {
                log.warn("Failed to delete - audio not found with id: {}", id);
                throw new ResourceNotFoundException("Audio", "id", id);
            }
            audioRepository.deleteById(id);
            log.info("Successfully deleted audio with id: {}", id);
        } catch (DataAccessException e) {
            log.error("Database error while deleting audio {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete audio", e);
        }
    }

    private void validateAudio(Audio audio) {
        if (audio == null) {
            throw new IllegalArgumentException("Audio cannot be null");
        }
        if (audio.getFilename() == null || audio.getFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("Audio filename cannot be empty");
        }
        if (audio.getFilePath() == null || audio.getFilePath().trim().isEmpty()) {
            throw new IllegalArgumentException("Audio file path cannot be empty");
        }
        // Add more validation as needed
    }

    private void updateAudioFields(Audio existing, Audio updated) {
        existing.setFilename(updated.getFilename());
        existing.setFilePath(updated.getFilePath());
        existing.setFileSize(updated.getFileSize());
        existing.setDuration(updated.getDuration());
        existing.setFormat(updated.getFormat());
        existing.setQualityMetrics(updated.getQualityMetrics());
        existing.setPodcast(updated.getPodcast());
    }
}
