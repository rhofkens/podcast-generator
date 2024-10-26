package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Participant;
import ai.bluefields.podcastgen.repository.ParticipantRepository;
import ai.bluefields.podcastgen.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Participant> getParticipantById(Long id) {
        return participantRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participant> getParticipantsByPodcastId(Long podcastId) {
        return participantRepository.findByPodcastId(podcastId);
    }

    @Override
    public Participant createParticipant(Participant participant) {
        return participantRepository.save(participant);
    }

    @Override
    public Participant updateParticipant(Long id, Participant participant) {
        return participantRepository.findById(id)
                .map(existingParticipant -> {
                    participant.setId(id);
                    return participantRepository.save(participant);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Participant", "id", id));
    }

    @Override
    public void deleteParticipant(Long id) {
        participantRepository.deleteById(id);
    }
}
