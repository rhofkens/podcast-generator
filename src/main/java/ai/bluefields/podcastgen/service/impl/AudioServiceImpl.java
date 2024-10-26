package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Audio;
import ai.bluefields.podcastgen.repository.AudioRepository;
import ai.bluefields.podcastgen.service.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AudioServiceImpl implements AudioService {

    private final AudioRepository audioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Audio> getAllAudios() {
        return audioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Audio> getAudioById(Long id) {
        return audioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Audio> getAudiosByPodcastId(Long podcastId) {
        return audioRepository.findByPodcastId(podcastId);
    }

    @Override
    public Audio createAudio(Audio audio) {
        return audioRepository.save(audio);
    }

    @Override
    public Audio updateAudio(Long id, Audio audio) {
        return audioRepository.findById(id)
                .map(existingAudio -> {
                    audio.setId(id);
                    return audioRepository.save(audio);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Audio", "id", id));
    }

    @Override
    public void deleteAudio(Long id) {
        audioRepository.deleteById(id);
    }
}
