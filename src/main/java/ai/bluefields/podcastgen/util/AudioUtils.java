package ai.bluefields.podcastgen.util;

import javazoom.jl.decoder.*;
import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.Lame;
import net.sourceforge.lame.mp3.MPEGMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AudioUtils {
    private static final Logger log = LoggerFactory.getLogger(AudioUtils.class);
    
    public static byte[] concatenateMP3Files(List<Path> mp3Files) throws Exception {
        if (mp3Files == null || mp3Files.isEmpty()) {
            throw new IllegalArgumentException("No MP3 files provided");
        }

        AudioFormat commonFormat = null;
        ByteArrayOutputStream concatenatedPCM = new ByteArrayOutputStream();
        
        // Step 1: Convert each MP3 to PCM and concatenate
        for (Path mp3File : mp3Files) {
            log.debug("Processing MP3 file: {}", mp3File);
            
            try (AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(mp3File.toFile())) {
                // Get or set common format
                if (commonFormat == null) {
                    // Convert to PCM format
                    commonFormat = new AudioFormat(
                        44100,                 // Sample rate
                        16,                    // Sample size in bits
                        2,                     // Channels
                        true,                  // Signed
                        false                  // Little endian
                    );
                }
                
                // Convert MP3 to PCM format if needed
                AudioInputStream pcmStream;
                if (!mp3Stream.getFormat().matches(commonFormat)) {
                    pcmStream = AudioSystem.getAudioInputStream(commonFormat, mp3Stream);
                } else {
                    pcmStream = mp3Stream;
                }
                
                // Read PCM data
                byte[] buffer = new byte[4096];
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
        
        return encodePCMtoMP3(concatenatedStream);
    }
    
    private static byte[] encodePCMtoMP3(AudioInputStream pcmStream) throws Exception {
        LameEncoder encoder = new LameEncoder(pcmStream.getFormat(), 
            256,                // Bitrate (kbps)
            MPEGMode.STEREO,    // STEREO mode
            Lame.QUALITY_HIGHEST, // Quality
            false               // VBR disabled
        );
        
        ByteArrayOutputStream mp3Output = new ByteArrayOutputStream();
        byte[] pcmBuffer = new byte[encoder.getPCMBufferSize()];
        byte[] mp3Buffer = new byte[encoder.getMP3BufferSize()];
        
        int bytesRead;
        while ((bytesRead = pcmStream.read(pcmBuffer)) > 0) {
            int bytesEncoded = encoder.encodeBuffer(pcmBuffer, 0, bytesRead, mp3Buffer);
            mp3Output.write(mp3Buffer, 0, bytesEncoded);
        }
        
        int bytesEncoded = encoder.encodeFinish(mp3Buffer);
        mp3Output.write(mp3Buffer, 0, bytesEncoded);
        
        return mp3Output.toByteArray();
    }
}
