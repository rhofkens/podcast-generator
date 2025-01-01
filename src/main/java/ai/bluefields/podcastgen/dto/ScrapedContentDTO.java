package ai.bluefields.podcastgen.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ScrapedContentDTO {
    private String content;
    private String sourceUrl;
    private String title;
    private String description;
}
