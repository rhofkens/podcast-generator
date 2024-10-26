package ai.bluefields.podcastgen.controller;

import ai.bluefields.podcastgen.model.Context;
import ai.bluefields.podcastgen.service.ContextService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/contexts")
@RequiredArgsConstructor
@Validated
public class ContextController {

    private static final Logger log = LoggerFactory.getLogger(ContextController.class);
    private final ContextService contextService;

    @GetMapping
    public ResponseEntity<List<Context>> getAllContexts() {
        log.info("REST request to get all contexts");
        try {
            List<Context> contexts = contextService.getAllContexts();
            log.info("Successfully retrieved {} contexts", contexts.size());
            return ResponseEntity.ok(contexts);
        } catch (Exception e) {
            log.error("Error retrieving all contexts: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Context> getContextById(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to get context by id: {}", id);
        try {
            return contextService.getContextById(id)
                    .map(context -> {
                        log.info("Successfully retrieved context with id: {}", id);
                        return ResponseEntity.ok(context);
                    })
                    .orElseGet(() -> {
                        log.warn("Context not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error retrieving context with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Context> createContext(@Valid @RequestBody Context context) {
        log.info("REST request to create new context");
        try {
            Context result = contextService.createContext(context);
            log.info("Successfully created context with id: {}", result.getId());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid context data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating context: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Context> updateContext(
            @PathVariable @Positive(message = "ID must be positive") Long id,
            @Valid @RequestBody Context context) {
        log.info("REST request to update context with id: {}", id);
        try {
            Context result = contextService.updateContext(id, context);
            log.info("Successfully updated context with id: {}", id);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid context data for update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating context {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContext(
            @PathVariable @Positive(message = "ID must be positive") Long id) {
        log.info("REST request to delete context with id: {}", id);
        try {
            contextService.deleteContext(id);
            log.info("Successfully deleted context with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting context {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
