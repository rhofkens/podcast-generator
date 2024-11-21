package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.model.Participant;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    private static final Logger log = LoggerFactory.getLogger(AIServiceImpl.class);
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode generateTranscript(String podcastTitle, String podcastDescription, String contextDescription, List<Participant> participants, int lengthInMinutes) {
        String participantsDescription = participants.stream()
            .map(p -> String.format("""
                Name: %s
                Role: %s
                Role Description: %s
                Voice Profile:
                  - Voice Characteristics: %s
                  - Speaking Style: Match these voice qualities throughout the dialogue
                  - Voice Pattern: Ensure dialogue reflects these speech characteristics
                Speaking Instructions:
                  - Maintain consistent voice personality
                  - Use language patterns fitting their voice profile
                  - Keep dialogue authentic to their speaking style
                """, 
                p.getName(), 
                p.getRole(), 
                p.getRoleDescription(), 
                p.getVoiceCharacteristics()))
            .collect(Collectors.joining("\n\n"));

        String promptText = String.format("""
            Generate a podcast transcript with the following details:
            
            Title: %s
            Description: %s
            Context: %s
            Length: %d minutes
            
            Participants:
            %s
            
            Generate a natural conversation transcript in JSON format:
            {
                "transcript": [
                    {
                        "speakerName": "participant name",
                        "timeOffset": seconds from start,
                        "duration": length in seconds,
                        "text": "what they say"
                    }
                ]
            }
            
            Requirements:
            - Create a natural flowing conversation between participants
            - Use their expertise and roles appropriately
            - Include relevant details from the context
            - Make timing realistic (pauses, overlaps, etc.)
            - Total duration should match podcast length
            - Keep responses concise (2-3 sentences max per turn)
            - Include some brief introductions at the start
            - Add a wrap-up/conclusion at the end
            - Strictly maintain each participant's voice characteristics in their dialogue
            - Ensure speaking patterns and word choices match their voice profile
            - Make voice personalities distinct and consistent throughout
            """,
            podcastTitle,
            podcastDescription,
            contextDescription,
            lengthInMinutes,
            participantsDescription
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
            
            IMPORTANT: Response must be valid JSON only, no additional text or explanations.
            """, 
            podcastTitle,
            podcastDescription,
            contextDescription
        );
        
        log.debug("Generating participant suggestions with prompt: {}", promptText);
        
        try {
            Prompt prompt = new Prompt(promptText);
            ChatResponse response = chatClient.call(prompt);
            String aiResponse = response.getResult().getOutput().getContent();
            
            log.debug("Received AI response: {}", aiResponse);
            
            try {
                return objectMapper.readTree(aiResponse);
            } catch (Exception e) {
                log.error("Failed to parse AI response as JSON: {}", aiResponse, e);
                throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error during AI participant generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate participants: " + e.getMessage(), e);
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
