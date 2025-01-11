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
        
        // Set up audio format with high quality MP3 parameters
        AudioFormat.Encoding mp3Encoding = new AudioFormat.Encoding("MP3");
        AudioFormat mp3Format = new AudioFormat(
            mp3Encoding,
            44100.0f,    // Sample rate
            16,          // Sample size in bits
            2,           // Channels
            -1,          // Frame size (compressed)
            -1,          // Frame rate (compressed)
            false        // Little endian
        );

        // Set up format properties for high quality MP3
        Map<String, Object> properties = Map.of(
            "bitrate", 192000,          // 192 kbps
            "quality", 0,               // Highest quality
            "channel_mode", "stereo",   // Stereo mode
            "vbr", false,              // Constant bitrate
            "encoding_quality", 0       // Highest encoding quality
        );

        AudioFileFormat.Type mp3FileType = new AudioFileTypes.Mp3FileType(
            "MP3",
            "mp3",
            properties
        );

        // Write to temporary file with high quality settings
        AudioSystem.write(
            new AudioInputStream(concatenatedStream, mp3Format, concatenatedStream.getFrameLength()),
            mp3FileType,
            tempFile
        );

        // Read the resulting MP3 file
        byte[] mp3Data = new byte[(int) tempFile.length()];
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            fis.read(mp3Data);
        }

        // Clean up
        tempFile.delete();
        
        return mp3Data;
    }
}
