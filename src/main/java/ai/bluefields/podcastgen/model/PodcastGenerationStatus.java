package ai.bluefields.podcastgen.model;

public enum PodcastGenerationStatus {
    QUEUED,
    GENERATING_VOICES,
    GENERATING_SEGMENTS,
    STITCHING,
    COMPLETED,
    ERROR,
    CANCELLED
}
