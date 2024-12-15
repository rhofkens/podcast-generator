package ai.bluefields.podcastgen.config;


import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.model}")
    private String model;

    @Value("${spring.ai.openai.temperature:0.7}")
    private double temperature;

    @Bean
    public ChatClient chatClient() {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .withModel(model)
            .withTemperature(temperature)
            .build();
            
        return new OpenAiChatClient(apiKey, options);
    }
}
