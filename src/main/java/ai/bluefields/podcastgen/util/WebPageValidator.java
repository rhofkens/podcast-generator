package ai.bluefields.podcastgen.util;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebPageValidator {
    private static final Logger log = LoggerFactory.getLogger(WebPageValidator.class);
    private static final int TIMEOUT_MS = 10000;

    public static boolean isWebPageAccessible(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .ignoreContentType(true)
                .execute();

            int statusCode = response.statusCode();
            String contentType = response.contentType();

            // Check if it's a webpage (HTML) and returns 200 OK
            boolean isAccessible = statusCode == 200 && 
                                 contentType != null && 
                                 contentType.contains("text/html");

            if (!isAccessible) {
                log.debug("Webpage not accessible: {} - Status: {}, Content-Type: {}", 
                    url, statusCode, contentType);
            }

            return isAccessible;

        } catch (Exception e) {
            log.debug("Failed to access webpage {}: {}", url, e.getMessage());
            return false;
        }
    }
}
