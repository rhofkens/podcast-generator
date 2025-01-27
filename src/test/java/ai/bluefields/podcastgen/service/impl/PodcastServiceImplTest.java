package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Podcast;
import ai.bluefields.podcastgen.model.PodcastStatus;
import ai.bluefields.podcastgen.repository.PodcastRepository;
import ai.bluefields.podcastgen.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PodcastServiceImplTest {

    @Mock
    private PodcastRepository podcastRepository;
    
    @Mock
    private AIService aiService;

    @InjectMocks
    private PodcastServiceImpl podcastService;

    private Podcast podcast;
    private Pageable pageable;
    private final String TEST_USER_ID = "test-user";

    @BeforeEach
    void setUp() {
        podcast = new Podcast();
        podcast.setId(1L);
        podcast.setTitle("Test Podcast");
        podcast.setDescription("Test Description");
        podcast.setUserId(TEST_USER_ID);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllPodcasts_ShouldReturnPageOfPodcasts() {
        // Given
        List<Podcast> podcastList = List.of(podcast);
        Page<Podcast> podcastPage = new PageImpl<>(podcastList, pageable, 1);
        when(podcastRepository.findByUserIdOrderByCreatedAtDesc(eq(TEST_USER_ID), any(Pageable.class)))
            .thenReturn(podcastPage);

        // When
        Page<Podcast> result = podcastService.getAllPodcasts(TEST_USER_ID, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Podcast");
        verify(podcastRepository).findByUserIdOrderByCreatedAtDesc(TEST_USER_ID, pageable);
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
        when(podcastRepository.findByIdAndUserId(eq(1L), eq(TEST_USER_ID))).thenReturn(Optional.of(podcast));
        when(podcastRepository.save(any(Podcast.class))).thenReturn(podcast);

        Podcast updatedPodcast = new Podcast();
        updatedPodcast.setTitle("Updated Title");
        updatedPodcast.setDescription("Updated Description");
        updatedPodcast.setUserId(TEST_USER_ID);

        Podcast result = podcastService.updatePodcast(1L, updatedPodcast);

        assertThat(result).isNotNull();
        verify(podcastRepository).findByIdAndUserId(1L, TEST_USER_ID);
        verify(podcastRepository).save(any(Podcast.class));
    }

    @Test
    void updatePodcast_WhenPodcastDoesNotExist_ShouldThrowException() {
        // Given
        when(podcastRepository.findByIdAndUserId(eq(1L), eq(TEST_USER_ID))).thenReturn(Optional.empty());
        
        // Create a valid podcast object for update
        Podcast updatedPodcast = new Podcast();
        updatedPodcast.setUserId(TEST_USER_ID);
        updatedPodcast.setTitle("Updated Title");
        updatedPodcast.setDescription("Updated Description");

        // When/Then
        assertThatThrownBy(() -> podcastService.updatePodcast(1L, updatedPodcast))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deletePodcast_WhenPodcastExists_ShouldDeletePodcast() {
        // Given
        when(podcastRepository.existsById(1L)).thenReturn(true);

        // When
        podcastService.deletePodcast(1L);

        // Then
        verify(podcastRepository).deleteById(1L);
    }

    @Test
    void deletePodcast_WhenPodcastDoesNotExist_ShouldThrowException() {
        when(podcastRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> podcastService.deletePodcast(1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllPodcasts_WithNullUserId_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> podcastService.getAllPodcasts(null, pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User ID cannot be null or empty");
    }

    @Test
    void getAllPodcasts_WithEmptyUserId_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> podcastService.getAllPodcasts("  ", pageable))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User ID cannot be null or empty");
    }

    @Test
    void createPodcast_WithNullUserId_ShouldThrowException() {
        // Given
        podcast.setUserId(null);

        // When/Then
        assertThatThrownBy(() -> podcastService.createPodcast(podcast))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Podcast userId cannot be empty");
    }

    @Test
    void createPodcast_WithEmptyUserId_ShouldThrowException() {
        // Given
        podcast.setUserId("");

        // When/Then
        assertThatThrownBy(() -> podcastService.createPodcast(podcast))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Podcast userId cannot be empty");
    }

    @Test
    void generateSamplePodcast_ShouldReturnValidPodcast() {
        // Given
        JsonNode mockSuggestion = createMockAISuggestion();
        when(aiService.generatePodcastSuggestion()).thenReturn(mockSuggestion);

        // When
        Podcast result = podcastService.generateSamplePodcast(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isNotEmpty();
        assertThat(result.getDescription()).isNotEmpty();
        assertThat(result.getLength()).isBetween(15, 45);
        assertThat(result.getStatus()).isEqualTo(PodcastStatus.DRAFT);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        
        // Context assertions
        assertThat(result.getContext()).isNotNull();
        assertThat(result.getContext().getDescriptionText()).isNotEmpty();
        assertThat(result.getContext().getSourceUrl())
            .isNotEmpty()
            .matches("^https?://.*"); // Basic URL format validation
    }

    private JsonNode createMockAISuggestion() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree("""
                {
                    "title": "Sample Podcast Title",
                    "description": "A sample description",
                    "length": 30,
                    "contextDescription": "Detailed context about the topic",
                    "sourceUrl": "https://example.com/article"
                }
                """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock AI suggestion", e);
        }
    }
}
