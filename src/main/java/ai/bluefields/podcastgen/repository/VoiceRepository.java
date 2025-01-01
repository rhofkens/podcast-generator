package ai.bluefields.podcastgen.repository;

import ai.bluefields.podcastgen.model.Voice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface VoiceRepository extends JpaRepository<Voice, Long> {
    
    List<Voice> findByVoiceType(Voice.VoiceType voiceType);
    
    List<Voice> findByUserId(String userId);
    
    List<Voice> findByGender(Voice.Gender gender);
    
    Optional<Voice> findByExternalVoiceId(String externalVoiceId);
    
    List<Voice> findByIsDefaultTrue();
    
    @Query("SELECT v FROM Voice v WHERE :tag = ANY(v.tags)")
    List<Voice> findByTag(@Param("tag") String tag);
    
    @Query("SELECT v FROM Voice v WHERE v.voiceType = :voiceType AND v.gender = :gender")
    List<Voice> findByVoiceTypeAndGender(
        @Param("voiceType") Voice.VoiceType voiceType, 
        @Param("gender") Voice.Gender gender
    );
}
