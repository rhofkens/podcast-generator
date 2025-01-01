
package ai.bluefields.podcastgen.service.impl;

import ai.bluefields.podcastgen.dto.ScrapedContentDTO;
import ai.bluefields.podcastgen.service.AIService;
import ai.bluefields.podcastgen.service.DocumentProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.core.io.FileSystemResource;
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
        File tempFile = null;
        
        try {
            // Create temp file with original extension
            String extension = getFileExtension(file.getOriginalFilename());
            tempFile = File.createTempFile("upload_", extension);
            file.transferTo(tempFile);
            
            // Create document reader based on file type
            DocumentReader reader = createDocumentReader(tempFile, file.getContentType());
            
            // Extract content from document
            List<Document> documents = reader.get();
            
            // Combine all document content
            String content = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n"));
            
            // Calculate appropriate context length based on content size
            int wordCount = content.split("\\s+").length;
            int targetContextLength = calculateTargetContextLength(wordCount);
            
            log.info("Document word count: {}, target context length: {}", wordCount, targetContextLength);
            
            // Generate context with appropriate length
            String processedContent = aiService.generateContextFromContent(content, targetContextLength);
            
            log.info("Successfully processed document: {}", file.getOriginalFilename());
            
            // Generate title and description using AI
            String generatedTitle = aiService.generateTitleFromContent(processedContent);
            String generatedDescription = aiService.generateDescriptionFromContent(processedContent);
            
            return ScrapedContentDTO.builder()
                .content(rewrittenContent)
                .title(generatedTitle)
                .description(generatedDescription)
                .sourceUrl(null)
                .build();
                
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        } finally {
            // Clean up temp file
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
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
        
        try {
            // Convert File to Resource
            org.springframework.core.io.Resource resource = 
                new org.springframework.core.io.FileSystemResource(file);
            
            return switch (contentType.toLowerCase()) {
                case "application/pdf" -> new PagePdfDocumentReader(resource);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> 
                    new TikaDocumentReader(resource);
                default -> throw new IllegalArgumentException("Unsupported file type: " + contentType);
            };
        } catch (Exception e) {
            log.error("Failed to create document reader for file {}: {}", file.getName(), e.getMessage());
            throw new RuntimeException("Failed to create document reader: " + e.getMessage(), e);
        }
    }
    
    private int calculateTargetContextLength(int sourceWordCount) {
        // Base minimum context length
        final int MIN_CONTEXT_LENGTH = 500;
        // Maximum context length to keep processing manageable
        final int MAX_CONTEXT_LENGTH = 3000;
        // Target ratio of context length to source length
        final double CONTEXT_RATIO = 0.3; // 30% of original content
        
        int targetLength = (int) (sourceWordCount * CONTEXT_RATIO);
        
        // Ensure minimum length
        targetLength = Math.max(targetLength, MIN_CONTEXT_LENGTH);
        // Cap at maximum length
        targetLength = Math.min(targetLength, MAX_CONTEXT_LENGTH);
        
        log.debug("Calculated target context length: {} words from source length: {} words", 
            targetLength, sourceWordCount);
        
        return targetLength;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
    }
}
