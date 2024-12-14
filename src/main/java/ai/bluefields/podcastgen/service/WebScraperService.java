package ai.bluefields.podcastgen.service;

import ai.bluefields.podcastgen.dto.ScrapedContentDTO;

public interface WebScraperService {
    /**
     * Scrapes content from a given URL
     * @param url The URL to scrape
     * @return DTO containing the scraped content and metadata
     * @throws IllegalArgumentException if URL is invalid
     * @throws RuntimeException if scraping fails
     */
    ScrapedContentDTO scrapeUrl(String url);
}
