# javafx-mini-player

Companion sample for [Play a local MP4 with JavaFX Media](https://zlu.github.io/) on [zlu.github.io](https://zlu.github.io/).

Opens a local file with JavaFX `Media` / `MediaPlayer` / `MediaView`. Format support is platform-dependent; use a short H.264/AAC MP4 that already plays in the OS media player.

## Requirements

- JDK 23 or newer (JavaFX 25)
- Maven 3.9+

## Run

```sh
mvn javafx:run
```

Use **Open…** and pick an `.mp4`. Play/Pause and the seek slider track `currentTimeProperty()`.

On macOS, prefer H.264/AAC or HEVC/AAC MP4s that already play in QuickTime. JavaFX 21 had a known AVFoundation bug (audio only / black video) fixed in JavaFX 23+; this project uses 25.x.
