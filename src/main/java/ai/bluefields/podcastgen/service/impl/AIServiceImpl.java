package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.model.Participant;
import org.springframework.ai.chat.ChatClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            Target Duration: %d minutes (IMPORTANT: ensure total duration matches this exactly)
            
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
            1. Timing Requirements:
               - Total duration MUST be exactly %d seconds
               - Each segment should be 10-30 seconds.  This means 30-100 words per segment.  Make sure to adhere to these limits!
               - Include natural pauses between segments (2-3 seconds)
               - Track cumulative time to ensure total matches target
            
            2. Content Structure:
               - Start with brief introductions (60-90 seconds total)
               - Main discussion (70%% of total time)
               - Wrap-up/conclusion (10%% of total time)
               - Distribute speaking time evenly between participants
            
            3. Speaking Guidelines:
               - Keep responses concise
               - Include relevant details from the context
               - Make timing realistic for natural speech
               - Maintain each participant's voice characteristics
               - Ensure speaking patterns match voice profiles
               - Make voice personalities distinct and consistent
            
            4. Technical Requirements:
               - Ensure timeOffset values are sequential
               - Duration must reflect realistic speaking pace
               - Total of all durations plus pauses must equal %d seconds
               - JSON must be valid and match the specified structure
            """,
            podcastTitle,
            podcastDescription,
            contextDescription,
            lengthInMinutes,
            participantsDescription,
            lengthInMinutes * 60,
            lengthInMinutes * 60
        );
        
        log.debug("Generating transcript with prompt: {}", promptText);
        
        try {
            Prompt prompt = new Prompt(promptText);
            ChatResponse response = chatClient.call(prompt);
            String aiResponse = response.getResult().getOutput().getContent();
            
            log.debug("Received AI response: {}", aiResponse);
            
            JsonNode transcript = objectMapper.readTree(aiResponse);
            
            // Validate total duration
            int totalDuration = 0;
            JsonNode segments = transcript.get("transcript");
            if (segments != null && segments.isArray()) {
                for (JsonNode segment : segments) {
                    totalDuration += segment.get("duration").asInt();
                }
            }
            
            int expectedDuration = lengthInMinutes * 60;
            if (Math.abs(totalDuration - expectedDuration) > 30) { // Allow 30 seconds variance
                log.warn("Generated transcript duration ({} seconds) significantly differs from target ({} seconds)", 
                        totalDuration, expectedDuration);
            }
            
            return transcript;
        } catch (Exception e) {
            log.error("Failed to generate or parse transcript: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate transcript: " + e.getMessage(), e);
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

    @Override
    public JsonNode createVoiceFromPreview(String name, String previewId) {
        log.info("Creating voice from preview ID: {} for name: {}", previewId, name);
        
        try {
            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);
            requestBody.put("preview_voice_id", previewId);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", "${elevenlabs.api.key}"); // Use configuration property
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Make API call to ElevenLabs using the correct endpoint
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.elevenlabs.io/v1/text-to-voice/create-voice",
                requestEntity,
                String.class
            );
            
            // Parse and return response
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to create voice from preview: {}", e.getMessage(), e);
            throw new RuntimeException("Voice creation failed", e);
        }
    }
}
