package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Audio;
import ai.bluefields.podcastgen.repository.AudioRepository;
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
class AudioServiceImplTest {

    @Mock
    private AudioRepository audioRepository;

    @InjectMocks
    private AudioServiceImpl audioService;

    private Audio audio;

    @BeforeEach
    void setUp() {
        audio = new Audio();
        audio.setId(1L);
        audio.setFilePath("/path/to/audio.mp3");
        audio.setFilename("audio.mp3");
    }

    @Test
    void getAllAudios_ShouldReturnAllAudios() {
        when(audioRepository.findAll()).thenReturn(Arrays.asList(audio));

        List<Audio> result = audioService.getAllAudios();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFilePath()).isEqualTo("/path/to/audio.mp3");
        verify(audioRepository).findAll();
    }

    @Test
    void getAudioById_WhenAudioExists_ShouldReturnAudio() {
        when(audioRepository.findById(1L)).thenReturn(Optional.of(audio));

        Optional<Audio> result = audioService.getAudioById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getFilename()).isEqualTo("audio.mp3");
        verify(audioRepository).findById(1L);
    }

    @Test
    void getAudiosByPodcastId_ShouldReturnAudiosForPodcast() {
        when(audioRepository.findByPodcastId(1L)).thenReturn(Arrays.asList(audio));

        List<Audio> result = audioService.getAudiosByPodcastId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFilePath()).isEqualTo("/path/to/audio.mp3");
        verify(audioRepository).findByPodcastId(1L);
    }

    @Test
    void createAudio_ShouldSaveAndReturnAudio() {
        when(audioRepository.save(any(Audio.class))).thenReturn(audio);

        Audio result = audioService.createAudio(audio);

        assertThat(result.getFilename()).isEqualTo("audio.mp3");
        verify(audioRepository).save(audio);
    }

    @Test
    void updateAudio_WhenAudioExists_ShouldUpdateAndReturnAudio() {
        when(audioRepository.findById(1L)).thenReturn(Optional.of(audio));
        when(audioRepository.save(any(Audio.class))).thenReturn(audio);

        Audio result = audioService.updateAudio(1L, audio);

        assertThat(result.getFilename()).isEqualTo("audio.mp3");
        verify(audioRepository).findById(1L);
        verify(audioRepository).save(audio);
    }

    @Test
    void updateAudio_WhenAudioDoesNotExist_ShouldThrowException() {
        when(audioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> audioService.updateAudio(1L, audio))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Audio");

        verify(audioRepository).findById(1L);
        verify(audioRepository, never()).save(any());
    }

    @Test
    void deleteAudio_ShouldDeleteAudio() {
        when(audioRepository.existsById(1L)).thenReturn(true);
        
        audioService.deleteAudio(1L);

        verify(audioRepository).deleteById(1L);
    }
}
