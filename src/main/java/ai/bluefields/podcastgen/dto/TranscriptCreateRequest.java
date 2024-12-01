package ai.bluefields.podcastgen.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import ai.bluefields.podcastgen.model.Podcast;

@Data
public class TranscriptCreateRequest {
    private Podcast podcast;
    private JsonNode content;
}
