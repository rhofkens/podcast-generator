package ai.bluefields.podcastgen.repository;

import ai.bluefields.podcastgen.model.Audio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AudioRepository extends JpaRepository<Audio, Long> {
    List<Audio> findByPodcastId(Long podcastId);
}
