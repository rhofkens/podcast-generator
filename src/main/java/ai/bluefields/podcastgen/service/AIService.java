package ai.bluefields.podcastgen.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface AIService {
    JsonNode generateParticipantSuggestions(String podcastTitle, String podcastDescription, String contextDescription);
    JsonNode generatePodcastSuggestion();
    JsonNode generateTranscript(String podcastTitle, String podcastDescription, String contextDescription, List<Participant> participants, int lengthInMinutes);
}
