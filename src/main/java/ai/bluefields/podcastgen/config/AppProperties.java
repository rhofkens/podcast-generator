package ai.bluefields.podcastgen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.uploads")
@Getter
@Setter
public class AppProperties {
    private String basePath;
    private String voicePreviewsPath;
    private String podcastsPath;
}
