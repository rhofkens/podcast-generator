package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.dto.ScrapedContentDTO;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.DocumentProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.DocumentReader;
import org.springframework.ai.reader.pdf.PDFDocumentReader;
import org.springframework.ai.reader.msoffice.WordDocumentReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentProcessorServiceImpl implements DocumentProcessorService {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentProcessorServiceImpl.class);
    private final AIService aiService;

    @Override
    public ScrapedContentDTO extractContent(MultipartFile file) {
        try {
            // Convert MultipartFile to temporary File
            File tempFile = convertMultipartFileToFile(file);
            
            // Get appropriate document reader based on file type
            DocumentReader reader = getDocumentReader(tempFile, file.getContentType());
            
            // Read and process document
            List<Document> documents = reader.get();
            
            // Combine all document content
            StringBuilder contentBuilder = new StringBuilder();
            for (Document doc : documents) {
                contentBuilder.append(doc.getContent()).append("\n\n");
            }
            
            String rawContent = contentBuilder.toString();
            
            // Use AI service to rewrite content for podcast context
            String rewrittenContent = aiService.rewriteScrapedContent(
                rawContent,
                file.getOriginalFilename(),
                "Podcast based on document: " + file.getOriginalFilename()
            );
            
            // Clean up temp file
            tempFile.delete();
            
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
        File tempFile = File.createTempFile("temp", file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        return tempFile;
    }
    
    private DocumentReader getDocumentReader(File file, String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type cannot be null");
        }
        
        return switch (contentType.toLowerCase()) {
            case "application/pdf" -> new PDFDocumentReader(file);
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> 
                new WordDocumentReader(file);
            default -> throw new IllegalArgumentException("Unsupported file type: " + contentType);
        };
    }
}
