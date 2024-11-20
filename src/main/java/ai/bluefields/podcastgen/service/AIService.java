package ai.bluefields.podcastgen.service;

import org.springframework.ai.openai.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.Completion;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIService {
    
    private final OpenAiApi openAiApi;
    private final ObjectMapper objectMapper;
    
    public JsonNode generatePodcastSuggestion() {
        String prompt = """
            Generate a creative podcast idea with the following JSON structure:
            {
                "title": "engaging podcast title",
                "description": "brief compelling description (max 200 chars)",
                "length": number between 15-45,
                "contextDescription": "detailed context about the podcast topic and goals (max 500 chars)",
                "sourceUrl": "URL to a real, existing webpage that provides background information relevant to this podcast topic. Must be a real, valid URL to an existing website with relevant content."
            }
            
            Requirements for the URL:
            - Must be a real, existing website
            - Must be relevant to the podcast topic
            - Prefer well-known, reputable sources
            - No social media links
            - Must be a specific article or page, not just a homepage
            
            Make it interesting and creative, but keep it professional.
            """;
        
        Completion completion = openAiApi.completion(prompt);
        String aiResponse = completion.getChoices().get(0).getText();
        try {
            return objectMapper.readTree(aiResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
}
