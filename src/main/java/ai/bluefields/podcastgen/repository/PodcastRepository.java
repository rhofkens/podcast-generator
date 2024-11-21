package ai.bluefields.podcastgen.repository;

import ai.bluefields.podcastgen.model.Podcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PodcastRepository extends JpaRepository<Podcast, Long> {
    Page<Podcast> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
package ai.bluefields.podcastgen.repository;

import ai.bluefields.podcastgen.model.Podcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PodcastRepository extends JpaRepository<Podcast, Long> {
    Page<Podcast> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
