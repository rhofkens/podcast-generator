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
    String generateDescriptionFromContent(String content);
    String generateTitleFromContent(String content);
    String generateContextFromContent(String content, int targetLength);
    
    /**
     * Generates relevant voice tags based on participant information.
     * @param participant The participant containing voice and role information
     * @return Array of relevant tags (maximum 5)
     */
    String[] generateVoiceTags(Participant participant);
}
