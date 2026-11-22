# javafx-mini-player

Companion sample for the JavaFX / libVLC posts on [zlu.github.io](https://zlu.github.io/).

Same window chrome (Open, Play/Pause, seek slider). Two engines:

| Main class | Engine | Run |
| --- | --- | --- |
| `VlcMiniPlayerApp` (default) | libVLC via [vlcj](https://github.com/caprica/vlcj) + [vlcj-javafx](https://github.com/caprica/vlcj-javafx) | `mvn javafx:run` |
| `MiniPlayerApp` | JavaFX `Media` / `MediaPlayer` / `MediaView` | `mvn javafx:run -Djavafx.mainClass=io.github.zlu.javafxplayer.MiniPlayerApp` |

## Requirements

- JDK 21+ (sample uses JavaFX 25)
- Maven 3.9+
- **For the libVLC app:** a system VLC install that provides `libvlc` (on macOS, `/Applications/VLC.app` is enough)

## Run (libVLC)

```sh
mvn javafx:run
```

Optional path (skips the file dialog):

```sh
mvn javafx:run -Djavafx.args="$PWD/sample-h264.mp4"
```

Use **Open…** for other files. libVLC handles a much wider range of containers/codecs than JavaFX Media.

## Run (JavaFX Media only)

```sh
mvn javafx:run -Djavafx.mainClass=io.github.zlu.javafxplayer.MiniPlayerApp
```

Prefer a short H.264/AAC MP4. Format support is platform-dependent.

## Notes

- Video in the libVLC app is drawn into a JavaFX `ImageView` through `ImageViewVideoSurface` (PixelBuffer). Heavyweight AWT embed does not work on modern macOS Java.
- Shipping an app that bundles VLC natives has LGPL obligations; this sample expects a user-installed VLC.
