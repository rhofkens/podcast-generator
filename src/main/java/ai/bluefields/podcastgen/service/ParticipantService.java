package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.model.Participant;
import java.util.List;
import java.util.Optional;

public interface ParticipantService {
    List<Participant> getAllParticipants();
    Optional<Participant> getParticipantById(Long id);
    List<Participant> getParticipantsByPodcastId(Long podcastId);
    Participant createParticipant(Participant participant);
    Participant updateParticipant(Long id, Participant participant);
    void deleteParticipant(Long id);
    Participant generateVoicePreview(Long id);
    Participant createVoiceFromPreview(Long id);
}
