package ai.bluefields.podcastgen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    private String role;

    private String voiceCharacteristics;

    private String speakingPatterns;

    private String syntheticVoiceId;

    @ManyToOne
    @JoinColumn(name = "podcast_id")
    private Podcast podcast;
}
