package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.exception.ResourceNotFoundException;
import ai.bluefields.podcastgen.model.Context;
import ai.bluefields.podcastgen.repository.ContextRepository;
import ai.bluefields.podcastgen.service.ContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContextServiceImpl implements ContextService {

    private final ContextRepository contextRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Context> getAllContexts() {
        return contextRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Context> getContextById(Long id) {
        return contextRepository.findById(id);
    }

    @Override
    public Context createContext(Context context) {
        return contextRepository.save(context);
    }

    @Override
    public Context updateContext(Long id, Context context) {
        return contextRepository.findById(id)
                .map(existingContext -> {
                    context.setId(id);
                    return contextRepository.save(context);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Context", "id", id));
    }

    @Override
    public void deleteContext(Long id) {
        contextRepository.deleteById(id);
    }
}
