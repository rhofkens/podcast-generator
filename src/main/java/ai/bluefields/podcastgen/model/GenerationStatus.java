package ai.bluefields.podcastgen.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerationStatus {
    private String status;
    private int progress;
    private String message;
}
