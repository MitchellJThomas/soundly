import org.bytedeco.javacv.*;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avutil.*;
import org.bytedeco.ffmpeg.swresample.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.global.swresample.*;

public class AudioProcessor {
    public static void main(String[] args) {
        try {
            String mp3File = "input.mp3";
            List<Double> audioSamples = new ArrayList<>();

            // Initialize FFmpeg
            av_log_set_level(AV_LOG_QUIET);
            AVFormatContext formatContext = new AVFormatContext(null);
            
            if (avformat_open_input(formatContext, mp3File, null, null) < 0) {
                throw new IllegalArgumentException("Could not open file: " + mp3File);
            }

            if (avformat_find_stream_info(formatContext, (AVDictionary) null) < 0) {
                throw new RuntimeException("Could not find stream information");
            }

            // Find the audio stream
            int audioStreamIndex = -1;
            AVCodecContext codecContext = null;
            
            for (int i = 0; i < formatContext.nb_streams(); i++) {
                AVStream stream = formatContext.streams(i);
                AVCodecParameters codecParams = stream.codecpar();
                
                if (codecParams.codec_type() == AVMEDIA_TYPE_AUDIO) {
                    audioStreamIndex = i;
                    AVCodec codec = avcodec_find_decoder(codecParams.codec_id());
                    codecContext = avcodec_alloc_context3(codec);
                    avcodec_parameters_to_context(codecContext, codecParams);
                    avcodec_open2(codecContext, codec, (AVDictionary) null);
                    break;
                }
            }

            if (audioStreamIndex == -1) {
                throw new RuntimeException("No audio stream found");
            }

            // Read audio packets
            AVPacket packet = av_packet_alloc();
            AVFrame frame = av_frame_alloc();
            
            while (av_read_frame(formatContext, packet) >= 0) {
                if (packet.stream_index() == audioStreamIndex) {
                    int ret = avcodec_send_packet(codecContext, packet);
                    
                    while (ret >= 0) {
                        ret = avcodec_receive_frame(codecContext, frame);
                        if (ret == AVERROR_EOF || ret == AVERROR_EAGAIN())
                            break;
                            
                        // Convert samples to doubles
                        for (int i = 0; i < frame.nb_samples(); i++) {
                            // Assuming 16-bit audio, adjust if different
                            short sample = frame.data(0).getShort(i * 2);
                            audioSamples.add(sample / 32768.0);
                        }
                    }
                }
                av_packet_unref(packet);
            }

            // Process audio with moving average
            int windowSize = 100;
            List<Double> processedSamples = movingAverage(audioSamples, windowSize);

            // Print statistics
            System.out.println("Original samples: " + audioSamples.size());
            System.out.println("Processed samples: " + processedSamples.size());

            // Clean up
            av_frame_free(frame);
            av_packet_free(packet);
            avcodec_free_context(codecContext);
            avformat_close_input(formatContext);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Double> movingAverage(List<Double> input, int windowSize) {
        return IntStream.range(0, input.size() - windowSize + 1)
                .mapToDouble(i -> input.subList(i, i + windowSize)
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0))
                .boxed()
                .collect(Collectors.toList());
    }

    public static void recordFromMicrophone(String outputFile, int durationSeconds) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("default")) {
            // Set up audio grabber
            grabber.setFormat("avfoundation"); // Use avfoundation for macOS
            grabber.setSampleRate(44100);
            grabber.setAudioChannels(2);
            grabber.start();

            // Set up recorder
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 2);
            recorder.setFormat("mp3");
            recorder.setSampleRate(44100);
            recorder.setAudioChannels(2);
            recorder.setAudioQuality(0); // Highest quality
            recorder.setAudioCodec(AV_CODEC_ID_MP3);
            recorder.start();

            // Record for specified duration
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < (durationSeconds * 1000)) {
                Frame frame = grabber.grab();
                if (frame != null && frame.samples != null) {
                    recorder.record(frame);
                }
            }

            // Clean up
            recorder.stop();
            recorder.release();
            
        } catch (Exception e) {
            System.err.println("Error recording audio: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


