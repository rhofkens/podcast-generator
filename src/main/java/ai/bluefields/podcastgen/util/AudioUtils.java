package ai.bluefields.podcastgen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

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
            // Create temporary output files
            File wavFile = new File(tempDir, "output.wav");
            File mp3File = new File(tempDir, "final.mp3");
            
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
                        44100, // Fixed sample rate for better compatibility
                        16,    // Standard bit depth
                        2,     // Stereo
                        4,     // Frame size (2 channels * 2 bytes per sample)
                        44100, // Frame rate same as sample rate
                        false  // Little endian
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
            
            // Write the concatenated audio to WAV file
            AudioSystem.write(
                concatenatedStream,
                AudioFileFormat.Type.WAVE,
                wavFile
            );

            // Convert WAV to MP3 using JAVE2
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setBitRate(192000);        // 192 kbps
            audio.setChannels(2);            // Stereo
            audio.setSamplingRate(44100);    // CD quality
            audio.setQuality(3);             // Good quality setting (1-5)
            audio.setVolume(100);            // Keep original volume

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("mp3");
            attrs.setAudioAttributes(audio);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(wavFile), mp3File, attrs);
            
            // Verify the output file exists and has content
            if (!mp3File.exists() || mp3File.length() == 0) {
                throw new IOException("MP3 encoding failed - output file is empty or missing");
            }
            
            log.debug("Generated MP3 file size: {} bytes", mp3File.length());
            
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
