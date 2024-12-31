package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.model.Participant;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ai.bluefields.podcastgen.util.WebPageValidator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;
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

    @Value("${elevenlabs.api.voice-settings.stability:0.5}")
    private double voiceStability;

    @Value("${elevenlabs.api.voice-settings.similarity-boost:0.75}")
    private double similarityBoost;

    @Value("${elevenlabs.api.model-id:eleven_multilingual_v2}")
    private String modelId;

    @Value("${elevenlabs.api.voice-settings.speaker-boost:true}")
    private boolean speakerBoost;

    @Value("${elevenlabs.api.voice-settings.style-exaggeration:0.45}")
    private double styleExaggeration;

    @Value("${app.uploads.voice-previews-path}")
    private String voicePreviewsPath;

    private JsonNode generateInitialTranscript(String podcastTitle, String podcastDescription, String contextDescription, List<Participant> participants, int lengthInMinutes) {
        String promptText = String.format("""
            You are an expert podcast writer known for creating engaging, dynamic conversations.
            
            Create an entertaining podcast transcript between:
            1. An interviewer: %s - %s
            2. An expert guest: %s - %s
            
            Topic Details:
            Title: %s
            Description: %s
            Context: %s
            Target Duration: %d minutes
            
            Writing Guidelines:
            1. Create a dynamic interview format where:
               - The host asks insightful, sometimes provocative questions (20-30 words each)
               - The expert gives detailed, in-depth answers (100-200 words each) that include:
                   * Specific examples and case studies
                   * Personal experiences and anecdotes
                   * Technical details explained in accessible ways
                   * Surprising insights or counterintuitive points
                   * Real-world applications or implications
               - Include occasional humor and light moments
               - Add some friendly banter and personality
            
            2. Structure:
               - Start with a catchy introduction (60-90 seconds)
               - Build tension/interest through the interview
               - Include 2-3 surprising or humorous moments
               - Each expert response should be substantial (45-90 seconds of speaking time)
               - End with impactful closing thoughts
            
            3. Make it engaging by:
               - Having the expert build complex arguments across multiple points
               - Using storytelling techniques in expert responses
               - Including follow-up questions from the host to dig deeper
               - Making sure expert responses fully explore each topic
               - Adding expert's personal insights and professional experiences
               
            Response Length Guidelines:
            - Host questions: 2-3 sentences (20-30 words)
            - Expert answers: 8-12 sentences (100-200 words)
            - Expert should speak roughly 70%% of the total time
            - Host should speak roughly 30%% of the total time
                   
            Generate in this JSON format:
            {
                "transcript": [
                    {
                        "speakerName": "name",
                        "timeOffset": seconds,
                        "duration": seconds,
                        "text": "dialogue"
                    }
                ]
            }
            """,
            participants.get(0).getName(),
            participants.get(0).getRoleDescription(),
            participants.get(1).getName(),
            participants.get(1).getRoleDescription(),
            podcastTitle,
            podcastDescription,
            contextDescription,
            lengthInMinutes
        );
        
        // Use the existing AI call mechanism
        ChatResponse response = chatClient.prompt()
            .user(promptText)
            .call()
            .chatResponse();
        
        return parseAndValidateResponse(response);
    }

    private JsonNode editTranscript(JsonNode initialTranscript, int targetLengthMinutes) {
        String promptText = String.format("""
            You are an expert podcast editor. Review and improve this transcript to match our requirements.
            
            Current Transcript:
            %s
            
            Target Length: %d minutes (%d seconds)
            
            Edit the transcript to:
            1. Timing Adjustments:
               - Ensure total duration is exactly %d seconds
               - Each segment should be 10-30 seconds (30-100 words)
               - Add natural 2-3 second pauses between segments
            
            2. Dynamic Improvements:
               - Enhance interview dynamics (questions should lead naturally to answers)
               - Add more personality to the dialogue
               - Ensure humor lands well
               - Make transitions smoother
            
            3. Content Balance:
               - 70%% expert insights
               - 30%% host guidance and questions
               - Keep the most engaging parts
               - Cut or compress less interesting segments
            
            Return the edited transcript in the same JSON format.
            """,
            initialTranscript.toString(),
            targetLengthMinutes,
            targetLengthMinutes * 60,
            targetLengthMinutes * 60
        );
        
        ChatResponse response = chatClient.prompt()
            .user(promptText)
            .call()
            .chatResponse();
        
        return parseAndValidateResponse(response);
    }

    @Override
    public JsonNode generateTranscript(String podcastTitle, String podcastDescription, String contextDescription, List<Participant> participants, int lengthInMinutes) {
        try {
            // Step 1: Generate initial creative transcript
            JsonNode initialTranscript = generateInitialTranscript(podcastTitle, podcastDescription, contextDescription, participants, lengthInMinutes);
            
            // Step 2: Edit and refine the transcript - DISABLED
            // JsonNode finalTranscript = editTranscript(initialTranscript, lengthInMinutes);
            
            // Step 3: Validate timing (now returns the transcript instead of throwing)
            return validateTranscriptTiming(initialTranscript, lengthInMinutes);
        } catch (Exception e) {
            log.error("Failed to generate transcript: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate transcript: " + e.getMessage(), e);
        }
    }

    private JsonNode validateTranscriptTiming(JsonNode transcript, int targetLengthMinutes) {
        int totalDuration = 0;
        JsonNode segments = transcript.get("transcript");
        if (segments != null && segments.isArray()) {
            for (JsonNode segment : segments) {
                totalDuration += segment.get("duration").asInt();
            }
        }
        
        int expectedDuration = targetLengthMinutes * 60;
        if (Math.abs(totalDuration - expectedDuration) > 30) { // Allow 30 seconds variance
            log.warn("Generated transcript duration ({} seconds) differs significantly from target ({} seconds). " + 
                    "This may affect the final podcast length.", totalDuration, expectedDuration);
        }
        
        return transcript; // Return the transcript instead of throwing exception
    }

    private JsonNode parseAndValidateResponse(ChatResponse response) {
        String aiResponse = Optional.ofNullable(response)
            .map(ChatResponse::getResult)
            .map(result -> result.getOutput().getContent())
            .orElseThrow(() -> new RuntimeException("No response received from AI service"));

        log.debug("Raw AI response: {}", aiResponse);

        // If response starts with "Here is" or similar text, try to find the JSON part
        if (aiResponse.contains("{")) {
            aiResponse = aiResponse.substring(aiResponse.indexOf("{"));
            if (aiResponse.contains("}")) {
                aiResponse = aiResponse.substring(0, aiResponse.lastIndexOf("}") + 1);
            }
        }

        // Clean up the response by removing markdown formatting
        String cleanedResponse = aiResponse
            .replaceAll("```json\\s*", "") // Remove opening markdown
            .replaceAll("```\\s*$", "")    // Remove closing markdown
            .trim();                       // Remove any extra whitespace
                
        log.debug("Cleaned AI response: {}", cleanedResponse);
        
        try {
            return objectMapper.readTree(cleanedResponse);
        } catch (Exception e) {
            log.error("Failed to parse AI response as JSON: {}", cleanedResponse, e);
            
            // Create a default JSON structure if parsing fails
            ObjectNode fallbackNode = objectMapper.createObjectNode();
            ArrayNode transcriptArray = fallbackNode.putArray("transcript");
            
            // Split the text response into segments
            String[] segments = cleanedResponse.split("\n\n");
            int timeOffset = 0;
            
            for (String segment : segments) {
                if (!segment.trim().isEmpty()) {
                    ObjectNode segmentNode = transcriptArray.addObject();
                    segmentNode.put("speakerName", "Speaker");
                    segmentNode.put("timeOffset", timeOffset);
                    segmentNode.put("duration", 30); // default duration
                    segmentNode.put("text", segment.trim());
                    timeOffset += 30;
                }
            }
            
            log.info("Created fallback transcript structure from text response");
            return fallbackNode;
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
                        "role": "Interview host",
                        "roleDescription": "Professional podcast host with expertise relevant to the topic (max 200 chars)",
                        "voiceCharacteristics": "description of their speaking style and voice qualities"
                    },
                    {
                        "name": "full name",
                        "gender": "male/female",
                        "age": number between 25-65,
                        "role": "professional role or title",
                        "roleDescription": "detailed description of their expertise and relevance (max 200 chars)",
                        "voiceCharacteristics": "description of their speaking style and voice qualities"
                    }
                ]
            }
            
            Make the participants diverse but relevant to the topic. The first participant must be an interview host, 
            while the second participant should be an expert or professional in the field being discussed. Their roles 
            and expertise should complement each other and create an interesting dynamic for the podcast discussion.
            
            IMPORTANT: Return ONLY the JSON object, no markdown formatting or additional text.
            """, 
            podcastTitle,
            podcastDescription,
            contextDescription
        );
        
        log.debug("Generating participant suggestions with prompt: {}", promptText);
        
        try {
            ChatResponse response = chatClient.prompt()
                .user(promptText)
                .call()
                .chatResponse();
                
            String aiResponse = Optional.ofNullable(response)
                .map(ChatResponse::getResult)
                .map(result -> result.getOutput().getContent())
                .orElseThrow(() -> new RuntimeException("No response received from AI service"));

            // Clean up the response by removing markdown formatting
            String cleanedResponse = aiResponse
                .replaceAll("```json\\s*", "") // Remove opening markdown
                .replaceAll("```\\s*$", "")    // Remove closing markdown
                .trim();                       // Remove any extra whitespace
                
            log.debug("Cleaned AI response: {}", cleanedResponse);
            
            try {
                return objectMapper.readTree(cleanedResponse);
            } catch (Exception e) {
                log.error("Failed to parse AI response as JSON: {}", cleanedResponse, e);
                throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error during AI participant generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate participants: " + e.getMessage(), e);
        }
    }
    
    private JsonNode validateAndFixPodcastSuggestion(JsonNode suggestion) {
        try {
            log.debug("Validating podcast suggestion: {}", suggestion);
            
            String title = suggestion.get("title").asText();
            String description = suggestion.get("contextDescription").asText();
            String sourceUrl = suggestion.get("sourceUrl").asText();
            
            log.debug("Extracted URL from suggestion: {}", sourceUrl);

            // Validate and potentially fix the URL
            String validatedUrl = ensureValidSourceUrl(sourceUrl, title, description);
            log.debug("Validation result URL: {}", validatedUrl);
            
            // If we couldn't get a valid URL after retries
            if (validatedUrl == null) {
                log.warn("No valid URL could be generated, creating suggestion without URL");
                ObjectNode modifiedSuggestion = objectMapper.createObjectNode();
                modifiedSuggestion.put("title", title);
                modifiedSuggestion.put("description", suggestion.get("description").asText());
                modifiedSuggestion.put("length", suggestion.get("length").asInt());
                modifiedSuggestion.put("contextDescription", description);
                modifiedSuggestion.put("sourceUrl", "");
                return modifiedSuggestion;
            }

            // If URL is valid but different from original
            if (!validatedUrl.equals(sourceUrl)) {
                log.info("Using alternative valid URL: {} instead of original: {}", validatedUrl, sourceUrl);
                ObjectNode modifiedSuggestion = objectMapper.createObjectNode();
                modifiedSuggestion.put("title", title);
                modifiedSuggestion.put("description", suggestion.get("description").asText());
                modifiedSuggestion.put("length", suggestion.get("length").asInt());
                modifiedSuggestion.put("contextDescription", description);
                modifiedSuggestion.put("sourceUrl", validatedUrl);
                return modifiedSuggestion;
            }

            log.debug("Original URL was valid, returning original suggestion");
            return suggestion;
        } catch (Exception e) {
            log.error("Error validating podcast suggestion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to validate podcast suggestion", e);
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
            - Must be from a major, well-known website (Wikipedia, major news sites, educational institutions)
            - Must be relevant to the podcast topic
            - Must be a specific article or page, not just a homepage
            - No social media links
            - The webpage must currently exist and be accessible
            
            IMPORTANT: Return ONLY the JSON object, no markdown formatting or additional text.
            """;
        
        try {
            ChatResponse response = chatClient.prompt()
                .user(promptText)
                .call()
                .chatResponse();
                
            String aiResponse = Optional.ofNullable(response)
                .map(ChatResponse::getResult)
                .map(result -> result.getOutput().getContent())
                .orElseThrow(() -> new RuntimeException("No response received from AI service"));

            // Clean up the response
            String cleanedResponse = aiResponse
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*$", "")
                .trim();

            try {
                JsonNode initialSuggestion = objectMapper.readTree(cleanedResponse);
                // Validate and potentially fix the URL
                return validateAndFixPodcastSuggestion(initialSuggestion);
            } catch (Exception e) {
                log.error("Failed to parse AI response as JSON: {}", cleanedResponse, e);
                throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error during AI podcast suggestion generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate podcast suggestion: " + e.getMessage(), e);
        }
    }


    private String ensureValidSourceUrl(String initialUrl, String title, String context) {
        log.debug("Validating source URL: {}", initialUrl);
        
        // First check the initial URL
        if (initialUrl != null && !initialUrl.trim().isEmpty()) {
            boolean isAccessible = WebPageValidator.isWebPageAccessible(initialUrl);
            log.debug("Initial URL {} accessibility check result: {}", initialUrl, isAccessible);
            
            if (isAccessible) {
                return initialUrl;
            }
        }
        
        // If initial URL is not valid, try to generate alternatives
        log.info("Initial URL {} is not accessible, attempting to generate alternatives", initialUrl);
        
        // Try up to 3 times to generate a valid URL
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                String prompt = String.format("""
                    The webpage %s is not accessible. Generate a URL for a currently existing webpage 
                    that discusses this topic:
                    
                    Title: %s
                    Context: %s
                    
                    Requirements:
                    - URL must be from a major, well-known website (e.g., Wikipedia, major news sites)
                    - Webpage must currently exist and be accessible
                    - Content must be relevant to the topic
                    - Return only the URL, nothing else
                    """, initialUrl, title, context);

                ChatResponse response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .chatResponse();

                String newUrl = response.getResult().getOutput().getContent().trim();
                log.debug("Generated alternative URL (attempt {}): {}", attempt + 1, newUrl);
                
                // Basic URL format validation
                if (!newUrl.startsWith("http")) {
                    newUrl = "https://" + newUrl;
                }
                
                if (WebPageValidator.isWebPageAccessible(newUrl)) {
                    log.info("Successfully generated accessible alternative URL: {}", newUrl);
                    return newUrl;
                }
                
                log.debug("Generated URL {} is not accessible, trying again", newUrl);
            } catch (Exception e) {
                log.warn("Error generating alternative URL on attempt {}: {}", attempt + 1, e.getMessage());
            }
        }
        
        log.warn("Failed to generate any accessible webpage URL after multiple attempts");
        return null;
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
            
            if (previews == null || !previews.isArray() || previews.isEmpty()) {
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
                UUID.randomUUID(),
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

    @Override
    public JsonNode generateAudioSegment(String text, String voiceId, List<String> previousRequestIds,
            String previousText, String nextText) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("text", text);
            requestBody.put("model_id", modelId);
            
            ObjectNode voiceSettings = objectMapper.createObjectNode();
            voiceSettings.put("stability", voiceStability);
            voiceSettings.put("similarity_boost", similarityBoost);
            voiceSettings.put("speaker_boost", speakerBoost);
            voiceSettings.put("style_exaggeration", styleExaggeration);
            requestBody.set("voice_settings", voiceSettings);
            
            if (previousRequestIds != null && !previousRequestIds.isEmpty()) {
                requestBody.set("previous_request_ids", 
                    objectMapper.valueToTree(previousRequestIds.subList(0, 
                        Math.min(previousRequestIds.size(), 3))));
            }
            
            if (previousText != null) {
                requestBody.put("previous_text", previousText);
            }
            
            if (nextText != null) {
                requestBody.put("next_text", nextText);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", elevenLabsApiKey);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), 
                headers
            );
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(
                String.format("https://api.elevenlabs.io/v1/text-to-speech/%s", voiceId),
                HttpMethod.POST,
                requestEntity,
                byte[].class
            );
            
            // Return response metadata including request-id
            ObjectNode result = objectMapper.createObjectNode();
            result.put("request_id", response.getHeaders().getFirst("request-id"));
            result.put("audio_data", Base64.getEncoder().encodeToString(response.getBody()));
            
            return result;
        } catch (Exception e) {
            log.error("Failed to generate audio segment: {}", e.getMessage(), e);
            throw new RuntimeException("Audio generation failed", e);
        }
    }

    @Override
    public String rewriteScrapedContent(String scrapedText, String podcastTitle, String podcastDescription) {
        log.info("Rewriting scraped content for podcast: {}", podcastTitle);
        
        String promptText = String.format("""
            Rewrite the following content to be more natural and podcast-friendly.
            The content should be engaging and suitable for a podcast discussion.
            
            Podcast Title: %s
            Podcast Description: %s
            
            Original Content:
            %s
            
            Requirements:
            1. Maintain all key information and facts from the original
            2. Make it more conversational and engaging
            3. Break up long paragraphs into digestible segments
            4. Remove any web-specific formatting or references
            5. Keep technical terms but explain them naturally
            6. Aim for a clear, flowing narrative
            7. Make it suitable for spoken discussion
            
            Return only the rewritten content, no additional formatting or metadata.
            """,
            podcastTitle,
            podcastDescription,
            scrapedText
        );
        
        try {
            ChatResponse response = chatClient.prompt()
                .user(promptText)
                .call()
                .chatResponse();
                
            String rewrittenContent = Optional.ofNullable(response)
                .map(ChatResponse::getResult)
                .map(result -> result.getOutput().getContent())
                .orElseThrow(() -> new RuntimeException("No response received from AI service"));
            log.debug("Successfully rewrote content, new length: {} chars", rewrittenContent.length());
            return rewrittenContent;
            
        } catch (Exception e) {
            log.error("Failed to rewrite content: {}", e.getMessage(), e);
            throw new RuntimeException("Content rewriting failed: " + e.getMessage(), e);
        }
    }
}
