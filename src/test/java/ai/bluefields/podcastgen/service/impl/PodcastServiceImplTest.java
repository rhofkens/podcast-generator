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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        podcast = new Podcast();
        podcast.setId(1L);
        podcast.setTitle("Test Podcast");
        podcast.setDescription("Test Description");
        podcast.setUserId("test-user");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllPodcasts_ShouldReturnPageOfPodcasts() {
        List<Podcast> podcastList = List.of(podcast);
        Page<Podcast> podcastPage = new PageImpl<>(podcastList, pageable, 1);
        when(podcastRepository.findAll(any(Pageable.class))).thenReturn(podcastPage);

        Page<Podcast> result = podcastService.getAllPodcasts(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Podcast");
        verify(podcastRepository).findAll(pageable);
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
    void createPodcast_WithValidData_ShouldReturnCreatedPodcast() {
        when(podcastRepository.save(any(Podcast.class))).thenReturn(podcast);

        Podcast result = podcastService.createPodcast(podcast);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Podcast");
        verify(podcastRepository).save(podcast);
    }

    @Test
    void createPodcast_WithNullTitle_ShouldThrowException() {
        podcast.setTitle(null);

        assertThatThrownBy(() -> podcastService.createPodcast(podcast))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Podcast title cannot be empty");
    }

    @Test
    void updatePodcast_WhenPodcastExists_ShouldReturnUpdatedPodcast() {
        when(podcastRepository.findById(1L)).thenReturn(Optional.of(podcast));
        when(podcastRepository.save(any(Podcast.class))).thenReturn(podcast);

        Podcast updatedPodcast = new Podcast();
        updatedPodcast.setTitle("Updated Title");
        updatedPodcast.setDescription("Updated Description");
        updatedPodcast.setUserId("test-user");

        Podcast result = podcastService.updatePodcast(1L, updatedPodcast);

        assertThat(result).isNotNull();
        verify(podcastRepository).findById(1L);
        verify(podcastRepository).save(any(Podcast.class));
    }

    @Test
    void updatePodcast_WhenPodcastDoesNotExist_ShouldThrowException() {
        when(podcastRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> podcastService.updatePodcast(1L, podcast))
            .isInstanceOf(ResourceNotFoundException.class);
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
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
