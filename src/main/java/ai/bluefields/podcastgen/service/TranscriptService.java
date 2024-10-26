package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.model.Transcript;
import java.util.List;
import java.util.Optional;

public interface TranscriptService {
    List<Transcript> getAllTranscripts();
    Optional<Transcript> getTranscriptById(Long id);
    List<Transcript> getTranscriptsByPodcastId(Long podcastId);
    Transcript createTranscript(Transcript transcript);
    Transcript updateTranscript(Long id, Transcript transcript);
    void deleteTranscript(Long id);
}
