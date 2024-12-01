package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Transcript;
import ai.bluefields.podcastgen.repository.TranscriptRepository;
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
class TranscriptServiceImplTest {

    @Mock
    private TranscriptRepository transcriptRepository;

    @InjectMocks
    private TranscriptServiceImpl transcriptService;

    private Transcript transcript;
    private ObjectMapper objectMapper;
    private JsonNode testContent;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testContent = objectMapper.readTree("""
            {
                "messages": [
                    {
                        "participantId": 1,
                        "content": "Test Content",
                        "timing": 0
                    }
                ]
            }
            """);

        transcript = new Transcript();
        transcript.setId(1L);
        transcript.setContent(testContent);
    }

    @Test
    void getAllTranscripts_ShouldReturnAllTranscripts() {
        when(transcriptRepository.findAll()).thenReturn(Arrays.asList(transcript));

        List<Transcript> result = transcriptService.getAllTranscripts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent().get("messages").get(0).get("content").asText())
            .isEqualTo("Test Content");
        verify(transcriptRepository).findAll();
    }

    @Test
    void getTranscriptById_WhenTranscriptExists_ShouldReturnTranscript() {
        when(transcriptRepository.findById(1L)).thenReturn(Optional.of(transcript));

        Optional<Transcript> result = transcriptService.getTranscriptById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getContent().get("messages").get(0).get("content").asText())
            .isEqualTo("Test Content");
        verify(transcriptRepository).findById(1L);
    }

    @Test
    void getTranscriptsByPodcastId_ShouldReturnTranscriptsForPodcast() {
        when(transcriptRepository.findByPodcastId(1L)).thenReturn(Arrays.asList(transcript));

        List<Transcript> result = transcriptService.getTranscriptsByPodcastId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent().get("messages").get(0).get("content").asText())
            .isEqualTo("Test Content");
        verify(transcriptRepository).findByPodcastId(1L);
    }

    @Test
    void createTranscript_ShouldSaveAndReturnTranscript() {
        when(transcriptRepository.save(any(Transcript.class))).thenReturn(transcript);

        Transcript result = transcriptService.createTranscript(transcript);

        assertThat(result.getContent().get("messages").get(0).get("content").asText())
            .isEqualTo("Test Content");
        verify(transcriptRepository).save(transcript);
    }

    @Test
    void updateTranscript_WhenTranscriptExists_ShouldUpdateAndReturnTranscript() {
        when(transcriptRepository.findById(1L)).thenReturn(Optional.of(transcript));
        when(transcriptRepository.save(any(Transcript.class))).thenReturn(transcript);

        Transcript result = transcriptService.updateTranscript(1L, transcript);

        assertThat(result.getContent().get("messages").get(0).get("content").asText())
            .isEqualTo("Test Content");
        verify(transcriptRepository).findById(1L);
        verify(transcriptRepository).save(transcript);
    }

    @Test
    void updateTranscript_WhenTranscriptDoesNotExist_ShouldThrowException() {
        when(transcriptRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transcriptService.updateTranscript(1L, transcript))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transcript");

        verify(transcriptRepository).findById(1L);
        verify(transcriptRepository, never()).save(any());
    }

    @Test
    void deleteTranscript_WhenTranscriptExists_ShouldDeleteTranscript() {
        when(transcriptRepository.findById(1L)).thenReturn(Optional.of(transcript));
        
        transcriptService.deleteTranscript(1L);

        verify(transcriptRepository).findById(1L);
        verify(transcriptRepository).deleteById(1L);
    }

    @Test
    void deleteTranscript_WhenTranscriptDoesNotExist_ShouldThrowException() {
        when(transcriptRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transcriptService.deleteTranscript(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transcript");

        verify(transcriptRepository).findById(1L);
        verify(transcriptRepository, never()).deleteById(any());
    }
}
