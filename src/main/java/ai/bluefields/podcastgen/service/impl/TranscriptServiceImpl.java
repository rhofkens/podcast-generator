package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Transcript;
import ai.bluefields.podcastgen.repository.TranscriptRepository;
import ai.bluefields.podcastgen.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TranscriptServiceImpl implements TranscriptService {
    
    private final TranscriptRepository transcriptRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Transcript> getAllTranscripts() {
        return transcriptRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transcript> getTranscriptById(Long id) {
        return transcriptRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transcript> getTranscriptsByPodcastId(Long podcastId) {
        return transcriptRepository.findByPodcastId(podcastId);
    }

    @Override
    public Transcript createTranscript(Transcript transcript) {
        return transcriptRepository.save(transcript);
    }

    @Override
    public Transcript updateTranscript(Long id, Transcript transcript) {
        return transcriptRepository.findById(id)
            .map(existingTranscript -> {
                // Update fields here
                return transcriptRepository.save(existingTranscript);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Transcript", "id", id));
    }

    @Override
    public void deleteTranscript(Long id) {
        if (!transcriptRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transcript", "id", id);
        }
        transcriptRepository.deleteById(id);
    }
}
