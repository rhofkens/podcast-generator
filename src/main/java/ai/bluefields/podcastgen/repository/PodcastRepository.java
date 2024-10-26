package ai.bluefields.podcastgen.repository;

import ai.bluefields.podcastgen.model.Podcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PodcastRepository extends JpaRepository<Podcast, Long> {
    // Add custom query methods if needed
}
