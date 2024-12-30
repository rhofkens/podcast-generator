package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.model.Podcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PodcastService {
    Page<Podcast> getAllPodcasts(Pageable pageable);
    Optional<Podcast> getPodcastById(Long id);
    Podcast createPodcast(Podcast podcast);
    Podcast updatePodcast(Long id, Podcast podcast);
    void deletePodcast(Long id);
    Podcast generateSamplePodcast();
}
package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.model.Podcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PodcastService {
    /**
     * Get all podcasts for a specific user with pagination
     *
     * @param userId the ID of the user
     * @param pageable pagination information
     * @return Page of podcasts
     */
    Page<Podcast> getAllPodcasts(String userId, Pageable pageable);

    /**
     * Get a specific podcast by ID
     * Note: Service layer will not filter by user ID - this should be done at controller level
     *
     * @param id the ID of the podcast
     * @return Optional containing the podcast if found
     */
    Optional<Podcast> getPodcastById(Long id);

    /**
     * Create a new podcast
     *
     * @param podcast the podcast to create (must include userId)
     * @return the created podcast
     * @throws IllegalArgumentException if podcast data is invalid
     */
    Podcast createPodcast(Podcast podcast);

    /**
     * Update an existing podcast
     *
     * @param id the ID of the podcast to update
     * @param podcast the updated podcast data (must include userId)
     * @return the updated podcast
     * @throws ResourceNotFoundException if podcast not found or doesn't belong to user
     * @throws IllegalArgumentException if podcast data is invalid
     */
    Podcast updatePodcast(Long id, Podcast podcast);

    /**
     * Delete a podcast
     *
     * @param id the ID of the podcast to delete
     * @throws ResourceNotFoundException if podcast not found
     */
    void deletePodcast(Long id);

    /**
     * Generate a sample podcast for a user
     *
     * @param userId the ID of the user
     * @return the generated sample podcast
     */
    Podcast generateSamplePodcast(String userId);

    /**
     * Check if a podcast belongs to a specific user
     *
     * @param id the ID of the podcast
     * @param userId the ID of the user
     * @return true if the podcast belongs to the user, false otherwise
     */
    default boolean isPodcastOwnedByUser(Long id, String userId) {
        return getPodcastById(id)
            .map(podcast -> podcast.getUserId().equals(userId))
            .orElse(false);
    }
}
