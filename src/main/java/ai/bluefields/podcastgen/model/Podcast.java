package ai.bluefields.podcastgen.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "podcasts")
@Getter
@Setter
public class Podcast {
    private static final Logger log = LoggerFactory.getLogger(Podcast.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private String icon;

    private Integer length;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonManagedReference
    @OneToOne(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
    private Context context;

    @JsonManagedReference
    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants;

    @JsonManagedReference
    @OneToOne(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
    private Transcript transcript;

    @ElementCollection
    @CollectionTable(
        name = "podcast_audio_segments",
        joinColumns = @JoinColumn(name = "podcast_id")
    )
    @OrderColumn(name = "segment_index")
    @Column(name = "segment_path")
    private List<String> audioSegmentPaths;

    @JsonManagedReference
    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Audio> audioOutputs;

    @Enumerated(EnumType.STRING)
    private PodcastStatus status;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status")
    private PodcastGenerationStatus generationStatus;

    @Column(name = "generation_progress")
    private Integer generationProgress;

    @Column(name = "generation_message")
    private String generationMessage;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getAudioUrl() {
        log.debug("Getting audio URL for podcast {}", id);
        log.debug("Audio outputs: {}, GenerationStatus: {}", 
            audioOutputs != null ? audioOutputs.size() : "null", 
            generationStatus);
        
        if (audioOutputs != null && !audioOutputs.isEmpty() && 
            generationStatus == PodcastGenerationStatus.COMPLETED) {
            Audio latestAudio = audioOutputs.get(audioOutputs.size() - 1);
            String url = latestAudio.getUrl();
            log.debug("Returning audio URL: {}", url);
            return url;
        }
        log.debug("No audio URL available");
        return null;
    }
}
