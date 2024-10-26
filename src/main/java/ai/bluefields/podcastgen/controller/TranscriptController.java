package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Transcript;
import ai.bluefields.podcastgen.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService transcriptService;

    @GetMapping
    public ResponseEntity<List<Transcript>> getAllTranscripts() {
        return ResponseEntity.ok(transcriptService.getAllTranscripts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transcript> getTranscriptById(@PathVariable Long id) {
        return transcriptService.getTranscriptById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/podcast/{podcastId}")
    public ResponseEntity<List<Transcript>> getTranscriptsByPodcastId(@PathVariable Long podcastId) {
        return ResponseEntity.ok(transcriptService.getTranscriptsByPodcastId(podcastId));
    }

    @PostMapping
    public ResponseEntity<Transcript> createTranscript(@RequestBody Transcript transcript) {
        return new ResponseEntity<>(transcriptService.createTranscript(transcript), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transcript> updateTranscript(@PathVariable Long id, @RequestBody Transcript transcript) {
        return ResponseEntity.ok(transcriptService.updateTranscript(id, transcript));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTranscript(@PathVariable Long id) {
        transcriptService.deleteTranscript(id);
        return ResponseEntity.noContent().build();
    }
}
