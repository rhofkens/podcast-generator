package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Podcast;
import ai.bluefields.podcastgen.repository.PodcastRepository;
import ai.bluefields.podcastgen.service.PodcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PodcastServiceImpl implements PodcastService {
    
    private final PodcastRepository podcastRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Podcast> getAllPodcasts() {
        return podcastRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Podcast> getPodcastById(Long id) {
        return podcastRepository.findById(id);
    }

    @Override
    public Podcast createPodcast(Podcast podcast) {
        return podcastRepository.save(podcast);
    }

    @Override
    public Podcast updatePodcast(Long id, Podcast podcast) {
        return podcastRepository.findById(id)
            .map(existingPodcast -> {
                // Update fields here
                return podcastRepository.save(existingPodcast);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Podcast", "id", id));
    }

    @Override
    public void deletePodcast(Long id) {
        if (!podcastRepository.existsById(id)) {
            throw new ResourceNotFoundException("Podcast", "id", id);
        }
        podcastRepository.deleteById(id);
    }
}
