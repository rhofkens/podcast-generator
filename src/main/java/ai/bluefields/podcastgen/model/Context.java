package ai.bluefields.podcastgen.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "contexts")
@Getter
@Setter
public class Context {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description_text")
    private String descriptionText;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "processed_content")
    private String processedContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonBackReference
    @OneToOne
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
