package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Audio;
import ai.bluefields.podcastgen.service.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audios")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    @GetMapping
    public ResponseEntity<List<Audio>> getAllAudios() {
        return ResponseEntity.ok(audioService.getAllAudios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Audio> getAudioById(@PathVariable Long id) {
        return audioService.getAudioById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/podcast/{podcastId}")
    public ResponseEntity<List<Audio>> getAudiosByPodcastId(@PathVariable Long podcastId) {
        return ResponseEntity.ok(audioService.getAudiosByPodcastId(podcastId));
    }

    @PostMapping
    public ResponseEntity<Audio> createAudio(@RequestBody Audio audio) {
        return new ResponseEntity<>(audioService.createAudio(audio), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Audio> updateAudio(@PathVariable Long id, @RequestBody Audio audio) {
        return ResponseEntity.ok(audioService.updateAudio(id, audio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAudio(@PathVariable Long id) {
        audioService.deleteAudio(id);
        return ResponseEntity.noContent().build();
    }
}
