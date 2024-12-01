package ai.bluefields.podcastgen.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "transcripts")
@Getter
@Setter
public class Transcript {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(ai.bluefields.podcastgen.config.hibernate.JsonNodeType.class)
    @Column(name = "content", columnDefinition = "jsonb")
    private JsonNode content;

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
