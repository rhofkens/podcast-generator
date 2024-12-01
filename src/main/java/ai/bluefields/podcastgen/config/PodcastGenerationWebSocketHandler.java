package ai.bluefields.podcastgen.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PodcastGenerationWebSocketHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public PodcastGenerationWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String podcastId = extractPodcastId(session);
        sessions.put(podcastId, session);
    }

    public void sendUpdate(String podcastId, GenerationStatus status) {
        WebSocketSession session = sessions.get(podcastId);
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(status);
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                // Handle error
            }
        }
    }

    private String extractPodcastId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
