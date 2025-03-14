# Soundly 🎵

> A Java-based audio processing application for recording and analyzing audio signals.

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/downloads/#java21)
[![License](https://img.shields.io/badge/License-CC%20BY%204.0-lightgrey.svg)](http://creativecommons.org/licenses/by/4.0/)

## Features

- 🎤 Record audio from your microphone in MP3 format
- 📊 Process audio signals using moving average algorithm
- 🔊 High-quality audio processing (44.1kHz, stereo)
- 🎯 Real-time audio sample processing
- 📈 Statistical analysis of audio data

## Installation

1. Ensure you have Java 21 installed:
```bash
java --version
```

2. Clone this repository:
```bash
git clone https://github.com/yourusername/soundly.git
cd soundly
```

3. Install dependencies using Maven:
```bash
mvn clean install
```

## Usage

### Recording Audio

```java
// Record 10 seconds of audio to test.mp3
AudioProcessor.recordFromMicrophone("test.mp3", 10);
```

### Processing Audio Files

```java
// Process an audio file with moving average
List<Double> processedSamples = AudioProcessor.movingAverage(audioSamples, 100);
```

## Technical Details

The project uses JavaCV/FFmpeg for audio processing with the following specifications:

- Sample Rate: 44.1kHz
- Channels: Stereo (2)
- Audio Format: MP3
- Processing: Moving average algorithm for noise reduction

## Dependencies

- JavaCV 1.5.10
- FFmpeg Platform 1.5.10

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Creative Commons Attribution 4.0 International License - see the [LICENSE](LICENSE) file for details.

For more information, visit [Creative Commons BY 4.0](http://creativecommons.org/licenses/by/4.0/).

## Acknowledgments

- [JavaCV](https://github.com/bytedeco/javacv) for providing Java interface to FFmpeg
- [FFmpeg](https://ffmpeg.org/) for audio processing capabilities
