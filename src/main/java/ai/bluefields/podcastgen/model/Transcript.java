package ai.bluefields.podcastgen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "transcripts")
@Getter
@Setter
public class Transcript {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "last_edited")
    private LocalDateTime lastEdited;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "podcast_id")
    private Podcast podcast;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastEdited = LocalDateTime.now();
    }
}
