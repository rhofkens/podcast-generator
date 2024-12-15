
package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.dto.ScrapedContentDTO;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.DocumentProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentProcessorServiceImpl implements DocumentProcessorService {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentProcessorServiceImpl.class);
    private final AIService aiService;

    @Override
    public ScrapedContentDTO extractContent(MultipartFile file) {
        log.info("Starting to process document: {}", file.getOriginalFilename());
        
        try {
            // Convert MultipartFile to temporary File
            File tempFile = convertMultipartFileToFile(file);
            
            // Create document reader based on file type
            DocumentReader reader = createDocumentReader(tempFile, file.getContentType());
            
            // Extract content from document
            List<Document> documents = reader.get();
            
            // Combine all document content
            String content = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n"));
            
            // Clean up temp file
            tempFile.delete();
            
            // Process through AI service
            String rewrittenContent = aiService.rewriteScrapedContent(
                content,
                file.getOriginalFilename(),
                "Podcast based on document: " + file.getOriginalFilename()
            );
            
            log.info("Successfully processed document: {}", file.getOriginalFilename());
            
            return ScrapedContentDTO.builder()
                .content(rewrittenContent)
                .title(file.getOriginalFilename())
                .sourceUrl(null)
                .build();
                
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        }
    }
    
    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp", getFileExtension(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        return tempFile;
    }
    
    private DocumentReader createDocumentReader(File file, String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type cannot be null");
        }
        
        return switch (contentType.toLowerCase()) {
            case "application/pdf" -> new PagePdfDocumentReader(file.getPath());
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> 
                new TikaDocumentReader(file.getPath());
            default -> throw new IllegalArgumentException("Unsupported file type: " + contentType);
        };
    }
    
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
    }
}
