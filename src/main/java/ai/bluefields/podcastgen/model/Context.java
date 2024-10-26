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
