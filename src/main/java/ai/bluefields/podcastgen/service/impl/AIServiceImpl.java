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
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    private static final Logger log = LoggerFactory.getLogger(AIServiceImpl.class);
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    
    @Value("${elevenlabs.api.key}")
    private String elevenLabsApiKey;

    @Value("${app.uploads.voice-previews-path}")
    private String voicePreviewsPath;

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


    private void validateJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("Invalid JSON generated: {}", json);
            throw new IllegalStateException("Generated invalid JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public JsonNode generateVoicePreview(String gender, int age, String voiceCharacteristics) {
        log.info("Generating voice preview for gender: {}, age: {}", gender, age);
        
        try {
            // Compose voice description from participant attributes
            String voiceDescription = String.format(
                "A %d year old %s voice with the following characteristics: %s",
                age,
                gender.toLowerCase(),
                voiceCharacteristics
            );

            // Sample text that's at least 150 characters
            String sampleText = "Hello everyone! I'm excited to share my thoughts on this topic. " +
                "Let me walk you through my perspective and experience. " +
                "I believe this discussion will be both informative and engaging for our listeners. " +
                "I look forward to exploring these ideas together.";

            // Create request body using ObjectNode
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("voice_description", voiceDescription);
            requestBody.put("text", sampleText);

            // Convert to JSON string and validate
            String requestJson = objectMapper.writeValueAsString(requestBody);
            validateJson(requestJson);
            log.debug("Request JSON: {}", requestJson);

            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", elevenLabsApiKey);
            
            // Create HTTP entity with the JSON string
            HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);
            
            // Make API call to ElevenLabs
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.elevenlabs.io/v1/text-to-voice/create-previews",
                requestEntity,
                String.class
            );
            
            // Log response for debugging
            //log.debug("ElevenLabs API Response: {}", response.getBody());
            
            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode previews = responseJson.get("previews");
            
            if (previews == null || !previews.isArray() || previews.size() == 0) {
                throw new RuntimeException("No voice previews received from API");
            }
            
            // Get the first preview
            JsonNode firstPreview = previews.get(0);
            String audioBase64 = firstPreview.get("audio_base_64").asText();
            String generatedVoiceId = firstPreview.get("generated_voice_id").asText();
            
            // Decode base64 and save as audio file
            byte[] audioData = Base64.getDecoder().decode(audioBase64);
            
            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get(voicePreviewsPath));
            
            // Generate unique filename
            String filename = String.format("voice-preview-%s-%s.mp3", 
                UUID.randomUUID().toString(),
                generatedVoiceId);
            
            Path filePath = Paths.get(voicePreviewsPath, filename);
            Files.write(filePath, audioData);
            
            // Create URL for the saved file
            String fileUrl = String.format("/api/uploads/voice-previews/%s", filename);
            
            // Create response with file URL and preview ID
            ObjectNode result = objectMapper.createObjectNode();
            result.put("preview_id", generatedVoiceId);
            result.put("preview_url", fileUrl);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to generate voice preview: {}", e.getMessage(), e);
            throw new RuntimeException("Voice preview generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public JsonNode createVoiceFromPreview(String name, String previewId) {
        log.info("Creating voice from preview ID: {} for name: {}", previewId, name);
        
        try {
            // Create request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("voice_name", name);
            requestBody.put("voice_description", "Generated voice for podcast participant " + name);
            requestBody.put("generated_voice_id", previewId);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", elevenLabsApiKey);
            
            // Create HTTP entity
            HttpEntity<String> requestEntity = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), 
                headers
            );
            
            // Make API call to ElevenLabs using the correct endpoint
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.elevenlabs.io/v1/text-to-voice/create-voice-from-preview",
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
