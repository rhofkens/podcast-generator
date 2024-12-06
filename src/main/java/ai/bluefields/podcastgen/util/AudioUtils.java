package ai.bluefields.podcastgen.util;

import javazoom.jl.decoder.*;
import javazoom.jl.converter.Converter;
import javazoom.jl.converter.WaveFileObuffer;
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
        List<byte[]> pcmDataList = new ArrayList<>();
        AudioFormat format = null;
        
        // Step 1: Decode all MP3 files to PCM
        for (Path mp3File : mp3Files) {
            try (FileInputStream fis = new FileInputStream(mp3File.toFile())) {
                Bitstream bitstream = new Bitstream(fis);
                Decoder decoder = new Decoder();
                
                // Get audio format from first frame
                Header frameHeader = bitstream.readFrame();
                if (format == null) {
                    format = new AudioFormat(
                        frameHeader.frequency(),
                        16, // sample size in bits
                        frameHeader.mode() == Header.SINGLE_CHANNEL ? 1 : 2, // channels
                        true, // signed
                        false // little endian
                    );
                }
                
                // Decode frames
                ByteArrayOutputStream pcmBuffer = new ByteArrayOutputStream();
                while (frameHeader != null) {
                    SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
                    writeShortArrayToStream(output.getBuffer(), pcmBuffer);
                    bitstream.closeFrame();
                    frameHeader = bitstream.readFrame();
                }
                
                pcmDataList.add(pcmBuffer.toByteArray());
            }
        }
        
        // Step 2: Concatenate PCM data
        ByteArrayOutputStream concatenatedPCM = new ByteArrayOutputStream();
        for (byte[] pcmData : pcmDataList) {
            concatenatedPCM.write(pcmData);
        }
        
        // Step 3: Convert back to MP3
        ByteArrayOutputStream mp3Output = new ByteArrayOutputStream();
        AudioInputStream pcmStream = new AudioInputStream(
            new ByteArrayInputStream(concatenatedPCM.toByteArray()),
            format,
            concatenatedPCM.size() / format.getFrameSize()
        );
        
        // Use LAME or other MP3 encoder
        AudioSystem.write(pcmStream, AudioFileFormat.Type.WAVE, mp3Output);
        
        return mp3Output.toByteArray();
    }
    
    private static void writeShortArrayToStream(short[] samples, OutputStream out) throws IOException {
        byte[] buffer = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            buffer[i * 2] = (byte) (samples[i] & 0xff);
            buffer[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xff);
        }
        out.write(buffer);
    }
}
