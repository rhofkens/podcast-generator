package ai.bluefields.podcastgen.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PodcastDTO {
    private Long id;
    private String title;
    private String description;
    private Integer length;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;
}
