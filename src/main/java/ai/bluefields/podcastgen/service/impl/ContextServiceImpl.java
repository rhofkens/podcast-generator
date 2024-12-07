package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Context;
import ai.bluefields.podcastgen.repository.ContextRepository;
import ai.bluefields.podcastgen.service.ContextService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContextServiceImpl implements ContextService {

    private static final Logger log = LoggerFactory.getLogger(ContextServiceImpl.class);
    private final ContextRepository contextRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Context> getAllContexts() {
        log.info("Fetching all contexts");
        try {
            List<Context> contexts = contextRepository.findAll();
            log.info("Successfully retrieved {} contexts", contexts.size());
            return contexts;
        } catch (DataAccessException e) {
            log.error("Database error while fetching all contexts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch contexts", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Context> getContextById(Long id) {
        log.info("Fetching context with id: {}", id);
        try {
            Optional<Context> context = contextRepository.findById(id);
            if (context.isPresent()) {
                log.info("Found context with id: {}", id);
            } else {
                log.warn("No context found with id: {}", id);
            }
            return context;
        } catch (DataAccessException e) {
            log.error("Database error while fetching context id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch context", e);
        }
    }

    @Override
    public Context createContext(Context context) {
        log.info("Creating new context");
        try {
            validateContext(context);
            Context saved = contextRepository.save(context);
            log.info("Successfully created context with id: {}", saved.getId());
            return saved;
        } catch (DataAccessException e) {
            log.error("Database error while creating context: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create context", e);
        }
    }

    @Override
    public Context updateContext(Long id, Context context) {
        log.info("Updating context with id: {}", id);
        try {
            validateContext(context);
            return contextRepository.findById(id)
                .map(existingContext -> {
                    updateContextFields(existingContext, context);
                    Context updated = contextRepository.save(existingContext);
                    log.info("Successfully updated context with id: {}", id);
                    return updated;
                })
                .orElseThrow(() -> {
                    log.warn("Failed to update - context not found with id: {}", id);
                    return new ResourceNotFoundException("Context", "id", id);
                });
        } catch (DataAccessException e) {
            log.error("Database error while updating context {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update context", e);
        }
    }

    @Override
    public void deleteContext(Long id) {
        log.info("Deleting context with id: {}", id);
        try {
            if (!contextRepository.existsById(id)) {
                log.warn("Failed to delete - context not found with id: {}", id);
                throw new ResourceNotFoundException("Context", "id", id);
            }
            contextRepository.deleteById(id);
            log.info("Successfully deleted context with id: {}", id);
        } catch (DataAccessException e) {
            log.error("Database error while deleting context {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete context", e);
        }
    }

    private void validateContext(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (context.getDescriptionText() == null || context.getDescriptionText().trim().isEmpty()) {
            throw new IllegalArgumentException("Context description text cannot be empty");
        }
        // Add more validation as needed
    }

    private void updateContextFields(Context existing, Context updated) {
        existing.setDescriptionText(updated.getDescriptionText());
        existing.setSourceUrl(updated.getSourceUrl());
        existing.setFilePath(updated.getFilePath());
        existing.setProcessedContent(updated.getProcessedContent());
        existing.setPodcast(updated.getPodcast());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Context> getContextByPodcastId(Long podcastId) {
        log.info("Fetching context for podcast id: {}", podcastId);
        try {
            Optional<Context> context = contextRepository.findByPodcastId(podcastId);
            if (context.isPresent()) {
                log.info("Found context for podcast id: {}", podcastId);
            } else {
                log.warn("No context found for podcast id: {}", podcastId);
            }
            return context;
        } catch (DataAccessException e) {
            log.error("Database error while fetching context for podcast {}: {}", podcastId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch context for podcast", e);
        }
    }
}
