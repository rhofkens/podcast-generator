package ai.bluefields.podcastgen.repository;

import ai.bluefields.podcastgen.model.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
    List<Transcript> findByPodcastId(Long podcastId);
}
