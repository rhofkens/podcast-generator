package ai.bluefields.podcastgen.service;

public interface WebScraperService {
    /**
     * Scrapes content from a given URL
     * @param url The URL to scrape
     * @return The scraped content as a string
     * @throws IllegalArgumentException if URL is invalid
     * @throws RuntimeException if scraping fails
     */
    String scrapeUrl(String url);
}
