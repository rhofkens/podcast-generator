package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Podcast;
import ai.bluefields.podcastgen.repository.PodcastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PodcastServiceImplTest {

    @Mock
    private PodcastRepository podcastRepository;

    @InjectMocks
    private PodcastServiceImpl podcastService;

    private Podcast podcast;

    @BeforeEach
    void setUp() {
        podcast = new Podcast();
        podcast.setId(1L);
        podcast.setTitle("Test Podcast");
        podcast.setDescription("Test Description");
        podcast.setUserId("test-user");
    }

    @Test
    void getAllPodcasts_ShouldReturnAllPodcasts() {
        when(podcastRepository.findAll()).thenReturn(Arrays.asList(podcast));

        List<Podcast> result = podcastService.getAllPodcasts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Podcast");
        verify(podcastRepository).findAll();
    }

    @Test
    void getPodcastById_WhenPodcastExists_ShouldReturnPodcast() {
        when(podcastRepository.findById(1L)).thenReturn(Optional.of(podcast));

        Optional<Podcast> result = podcastService.getPodcastById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Podcast");
        verify(podcastRepository).findById(1L);
    }

    @Test
    void createPodcast_ShouldSaveAndReturnPodcast() {
        when(podcastRepository.save(any(Podcast.class))).thenReturn(podcast);

        Podcast result = podcastService.createPodcast(podcast);

        assertThat(result.getTitle()).isEqualTo("Test Podcast");
        verify(podcastRepository).save(podcast);
    }

    @Test
    void updatePodcast_WhenPodcastExists_ShouldUpdateAndReturnPodcast() {
        when(podcastRepository.findById(1L)).thenReturn(Optional.of(podcast));
        when(podcastRepository.save(any(Podcast.class))).thenReturn(podcast);

        Podcast result = podcastService.updatePodcast(1L, podcast);

        assertThat(result.getTitle()).isEqualTo("Test Podcast");
        verify(podcastRepository).findById(1L);
        verify(podcastRepository).save(podcast);
    }

    @Test
    void updatePodcast_WhenPodcastDoesNotExist_ShouldThrowException() {
        when(podcastRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> podcastService.updatePodcast(1L, podcast))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Podcast");

        verify(podcastRepository).findById(1L);
        verify(podcastRepository, never()).save(any());
    }

    @Test
    void deletePodcast_WhenPodcastExists_ShouldDeletePodcast() {
        when(podcastRepository.existsById(1L)).thenReturn(true);
        
        podcastService.deletePodcast(1L);

        verify(podcastRepository).deleteById(1L);
    }

    @Test
    void deletePodcast_WhenPodcastDoesNotExist_ShouldThrowException() {
        when(podcastRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> podcastService.deletePodcast(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Podcast");

        verify(podcastRepository).existsById(1L);
        verify(podcastRepository, never()).deleteById(any());
    }
}
