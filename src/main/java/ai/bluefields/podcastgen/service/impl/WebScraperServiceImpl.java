package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.dto.ScrapedContentDTO;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.WebScraperService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebScraperServiceImpl implements WebScraperService {
    
    private static final Logger log = LoggerFactory.getLogger(WebScraperServiceImpl.class);
    private static final int TIMEOUT_MILLIS = 10000;
    
    private final AIService aiService;

    @Override
    public ScrapedContentDTO scrapeUrl(String urlString) {
        log.info("Starting to scrape URL: {}", urlString);
        
        try {
            // Validate URL
            URL url = new URL(urlString);
            
            // Connect and get the document
            Document doc = Jsoup.connect(url.toString())
                .timeout(TIMEOUT_MILLIS)
                .get();
            
            // Extract main content
            List<String> contentParts = new ArrayList<>();
            
            // Try to find main content container
            Elements mainContent = doc.select("main, article, [role=main], .main-content, #main-content");
            if (!mainContent.isEmpty()) {
                // Use the first main content container found
                Element container = mainContent.first();
                extractContent(container, contentParts);
            } else {
                // Fallback to body if no main content container found
                Element body = doc.body();
                extractContent(body, contentParts);
            }
            
            // Join all content parts with newlines
            String rawContent = String.join("\n\n", contentParts);
            
            // Cleanup the content
            String cleanedContent = cleanupContent(rawContent);
            
            // Get page title
            String title = doc.title();

            // Use AI service to rewrite content for podcast context
            String rewrittenContent = aiService.rewriteScrapedContent(
                cleanedContent,
                title,
                "Podcast about " + title
            );
            
            log.info("Successfully scraped and processed content from URL: {}", urlString);
            return ScrapedContentDTO.builder()
                .content(rewrittenContent)
                .sourceUrl(urlString)
                .title(title)
                .build();
            
        } catch (IOException e) {
            log.error("Failed to scrape URL {}: {}", urlString, e.getMessage(), e);
            throw new RuntimeException("Failed to scrape URL: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while scraping URL {}: {}", urlString, e.getMessage(), e);
            throw new RuntimeException("Error scraping URL: " + e.getMessage(), e);
        }
    }
    
    private void extractContent(Element container, List<String> contentParts) {
        // Add null check for container
        if (container == null) {
            log.warn("Container element is null, skipping content extraction");
            return;
        }

        // Get the title if present
        Elements title = container.select("h1");
        if (!title.isEmpty() && title.first() != null) {
            String titleText = title.first().text();
            if (titleText != null && !titleText.trim().isEmpty()) {
                contentParts.add(titleText);
            }
        }
        
        // Get all paragraphs
        Elements paragraphs = container.select("p");
        for (Element p : paragraphs) {
            if (p != null) {
                String text = p.text();
                if (text != null && !text.trim().isEmpty()) {
                    contentParts.add(text);
                }
            }
        }
        
        // Get all headers
        Elements headers = container.select("h2, h3, h4, h5, h6");
        for (Element header : headers) {
            if (header != null) {
                String text = header.text();
                if (text != null && !text.trim().isEmpty()) {
                    contentParts.add(text);
                }
            }
        }
        
        // Get list items
        Elements lists = container.select("ul, ol");
        for (Element list : lists) {
            if (list != null) {
                Elements items = list.select("li");
                for (Element item : items) {
                    if (item != null) {
                        String text = item.text();
                        if (text != null && !text.trim().isEmpty()) {
                            contentParts.add("• " + text);
                        }
                    }
                }
            }
        }
    }
    
    private String cleanupContent(String content) {
        return content
            .replaceAll("\\s+", " ")           // Replace multiple spaces with single space
            .replaceAll("\\n\\s*\\n", "\n\n")  // Replace multiple newlines with double newline
            .trim();                           // Remove leading/trailing whitespace
    }
}
