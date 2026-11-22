# javafx-mini-player

Companion code for the blog series **JavaFX UI → libVLC** on [zlu.github.io](https://zlu.github.io/).

Phase 1 stays inside JavaFX (UI first, then `MediaPlayer`). Phase 2 swaps the engine for libVLC via `vlcj` — that part is beyond pure JavaFX.

## Requirements

- JDK 21 or newer
- Maven 3.9+

## Part I — Hello window

```sh
mvn javafx:run
```

You should see a 640×360 window titled **Mini Player** with a centered label.

## Tags (once published)

| Tag | Post |
| --- | --- |
| `part-1` | Stage, Scene, Application |
| `part-2` | Layout and controls (planned) |
| `part-3` | State and events (planned) |
| `part-4` | JavaFX Media (planned) |

```sh
git checkout part-1
mvn javafx:run
```
