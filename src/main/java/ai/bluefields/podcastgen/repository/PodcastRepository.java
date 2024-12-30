package ai.bluefields.podcastgen.repository;

import ai.bluefields.podcastgen.model.Podcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PodcastRepository extends JpaRepository<Podcast, Long> {
    Page<Podcast> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Optional<Podcast> findByIdAndUserId(Long id, String userId);
    boolean existsByIdAndUserId(Long id, String userId);
}
