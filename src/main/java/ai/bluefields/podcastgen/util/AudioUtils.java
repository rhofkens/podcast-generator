package ai.bluefields.podcastgen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileFormat;

public class AudioUtils {
    private static final Logger log = LoggerFactory.getLogger(AudioUtils.class);
    
    public static byte[] concatenateMP3Files(List<Path> mp3Files) throws Exception {
        if (mp3Files == null || mp3Files.isEmpty()) {
            throw new IllegalArgumentException("No MP3 files provided");
        }

        // Create temporary directory for processing
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "podcast-concat-" + System.currentTimeMillis());
        tempDir.mkdirs();
        
        try {
            // Create temporary output file
            File outputFile = new File(tempDir, "output.mp3");
            
            // First, collect all AudioInputStreams with consistent format
            List<AudioInputStream> audioStreams = new ArrayList<>();
            AudioFormat targetFormat = null;
            
            // Open all input streams and convert to consistent format
            for (Path mp3Path : mp3Files) {
                log.debug("Opening MP3 file: {}", mp3Path.getFileName());
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(mp3Path.toFile());
                
                if (targetFormat == null) {
                    // Use the format of the first file as target format
                    AudioFormat baseFormat = audioStream.getFormat();
                    targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                    );
                }
                
                // Convert to target format if needed
                if (!audioStream.getFormat().matches(targetFormat)) {
                    audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                }
                
                audioStreams.add(audioStream);
            }
            
            // Create a sequence input stream that concatenates all streams
            AudioInputStream concatenatedStream = new SequenceAudioInputStream(
                targetFormat,
                audioStreams
            );
            
            // Write the concatenated audio to the output file
            AudioSystem.write(
                concatenatedStream,
                AudioFileFormat.Type.WAVE,
                outputFile
            );
            
            // Convert the WAV back to MP3 using AudioSystem
            AudioInputStream wavStream = AudioSystem.getAudioInputStream(outputFile);
            File mp3File = new File(tempDir, "final.mp3");
            
            // Get supported audio file types
            AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes(wavStream);
            AudioFileFormat.Type mp3Type = null;
            
            // Find MP3 type from available types
            for (AudioFileFormat.Type type : types) {
                if (type.getExtension().equalsIgnoreCase("mp3")) {
                    mp3Type = type;
                    break;
                }
            }
            
            if (mp3Type == null) {
                throw new IllegalStateException("MP3 encoding is not supported. Make sure mp3spi is properly included.");
            }
            
            AudioSystem.write(
                wavStream,
                mp3Type,
                mp3File
            );
            
            // Read the final MP3 file into byte array
            byte[] mp3Data = new byte[(int) mp3File.length()];
            try (FileInputStream fis = new FileInputStream(mp3File)) {
                int bytesRead = fis.read(mp3Data);
                if (bytesRead != mp3Data.length) {
                    throw new IOException("Failed to read entire MP3 file");
                }
            }
            
            return mp3Data;
            
        } finally {
            // Clean up temporary directory and all files
            deleteDirectory(tempDir);
        }
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
        private final long totalLength;
        
        public SequenceAudioInputStream(AudioFormat format, List<AudioInputStream> audioStreams) {
            super(new ByteArrayInputStream(new byte[0]), format, 0);
            this.audioStreams = new ArrayList<>(audioStreams);
            
            // Calculate total frame length
            long length = 0;
            for (AudioInputStream stream : audioStreams) {
                if (stream.getFrameLength() != AudioSystem.NOT_SPECIFIED) {
                    length += stream.getFrameLength();
                }
            }
            this.totalLength = length;
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (currentStreamIndex >= audioStreams.size()) {
                return -1;
            }
            
            int bytesRead = audioStreams.get(currentStreamIndex).read(b, off, len);
            
            // If we've reached the end of the current stream, move to the next one
            while (bytesRead == -1 && currentStreamIndex < audioStreams.size() - 1) {
                currentStreamIndex++;
                bytesRead = audioStreams.get(currentStreamIndex).read(b, off, len);
            }
            
            return bytesRead;
        }
        
        @Override
        public long getFrameLength() {
            return totalLength;
        }
        
        @Override
        public void close() throws IOException {
            // Close all streams
            for (AudioInputStream stream : audioStreams) {
                stream.close();
            }
            super.close();
        }
    }
}
