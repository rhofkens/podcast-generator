package ai.bluefields.podcastgen.model;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "podcast_id")
    private Podcast podcast;
}
