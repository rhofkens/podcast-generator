package ai.bluefields.podcastgen.service;

import org.springframework.web.multipart.MultipartFile;
import ai.bluefields.podcastgen.dto.ScrapedContentDTO;

public interface DocumentProcessorService {
    ScrapedContentDTO extractContent(MultipartFile file);
}
