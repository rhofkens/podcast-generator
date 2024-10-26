package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Transcript;
import ai.bluefields.podcastgen.repository.TranscriptRepository;
import ai.bluefields.podcastgen.service.TranscriptService;
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
public class TranscriptServiceImpl implements TranscriptService {
    
    private static final Logger log = LoggerFactory.getLogger(TranscriptServiceImpl.class);
    private final TranscriptRepository transcriptRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Transcript> getAllTranscripts() {
        log.info("Fetching all transcripts");
        try {
            List<Transcript> transcripts = transcriptRepository.findAll();
            log.info("Successfully retrieved {} transcripts", transcripts.size());
            return transcripts;
        } catch (DataAccessException e) {
            log.error("Database error while fetching all transcripts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transcripts", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transcript> getTranscriptById(Long id) {
        log.info("Fetching transcript with id: {}", id);
        try {
            Optional<Transcript> transcript = transcriptRepository.findById(id);
            if (transcript.isPresent()) {
                log.info("Found transcript with id: {}", id);
            } else {
                log.warn("No transcript found with id: {}", id);
            }
            return transcript;
        } catch (DataAccessException e) {
            log.error("Database error while fetching transcript id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transcript", e);
        }
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
        transcriptRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transcript", "id", id));
        transcriptRepository.deleteById(id);
    }
}
