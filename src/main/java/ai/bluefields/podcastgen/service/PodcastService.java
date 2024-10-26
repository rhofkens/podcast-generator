package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.model.Podcast;
import java.util.List;
import java.util.Optional;

public interface PodcastService {
    List<Podcast> getAllPodcasts();
    Optional<Podcast> getPodcastById(Long id);
    Podcast createPodcast(Podcast podcast);
    Podcast updatePodcast(Long id, Podcast podcast);
    void deletePodcast(Long id);
}
