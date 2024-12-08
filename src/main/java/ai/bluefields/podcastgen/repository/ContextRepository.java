package ai.bluefields.podcastgen.repository;

import ai.bluefields.podcastgen.model.Context;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContextRepository extends JpaRepository<Context, Long> {
    Optional<Context> findByPodcastId(Long podcastId);
}
