package ai.bluefields.podcastgen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import javazoom.jl.converter.Converter;

public class AudioUtils {
    private static final Logger log = LoggerFactory.getLogger(AudioUtils.class);
    
    public static byte[] concatenateMP3Files(List<Path> mp3Files) throws Exception {
        if (mp3Files == null || mp3Files.isEmpty()) {
            throw new IllegalArgumentException("No MP3 files provided");
        }

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "podcast-concat-" + System.currentTimeMillis());
        tempDir.mkdirs();
        
        try {
            // Step 1: Convert each MP3 to WAV using a more robust approach
            List<File> wavFiles = new ArrayList<>();
            
            for (int i = 0; i < mp3Files.size(); i++) {
                File wavFile = new File(tempDir, "segment_" + i + ".wav");
                try (AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(mp3Files.get(i).toFile())) {
                    // Get base format
                    AudioFormat baseFormat = mp3Stream.getFormat();
                    
                    // Create target format
                    AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                    );
                    
                    // Convert to PCM
                    try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(targetFormat, mp3Stream)) {
                        // Write WAV file
                        AudioSystem.write(pcmStream, AudioFileFormat.Type.WAVE, wavFile);
                    }
                }
                wavFiles.add(wavFile);
                log.debug("Converted MP3 file {} to WAV", mp3Files.get(i));
            }
            
            // Step 2: Concatenate WAV files
            File concatenatedWav = new File(tempDir, "concatenated.wav");
            try (AudioInputStream concatenatedStream = concatenateWavFiles(wavFiles)) {
                AudioSystem.write(concatenatedStream, AudioFileFormat.Type.WAVE, concatenatedWav);
            }
            
            // Step 3: Convert final WAV back to MP3
            File outputMp3 = new File(tempDir, "output.mp3");
            Converter converter = new Converter();
            // Use the String-based convert method instead
            converter.convert(concatenatedWav.getAbsolutePath(), outputMp3.getAbsolutePath());
            
            // Read the final MP3 file
            byte[] mp3Data = new byte[(int) outputMp3.length()];
            try (FileInputStream fis = new FileInputStream(outputMp3)) {
                int bytesRead = 0;
                int offset = 0;
                while (offset < mp3Data.length && 
                       (bytesRead = fis.read(mp3Data, offset, mp3Data.length - offset)) != -1) {
                    offset += bytesRead;
                }
                if (offset != mp3Data.length) {
                    throw new IOException("Could not read entire MP3 file");
                }
            }
            
            return mp3Data;
            
        } finally {
            // Clean up temporary directory and all files
            deleteDirectory(tempDir);
        }
    }
    
    private static AudioInputStream concatenateWavFiles(List<File> wavFiles) throws Exception {
        if (wavFiles.isEmpty()) {
            throw new IllegalArgumentException("No WAV files to concatenate");
        }
        
        // Get format of first file
        AudioInputStream firstStream = AudioSystem.getAudioInputStream(wavFiles.get(0));
        AudioFormat format = firstStream.getFormat();
        firstStream.close();
        
        // Create list of compatible audio streams
        List<AudioInputStream> audioStreams = new ArrayList<>();
        for (File wavFile : wavFiles) {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile);
            AudioFormat streamFormat = audioStream.getFormat();
            
            // Ensure format compatibility
            if (!format.matches(streamFormat)) {
                AudioInputStream convertedStream = AudioSystem.getAudioInputStream(format, audioStream);
                audioStreams.add(convertedStream);
            } else {
                audioStreams.add(audioStream);
            }
        }
        
        // Create a new audio input stream that concatenates all streams
        return new SequenceAudioInputStream(format, audioStreams);
    }
    
    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!dir.delete()) {
            log.warn("Failed to delete temporary file: {}", dir);
        }
    }
    
    private static class SequenceAudioInputStream extends AudioInputStream {
        private final List<AudioInputStream> audioStreams;
        private int currentStreamIndex = 0;
        
        public SequenceAudioInputStream(AudioFormat format, List<AudioInputStream> audioStreams) {
            super(new ByteArrayInputStream(new byte[0]), format, AudioSystem.NOT_SPECIFIED);
            this.audioStreams = new ArrayList<>(audioStreams);
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (currentStreamIndex >= audioStreams.size()) {
                return -1;
            }
            
            int bytesRead = audioStreams.get(currentStreamIndex).read(b, off, len);
            while (bytesRead == -1 && currentStreamIndex < audioStreams.size() - 1) {
                currentStreamIndex++;
                bytesRead = audioStreams.get(currentStreamIndex).read(b, off, len);
            }
            
            return bytesRead;
        }
        
        @Override
        public void close() throws IOException {
            for (AudioInputStream stream : audioStreams) {
                stream.close();
            }
            super.close();
        }
    }
}
