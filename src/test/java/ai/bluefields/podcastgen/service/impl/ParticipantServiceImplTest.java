package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Participant;
import ai.bluefields.podcastgen.repository.ParticipantRepository;
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
    void getParticipantsByPodcastId_ShouldReturnParticipantsForPodcast() {
        when(participantRepository.findByPodcastId(1L)).thenReturn(Arrays.asList(participant));

        List<Participant> result = participantService.getParticipantsByPodcastId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        verify(participantRepository).findByPodcastId(1L);
    }

    @Test
    void createParticipant_ShouldSaveAndReturnParticipant() {
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        Participant result = participantService.createParticipant(participant);

        assertThat(result.getName()).isEqualTo("John Doe");
        verify(participantRepository).save(participant);
    }

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

    @Test
    void deleteParticipant_ShouldDeleteParticipant() {
        when(participantRepository.existsById(1L)).thenReturn(true);
        
        participantService.deleteParticipant(1L);

        verify(participantRepository).deleteById(1L);
    }
}
