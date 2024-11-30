package ai.bluefields.podcastgen.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "participants")
@Getter
@Setter
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String gender;

    private Integer age;

    @Column(name = "role")
    private String role;

    @Column(name = "role_description")
    private String roleDescription;

    @Column(name = "voice_characteristics")
    private String voiceCharacteristics;

    @Column(name = "synthetic_voice_id")
    private String syntheticVoiceId;

    @Column(name = "voice_preview_id")
    private String voicePreviewId;

    @Column(name = "voice_preview_url") 
    private String voicePreviewUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "podcast_id")
    private Podcast podcast;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
