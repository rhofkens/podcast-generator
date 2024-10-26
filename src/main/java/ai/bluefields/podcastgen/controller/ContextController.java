package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Context;
import ai.bluefields.podcastgen.service.ContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contexts")
@RequiredArgsConstructor
public class ContextController {

    private final ContextService contextService;

    @GetMapping
    public ResponseEntity<List<Context>> getAllContexts() {
        return ResponseEntity.ok(contextService.getAllContexts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Context> getContextById(@PathVariable Long id) {
        return contextService.getContextById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Context> createContext(@RequestBody Context context) {
        return new ResponseEntity<>(contextService.createContext(context), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Context> updateContext(@PathVariable Long id, @RequestBody Context context) {
        return ResponseEntity.ok(contextService.updateContext(id, context));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContext(@PathVariable Long id) {
        contextService.deleteContext(id);
        return ResponseEntity.noContent().build();
    }
}
