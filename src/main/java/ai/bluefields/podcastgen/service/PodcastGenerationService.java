package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.model.Podcast;
import java.util.concurrent.CompletableFuture;

public interface PodcastGenerationService {
    /**
     * Starts the podcast generation process asynchronously
     * @param podcastId ID of the podcast to generate
     * @return CompletableFuture that completes when generation is done
     */
    CompletableFuture<Void> generatePodcast(Long podcastId);
    
    /**
     * Gets the current generation status for a podcast
     * @param podcastId ID of the podcast
     * @return Current podcast with status information
     */
    Podcast getGenerationStatus(Long podcastId);
}

