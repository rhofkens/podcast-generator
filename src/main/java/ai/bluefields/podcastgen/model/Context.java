package ai.bluefields.podcastgen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "contexts")
@Getter
@Setter
public class Context {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String sourceUrl;

    private String uploadedFile;

    @Column(columnDefinition = "TEXT")
    private String processedContent;

    @OneToOne(mappedBy = "context")
    private Podcast podcast;
}
package ai.bluefields.podcastgen.model;

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
    private String description;

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

    @OneToOne
    @JoinColumn(name = "podcast_id")
    private Podcast podcast;
}
