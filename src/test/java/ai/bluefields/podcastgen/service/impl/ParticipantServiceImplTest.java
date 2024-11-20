package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Participant;
import ai.bluefields.podcastgen.repository.ParticipantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ParticipantServiceImplTest {

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    private Participant participant;

    @BeforeEach
    void setUp() {
        participant = new Participant();
        participant.setId(1L);
        participant.setName("John Doe");
        participant.setGender("Male");
        participant.setAge(30);
    }

    // Create operations
    @Test
    void createParticipant_ShouldSaveAndReturnParticipant() {
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        Participant result = participantService.createParticipant(participant);

        assertThat(result.getName()).isEqualTo("John Doe");
        verify(participantRepository).save(participant);
    }

    @Test
    void createParticipant_WithInvalidData_ShouldThrowException() {
        Participant invalidParticipant = new Participant();
        
        assertThatThrownBy(() -> participantService.createParticipant(invalidParticipant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot be empty");
    }

    // Read operations
    @Test
    void getAllParticipants_ShouldReturnAllParticipants() {
        when(participantRepository.findAll()).thenReturn(Arrays.asList(participant));

        List<Participant> result = participantService.getAllParticipants();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        verify(participantRepository).findAll();
    }

    @Test
    void getParticipantById_WhenParticipantExists_ShouldReturnParticipant() {
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));

        Optional<Participant> result = participantService.getParticipantById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        verify(participantRepository).findById(1L);
    }

    @Test
    void getParticipantById_WhenParticipantDoesNotExist_ShouldReturnEmpty() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Participant> result = participantService.getParticipantById(1L);

        assertThat(result).isEmpty();
        verify(participantRepository).findById(1L);
    }

    @Test
    void getParticipantsByPodcastId_ShouldReturnParticipantsForPodcast() {
        when(participantRepository.findByPodcastId(1L)).thenReturn(Arrays.asList(participant));

        List<Participant> result = participantService.getParticipantsByPodcastId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        verify(participantRepository).findByPodcastId(1L);
    }

    // Update operations
    @Test
    void updateParticipant_WhenParticipantExists_ShouldUpdateAndReturnParticipant() {
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        Participant result = participantService.updateParticipant(1L, participant);

        assertThat(result.getName()).isEqualTo("John Doe");
        verify(participantRepository).findById(1L);
        verify(participantRepository).save(participant);
    }

    @Test
    void updateParticipant_WhenParticipantDoesNotExist_ShouldThrowException() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> participantService.updateParticipant(1L, participant))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Participant");

        verify(participantRepository).findById(1L);
        verify(participantRepository, never()).save(any());
    }

    // Delete operations
    @Test
    void deleteParticipant_WhenParticipantExists_ShouldDeleteParticipant() {
        when(participantRepository.existsById(1L)).thenReturn(true);
        
        participantService.deleteParticipant(1L);

        verify(participantRepository).deleteById(1L);
    }

    @Test
    void deleteParticipant_WhenParticipantDoesNotExist_ShouldThrowException() {
        when(participantRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> participantService.deleteParticipant(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Participant");

        verify(participantRepository).existsById(1L);
        verify(participantRepository, never()).deleteById(any());
    }

    @Test
    void generateSamplePodcast_ShouldReturnValidPodcast() {
        // Given
        JsonNode mockSuggestion = createMockAISuggestion();
        when(aiService.generatePodcastSuggestion()).thenReturn(mockSuggestion);

        // When
        Podcast result = podcastService.generateSamplePodcast();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isNotEmpty();
        assertThat(result.getDescription()).isNotEmpty();
        assertThat(result.getLength()).isBetween(15, 45);
        assertThat(result.getStatus()).isEqualTo(PodcastStatus.DRAFT);
        
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
