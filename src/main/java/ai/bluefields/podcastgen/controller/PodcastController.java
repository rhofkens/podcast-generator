package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Podcast;
import ai.bluefields.podcastgen.service.PodcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/podcasts")
@RequiredArgsConstructor
public class PodcastController {

    private final PodcastService podcastService;

    @GetMapping
    public ResponseEntity<List<Podcast>> getAllPodcasts() {
        return ResponseEntity.ok(podcastService.getAllPodcasts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Podcast> getPodcastById(@PathVariable Long id) {
        return podcastService.getPodcastById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Podcast> createPodcast(@RequestBody Podcast podcast) {
        return new ResponseEntity<>(podcastService.createPodcast(podcast), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Podcast> updatePodcast(@PathVariable Long id, @RequestBody Podcast podcast) {
        return ResponseEntity.ok(podcastService.updatePodcast(id, podcast));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePodcast(@PathVariable Long id) {
        podcastService.deletePodcast(id);
        return ResponseEntity.noContent().build();
    }
}
