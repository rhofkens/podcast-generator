package ai.bluefields.podcastgen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileFormat;
import javazoom.spi.mpeg.sampled.file.MpegEncoding;
import javazoom.spi.mpeg.sampled.file.MpegFileFormatType;

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
            // Configure MP3 encoding parameters
            Map<String, Object> encodingProperties = Map.of(
                "bitrate", Integer.valueOf(192000),
                "channels", Integer.valueOf(2),
                "quality", Integer.valueOf(0),           // 0 = highest quality
                "vbr", Boolean.FALSE,                    // Use CBR
                "mode", Integer.valueOf(1),              // 1 = Joint Stereo
                "copyright", Boolean.FALSE,
                "original", Boolean.TRUE
            );

            // Create MP3 audio format
            AudioFormat mp3Format = new AudioFormat(
                new MpegEncoding("MPEG1L3"),
                44100.0f,
                AudioSystem.NOT_SPECIFIED,
                2,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                false,
                encodingProperties
            );

            // Write MP3 file with configured format
            AudioFileFormat.Type mp3FileType = new MpegFileFormatType("MP3", "mp3");
            AudioSystem.write(
                AudioSystem.getAudioInputStream(mp3Format, concatenatedStream),
                mp3FileType,
                tempFile
            );

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
