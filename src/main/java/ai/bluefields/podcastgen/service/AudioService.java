package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.model.Audio;
import java.util.List;
import java.util.Optional;

public interface AudioService {
    List<Audio> getAllAudios();
    Optional<Audio> getAudioById(Long id);
    List<Audio> getAudiosByPodcastId(Long podcastId);
    Audio createAudio(Audio audio);
    Audio updateAudio(Long id, Audio audio);
    void deleteAudio(Long id);
}
