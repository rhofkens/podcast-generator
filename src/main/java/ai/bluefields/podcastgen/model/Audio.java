package ai.bluefields.podcastgen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "audios")
@Getter
@Setter
public class Audio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "filename")
    private String filename;

    @Column(name = "file_size")
    private Long fileSize;

    private Integer duration;

    private String format;

    @Type(ai.bluefields.podcastgen.config.hibernate.JsonNodeType.class)
    @Column(name = "quality_metrics", columnDefinition = "jsonb")
    private JsonNode qualityMetrics;

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

    public String getUrl() {
        // Return URL path for accessing the audio file through the API
        return filePath != null ? "/api/audio/" + filePath : null;
    }
}
