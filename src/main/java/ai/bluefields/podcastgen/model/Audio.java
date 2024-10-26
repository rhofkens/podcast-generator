package ai.bluefields.podcastgen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audios")
@Getter
@Setter
public class Audio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private Long size;

    private Integer duration;

    private String format;

    @Column(name = "quality_score")
    private Float qualityScore;

    @ManyToOne
    @JoinColumn(name = "podcast_id")
    private Podcast podcast;
}
