package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.service.AIService;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode generateParticipantSuggestions(String podcastTitle, String podcastDescription, String contextDescription) {
        String promptText = String.format("""
            Given a podcast with the following details:
            Title: %s
            Description: %s
            Context: %s
            
            Generate two suitable participants for this podcast discussion with the following JSON structure:
            {
                "participants": [
                    {
                        "name": "full name",
                        "gender": "male/female",
                        "age": number between 25-65,
                        "role": "professional role or title",
                        "roleDescription": "detailed description of their expertise and relevance (max 200 chars)",
                        "voiceCharacteristics": "description of their speaking style and voice qualities"
                    },
                    {
                        // second participant with same structure
                    }
                ]
            }
            
            Make the participants diverse but relevant to the topic. Their roles and expertise should complement each other 
            and create an interesting dynamic for the podcast discussion.
            """, 
            podcastTitle,
            podcastDescription,
            contextDescription
        );
        
        Prompt prompt = new Prompt(promptText);
        ChatResponse response = chatClient.call(prompt);
        String aiResponse = response.getResult().getOutput().getContent();
        try {
            return objectMapper.readTree(aiResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
    
    @Override
    public JsonNode generatePodcastSuggestion() {
        String promptText = """
            Generate a creative podcast idea with the following JSON structure:
            {
                "title": "engaging podcast title",
                "description": "brief compelling description (max 200 chars)",
                "length": number between 3-10,
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
        
        Prompt prompt = new Prompt(promptText);
        ChatResponse response = chatClient.call(prompt);
        String aiResponse = response.getResult().getOutput().getContent();
        try {
            return objectMapper.readTree(aiResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
}