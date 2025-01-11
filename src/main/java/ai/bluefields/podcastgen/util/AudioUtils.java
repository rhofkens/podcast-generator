package ai.bluefields.podcastgen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Enumeration;
import javazoom.jl.converter.Converter;

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

        // Create temporary files
        File tempWavFile = File.createTempFile("concat", ".wav");
        File tempMp3File = File.createTempFile("concat", ".mp3");
        
        try {
            // Step 1: Convert each MP3 to PCM and concatenate to WAV
            try (AudioInputStream concatenatedStream = new AudioInputStream(
                    new SequenceInputStream(createInputStreamEnumeration(mp3Files, commonFormat)),
                    commonFormat,
                    AudioSystem.NOT_SPECIFIED)) {
                
                // Write concatenated PCM data to WAV file
                AudioSystem.write(concatenatedStream, AudioFileFormat.Type.WAVE, tempWavFile);
            }

            // Step 2: Convert WAV to MP3 using JLayer
            Converter converter = new Converter();
            converter.convert(tempWavFile.getPath(), tempMp3File.getPath(), null, null);

            // Read the resulting MP3 file
            byte[] mp3Data = new byte[(int) tempMp3File.length()];
            try (FileInputStream fis = new FileInputStream(tempMp3File)) {
                if (fis.read(mp3Data) != mp3Data.length) {
                    throw new IOException("Could not read entire MP3 file");
                }
            }
            
            return mp3Data;
            
        } finally {
            // Clean up temporary files
            if (!tempWavFile.delete()) {
                log.warn("Failed to delete temporary WAV file: {}", tempWavFile);
            }
            if (!tempMp3File.delete()) {
                log.warn("Failed to delete temporary MP3 file: {}", tempMp3File);
            }
        }
    }

    // Helper method to create input stream enumeration
    private static Enumeration<InputStream> createInputStreamEnumeration(
            List<Path> mp3Files, 
            AudioFormat targetFormat) {
        return new Enumeration<InputStream>() {
            private int index = 0;
            
            @Override
            public boolean hasMoreElements() {
                return index < mp3Files.size();
            }
            
            @Override
            public InputStream nextElement() {
                try {
                    Path mp3File = mp3Files.get(index++);
                    AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(mp3File.toFile());
                    return AudioSystem.getAudioInputStream(targetFormat, mp3Stream);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to process MP3 file at index " + (index-1), e);
                }
            }
        };
    }
}
