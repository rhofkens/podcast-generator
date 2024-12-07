package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.model.Context;
import java.util.List;
import java.util.Optional;

public interface ContextService {
    List<Context> getAllContexts();
    Optional<Context> getContextById(Long id);
    Context createContext(Context context);
    Context updateContext(Long id, Context context);
    void deleteContext(Long id);
    Optional<Context> getContextByPodcastId(Long podcastId);
}
