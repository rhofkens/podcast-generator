package ai.bluefields.podcastgen.repository;

import ai.bluefields.podcastgen.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByPodcastId(Long podcastId);
}
