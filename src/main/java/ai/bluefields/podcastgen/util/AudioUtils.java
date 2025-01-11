package ai.bluefields.podcastgen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.tritonus.share.sampled.AudioFileTypes;
import org.tritonus.share.sampled.file.TAudioFileFormat;

public class AudioUtils {
    private static final Logger log = LoggerFactory.getLogger(AudioUtils.class);
    
    public static byte[] concatenateMP3Files(List<Path> mp3Files) throws Exception {
        if (mp3Files == null || mp3Files.isEmpty()) {
            throw new IllegalArgumentException("No MP3 files provided");
        }

        // Define high-quality audio format (44.1kHz, 16-bit, stereo)
        AudioFormat commonFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44100.0f,    // Sample rate
            16,          // Sample size in bits
            2,           // Channels
            4,           // Frame size (bytes)
            44100.0f,    // Frame rate
            false        // Little endian
        );

        ByteArrayOutputStream concatenatedPCM = new ByteArrayOutputStream();
        
        // Step 1: Convert each MP3 to PCM and concatenate
        for (Path mp3File : mp3Files) {
            log.debug("Processing MP3 file: {}", mp3File);
            
            try (AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(mp3File.toFile())) {
                // Convert MP3 to PCM format
                AudioInputStream pcmStream = AudioSystem.getAudioInputStream(commonFormat, mp3Stream);
                
                // Read PCM data
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = pcmStream.read(buffer)) != -1) {
                    concatenatedPCM.write(buffer, 0, bytesRead);
                }
                
                pcmStream.close();
            } catch (Exception e) {
                log.error("Error processing MP3 file {}: {}", mp3File, e.getMessage());
                throw new RuntimeException("Failed to process MP3 file: " + mp3File, e);
            }
        }
        
        // Step 2: Convert concatenated PCM back to MP3
        byte[] pcmData = concatenatedPCM.toByteArray();
        AudioInputStream concatenatedStream = new AudioInputStream(
            new ByteArrayInputStream(pcmData),
            commonFormat,
            pcmData.length / commonFormat.getFrameSize()
        );

        // Create temporary file for the MP3
        File tempFile = File.createTempFile("concat", ".mp3");
        
        try {
            // Configure MP3 encoding parameters using mp3spi
            Map<String, Object> encodingProperties = Map.of(
                "mp3.bitrate", "192000",
                "mp3.channels", "2",
                "mp3.quality", "0",           // 0 = highest quality
                "mp3.vbr", "false",          // Use CBR
                "mp3.mode", "1",             // 1 = Joint Stereo
                "mp3.copyright", "false",
                "mp3.original", "true"
            );

            // Create MP3 file type with encoding properties
            AudioFileFormat.Type mp3Type = new AudioFileTypes.Mp3FileType(
                AudioFileFormat.Type.WAVE,    // Base type
                encodingProperties
            );

            // Write MP3 file with configured properties
            AudioSystem.write(concatenatedStream, mp3Type, tempFile);

            // Read the resulting MP3 file
            byte[] mp3Data = new byte[(int) tempFile.length()];
            try (FileInputStream fis = new FileInputStream(tempFile)) {
                if (fis.read(mp3Data) != mp3Data.length) {
                    throw new IOException("Could not read entire MP3 file");
                }
            }
            
            return mp3Data;
        } finally {
            // Ensure temp file is deleted
            if (!tempFile.delete()) {
                log.warn("Failed to delete temporary file: {}", tempFile);
            }
        }
    }
}
