package ai.bluefields.podcastgen.service;

import com.fasterxml.jackson.databind.JsonNode;
import ai.bluefields.podcastgen.model.Participant;
import java.util.List;

public interface AIService {
    JsonNode generateParticipantSuggestions(String podcastTitle, String podcastDescription, String contextDescription);
    JsonNode generatePodcastSuggestion();
    JsonNode generateTranscript(String podcastTitle, String podcastDescription, String contextDescription, List<Participant> participants, int lengthInMinutes);
    JsonNode generateVoicePreview(String gender, int age, String voiceCharacteristics);
    JsonNode createVoiceFromPreview(String name, String previewId);
    JsonNode generateAudioSegment(String text, String voiceId, List<String> previousRequestIds, 
        String previousText, String nextText);
    String rewriteScrapedContent(String scrapedText, String podcastTitle, String podcastDescription);
}
