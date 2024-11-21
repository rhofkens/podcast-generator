package ai.bluefields.podcastgen.dto;

import ai.bluefields.podcastgen.model.Participant;
import lombok.Data;
import java.util.List;

@Data
public class TranscriptGenerationRequest {
    private List<Participant> participants;
}
