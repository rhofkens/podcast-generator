package ai.bluefields.podcastgen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for audio processing operations.
 * Handles MP3 concatenation, format conversion, and audio quality validation.
 */
public class AudioUtils {
    private static final Logger log = LoggerFactory.getLogger(AudioUtils.class);
    
    // Constants for audio processing limits and configuration
    private static final long MAX_TOTAL_SIZE = 500 * 1024 * 1024; // 500MB
    private static final int MAX_RETRIES = 3;
    private static final int BUFFER_SIZE = 8192; // 8KB buffer for audio processing
    
    // Thread pool for parallel processing
    private static final ExecutorService executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    /**
     * Validates and normalizes audio formats to ensure compatibility.
     * Supports standard audio formats commonly used in podcast production.
     */
    public static class AudioValidator {
        private static final Logger log = LoggerFactory.getLogger(AudioValidator.class);
        
        private static final int[] SUPPORTED_SAMPLE_RATES = {44100, 48000};
        private static final int[] SUPPORTED_BIT_RATES = {128000, 192000, 256000, 320000};
        
        /**
         * Validates an audio format against supported specifications.
         * 
         * @param format AudioFormat to validate
         * @throws IllegalArgumentException if format is not supported
         */
        public static void validateFormat(AudioFormat format) throws IllegalArgumentException {
            log.debug("Validating audio format: sampleRate={}, channels={}, encoding={}", 
                format.getSampleRate(), format.getChannels(), format.getEncoding());
            
            boolean validSampleRate = false;
            for (int rate : SUPPORTED_SAMPLE_RATES) {
                if (Math.abs(format.getSampleRate() - rate) < 0.1) {
                    validSampleRate = true;
                    log.debug("Found matching sample rate: {}", rate);
                    break;
                }
            }
            
            if (!validSampleRate) {
                log.error("Unsupported sample rate: {}", format.getSampleRate());
                throw new IllegalArgumentException("Unsupported sample rate: " + format.getSampleRate());
            }
            
            if (format.getChannels() != 1 && format.getChannels() != 2) {
                log.error("Unsupported channel count: {}", format.getChannels());
                throw new IllegalArgumentException("Unsupported channel count: " + format.getChannels());
            }
            
            log.debug("Audio format validation successful");
        }
        
        public static EncodingAttributes getNormalizedEncodingAttributes(AudioFormat inputFormat) {
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setSamplingRate(findClosestValue(inputFormat.getSampleRate(), SUPPORTED_SAMPLE_RATES));
            audio.setBitRate(findClosestValue(192000, SUPPORTED_BIT_RATES));
            audio.setChannels(Math.min(2, inputFormat.getChannels()));
            
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("mp3");
            attrs.setAudioAttributes(audio);
            return attrs;
        }
        
        private static int findClosestValue(float value, int[] supportedValues) {
            int closest = supportedValues[0];
            for (int supported : supportedValues) {
                if (Math.abs(value - supported) < Math.abs(value - closest)) {
                    closest = supported;
                }
            }
            return closest;
        }
    }

    /**
     * Tracks and reports audio processing metrics for performance monitoring.
     */
    public static class AudioProcessingMetrics {
        private static final Logger log = LoggerFactory.getLogger(AudioProcessingMetrics.class);
        
        private final long startTime;
        private final Map<String, Long> stageDurations = new HashMap<>();
        private String currentStage;
        private long currentStageStart;
        
        public AudioProcessingMetrics() {
            this.startTime = System.currentTimeMillis();
            log.debug("Started new audio processing metrics tracking");
        }
        
        /**
         * Starts timing a new processing stage.
         */
        public void startStage(String stage) {
            if (currentStage != null) {
                endStage();
            }
            currentStage = stage;
            currentStageStart = System.currentTimeMillis();
            log.debug("Started timing stage: {}", stage);
        }
        
        /**
         * Ends timing for the current stage and records duration.
         */
        public void endStage() {
            if (currentStage != null) {
                long duration = System.currentTimeMillis() - currentStageStart;
                stageDurations.put(currentStage, duration);
                log.debug("Ended stage '{}': {}ms", currentStage, duration);
                currentStage = null;
            }
        }
        
        /**
         * Logs complete metrics for the audio processing operation.
         */
        public void logMetrics() {
            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("Audio processing completed in {}ms", totalDuration);
            
            // Log individual stage durations
            stageDurations.forEach((stage, duration) -> {
                double percentage = (duration * 100.0) / totalDuration;
                log.info("Stage '{}' took {}ms ({:.1f}%)", stage, duration, percentage);
            });
            
            // Log performance summary
            log.info("Performance summary:");
            log.info("- Total stages: {}", stageDurations.size());
            log.info("- Average stage duration: {}ms", 
                stageDurations.values().stream().mapToLong(l -> l).average().orElse(0));
            log.info("- Longest stage: {}", 
                stageDurations.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> String.format("%s (%dms)", e.getKey(), e.getValue()))
                    .orElse("N/A"));
        }
    }

    /**
     * Checks audio quality metrics to ensure output meets minimum standards.
     */
    public static class AudioQualityChecker {
        private static final Logger log = LoggerFactory.getLogger(AudioQualityChecker.class);
        
        private static final double MIN_RMS_THRESHOLD = 0.01;
        private static final double MAX_RMS_THRESHOLD = 0.9;
        
        /**
         * Analyzes audio file for quality metrics including RMS levels.
         */
        public static void checkAudioQuality(File audioFile) throws Exception {
            log.debug("Starting audio quality check for: {}", audioFile.getName());
            
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile)) {
                log.debug("Audio format: {}", ais.getFormat());
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                double sumSquares = 0;
                long samples = 0;
                double maxLevel = 0;
                
                while ((bytesRead = ais.read(buffer)) != -1) {
                    for (int i = 0; i < bytesRead; i += 2) {
                        short sample = (short) ((buffer[i+1] << 8) | (buffer[i] & 0xFF));
                        double normalized = sample / 32768.0;
                        sumSquares += normalized * normalized;
                        maxLevel = Math.max(maxLevel, Math.abs(normalized));
                        samples++;
                    }
                }
                
                double rms = Math.sqrt(sumSquares / samples);
                log.debug("Audio analysis results:");
                log.debug("- RMS level: {}", rms);
                log.debug("- Peak level: {}", maxLevel);
                log.debug("- Total samples: {}", samples);
                
                if (rms < MIN_RMS_THRESHOLD) {
                    log.error("Audio level too low: {}", rms);
                    throw new Exception("Audio level too low: " + rms);
                }
                if (rms > MAX_RMS_THRESHOLD) {
                    log.error("Audio level too high (possible clipping): {}", rms);
                    throw new Exception("Audio level too high (possible clipping): " + rms);
                }
                
                log.info("Audio quality check passed: RMS={}, Peak={}", rms, maxLevel);
            }
        }
    }

    /**
     * Manages audio processing resources and ensures proper cleanup.
     * Handles temporary files and audio streams in a safe manner.
     */
    public static class AudioResourceManager implements AutoCloseable {
        private static final Logger log = LoggerFactory.getLogger(AudioResourceManager.class);
        
        private final List<File> temporaryFiles = new ArrayList<>();
        private final List<AudioInputStream> openStreams = new ArrayList<>();
        
        /**
         * Creates a temporary file and tracks it for cleanup.
         * 
         * @param prefix The prefix string for the temporary file
         * @param suffix The suffix string for the temporary file
         * @return The created temporary file
         * @throws IOException if file creation fails
         */
        public File createTempFile(String prefix, String suffix) throws IOException {
            log.debug("Creating temporary file with prefix='{}', suffix='{}'", prefix, suffix);
            File temp = File.createTempFile(prefix, suffix);
            temporaryFiles.add(temp);
            log.debug("Created temporary file: {}", temp.getAbsolutePath());
            return temp;
        }
        
        /**
         * Opens an audio input stream and tracks it for cleanup.
         * 
         * @param file The audio file to open
         * @return AudioInputStream for the file
         * @throws Exception if stream creation fails
         */
        public AudioInputStream openAudioStream(File file) throws Exception {
            log.debug("Opening audio stream for file: {}", file.getAbsolutePath());
            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(file);
                openStreams.add(stream);
                log.debug("Successfully opened audio stream: format={}", stream.getFormat());
                return stream;
            } catch (Exception e) {
                log.error("Failed to open audio stream for file {}: {}", file.getName(), e.getMessage());
                throw e;
            }
        }
        
        /**
         * Closes all tracked resources and cleans up temporary files.
         * Implements AutoCloseable for use in try-with-resources.
         */
        @Override
        public void close() {
            log.debug("Closing AudioResourceManager - cleaning up {} streams and {} temporary files", 
                openStreams.size(), temporaryFiles.size());
            
            // Close audio streams
            for (AudioInputStream stream : openStreams) {
                try {
                    stream.close();
                    log.trace("Closed audio stream successfully");
                } catch (IOException e) {
                    log.warn("Failed to close audio stream: {}", e.getMessage());
                }
            }
            openStreams.clear();
            
            // Delete temporary files
            for (File file : temporaryFiles) {
                if (file.exists()) {
                    if (file.delete()) {
                        log.trace("Deleted temporary file: {}", file.getAbsolutePath());
                    } else {
                        log.warn("Failed to delete temporary file: {}", file.getAbsolutePath());
                        // Schedule for deletion on JVM exit as fallback
                        file.deleteOnExit();
                    }
                }
            }
            temporaryFiles.clear();
            
            log.debug("AudioResourceManager cleanup completed");
        }
    }
    
    /**
     * Interface for tracking audio processing progress.
     */
    public interface AudioProcessingProgressListener {
        /**
         * Called to update processing progress.
         * 
         * @param stage Current processing stage description
         * @param progress Progress percentage (0-100)
         */
        void onProgress(String stage, int progress);
    }
    
    /**
     * Configuration class for audio format settings.
     * Provides standardized audio format configurations for consistent processing.
     */
    public static class AudioFormatConfig {
        private static final Logger log = LoggerFactory.getLogger(AudioFormatConfig.class);
        
        private final int sampleRate;
        private final int channels;
        private final int bitRate;
        private final String codec;
        
        /**
         * Default high-quality audio configuration.
         * CD quality stereo at 192kbps.
         */
        public static final AudioFormatConfig DEFAULT = new AudioFormatConfig(
            44100,  // CD quality
            2,      // Stereo
            192000, // 192kbps
            "libmp3lame"
        );
        
        /**
         * Creates a new audio format configuration.
         * 
         * @param sampleRate Sample rate in Hz
         * @param channels Number of audio channels
         * @param bitRate Bit rate in bits per second
         * @param codec Audio codec name
         */
        public AudioFormatConfig(int sampleRate, int channels, int bitRate, String codec) {
            log.debug("Creating AudioFormatConfig: sampleRate={}, channels={}, bitRate={}, codec={}", 
                sampleRate, channels, bitRate, codec);
            
            this.sampleRate = sampleRate;
            this.channels = channels;
            this.bitRate = bitRate;
            this.codec = codec;
        }
        
        /**
         * Converts configuration to JAVE2 AudioAttributes.
         * 
         * @return AudioAttributes configured according to this format
         */
        public AudioAttributes toAudioAttributes() {
            log.debug("Converting AudioFormatConfig to AudioAttributes");
            
            AudioAttributes attrs = new AudioAttributes();
            attrs.setCodec(codec);
            attrs.setBitRate(bitRate);
            attrs.setChannels(channels);
            attrs.setSamplingRate(sampleRate);
            
            log.debug("Created AudioAttributes: {}", attrs);
            return attrs;
        }
        
        /**
         * Creates a custom format configuration with validation.
         * 
         * @param sampleRate Sample rate in Hz
         * @param channels Number of channels
         * @param bitRate Bit rate in bps
         * @param codec Codec name
         * @return Validated AudioFormatConfig
         * @throws IllegalArgumentException if parameters are invalid
         */
        public static AudioFormatConfig custom(int sampleRate, int channels, int bitRate, String codec) {
            log.debug("Creating custom AudioFormatConfig");
            
            // Validate parameters
            if (sampleRate <= 0) {
                log.error("Invalid sample rate: {}", sampleRate);
                throw new IllegalArgumentException("Sample rate must be positive");
            }
            if (channels <= 0) {
                log.error("Invalid channel count: {}", channels);
                throw new IllegalArgumentException("Channel count must be positive");
            }
            if (bitRate <= 0) {
                log.error("Invalid bit rate: {}", bitRate);
                throw new IllegalArgumentException("Bit rate must be positive");
            }
            if (codec == null || codec.trim().isEmpty()) {
                log.error("Invalid codec: {}", codec);
                throw new IllegalArgumentException("Codec cannot be null or empty");
            }
            
            return new AudioFormatConfig(sampleRate, channels, bitRate, codec);
        }
        
        @Override
        public String toString() {
            return String.format("AudioFormatConfig[%dHz, %dch, %dkbps, %s]", 
                sampleRate, channels, bitRate/1000, codec);
        }
    }
    
    /**
     * Concatenates multiple MP3 files into a single MP3 file.
     * Process: MP3 -> WAV -> Concatenate WAVs -> Convert back to MP3
     *
     * @param mp3Files List of paths to MP3 files to concatenate
     * @return byte array containing the concatenated MP3 data
     * @throws Exception if any processing step fails
     */
    public static byte[] concatenateMP3Files(List<Path> mp3Files) throws Exception {
        return concatenateMP3Files(mp3Files, null);
    }
    
    /**
     * Concatenates multiple MP3 files with progress tracking.
     *
     * @param mp3Files List of paths to MP3 files to concatenate
     * @param progressListener Optional listener for progress updates
     * @return byte array containing the concatenated MP3 data
     * @throws Exception if any processing step fails
     */
    public static byte[] concatenateMP3Files(List<Path> mp3Files, 
            AudioProcessingProgressListener progressListener) throws Exception {
        log.debug("Starting MP3 concatenation for {} files", mp3Files.size());
        
        // Input validation
        if (mp3Files == null || mp3Files.isEmpty()) {
            log.error("No MP3 files provided for concatenation");
            throw new IllegalArgumentException("No MP3 files provided");
        }
        
        // Check total size
        long totalSize = mp3Files.stream()
            .mapToLong(path -> path.toFile().length())
            .sum();
        
        if (totalSize > MAX_TOTAL_SIZE) {
            throw new IllegalArgumentException("Total audio size exceeds maximum allowed");
        }

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "podcast-concat-" + System.currentTimeMillis());
        tempDir.mkdirs();
        
        try {
            // Create a list to store converted WAV files
            List<File> wavFiles = new ArrayList<>();
            
            // First convert each MP3 to WAV using JAVE2
            for (Path mp3Path : mp3Files) {
                log.debug("Converting MP3 file to WAV: {}", mp3Path.getFileName());
                
                File inputMp3 = mp3Path.toFile();
                File outputWav = new File(tempDir, "temp_" + mp3Path.getFileName().toString().replace(".mp3", ".wav"));
                
                AudioAttributes audioAttrs = new AudioAttributes();
                audioAttrs.setCodec("pcm_s16le");  // Standard PCM codec
                audioAttrs.setChannels(2);
                audioAttrs.setSamplingRate(44100);
                
                EncodingAttributes attrs = new EncodingAttributes();
                attrs.setOutputFormat("wav");
                attrs.setAudioAttributes(audioAttrs);
                
                Encoder encoder = new Encoder();
                encoder.encode(new MultimediaObject(inputMp3), outputWav, attrs);
                
                wavFiles.add(outputWav);
            }
            
            // Concatenate WAV files
            File concatenatedWav = new File(tempDir, "concatenated.wav");
            AudioInputStream concatenatedStream = null;
            
            for (File wavFile : wavFiles) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
                
                if (concatenatedStream == null) {
                    concatenatedStream = audioInputStream;
                } else {
                    AudioInputStream appendedStream = new AudioInputStream(
                        new SequenceInputStream(concatenatedStream, audioInputStream),
                        concatenatedStream.getFormat(),
                        concatenatedStream.getFrameLength() + audioInputStream.getFrameLength()
                    );
                    concatenatedStream = appendedStream;
                }
            }
            
            // Write concatenated WAV
            AudioSystem.write(concatenatedStream, AudioFileFormat.Type.WAVE, concatenatedWav);
            concatenatedStream.close();
            
            // Convert final WAV to MP3
            File outputMp3 = new File(tempDir, "final.mp3");
            
            AudioAttributes finalAudioAttrs = new AudioAttributes();
            finalAudioAttrs.setCodec("libmp3lame");
            finalAudioAttrs.setBitRate(192000);
            finalAudioAttrs.setChannels(2);
            finalAudioAttrs.setSamplingRate(44100);
            
            EncodingAttributes finalAttrs = new EncodingAttributes();
            finalAttrs.setOutputFormat("mp3");
            finalAttrs.setAudioAttributes(finalAudioAttrs);
            
            Encoder finalEncoder = new Encoder();
            finalEncoder.encode(new MultimediaObject(concatenatedWav), outputMp3, finalAttrs);
            
            // Verify output
            if (!outputMp3.exists() || outputMp3.length() == 0) {
                throw new IOException("MP3 encoding failed - output file is empty or missing");
            }
            
            log.debug("Generated MP3 file size: {} bytes", outputMp3.length());
            
            // Read final MP3 file
            byte[] mp3Data = new byte[(int) outputMp3.length()];
            try (FileInputStream fis = new FileInputStream(outputMp3)) {
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
    
}
