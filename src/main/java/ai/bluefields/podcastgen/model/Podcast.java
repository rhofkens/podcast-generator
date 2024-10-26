package ai.bluefields.podcastgen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "podcasts")
@Getter
@Setter
public class Podcast {
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

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @OneToOne(mappedBy = "podcast", cascade = CascadeType.ALL)
    private Context context;

    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL)
    private List<Participant> participants;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transcript_id")
    private Transcript transcript;

    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL)
    private List<Audio> audioOutputs;

    @Enumerated(EnumType.STRING)
    private PodcastStatus status;
    
    @Column(name = "user_id", nullable = false)
    private String userId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }
}
