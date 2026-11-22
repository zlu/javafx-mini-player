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

## Series map

| Tag | Phase | Post |
| --- | --- | --- |
| `part-1` | JavaFX | Stage, Scene, Application |
| `part-2` | JavaFX | Layout and controls (planned) |
| `part-3` | JavaFX | State and events (planned) |
| `part-4` | JavaFX | JavaFX Media (planned) |
| `part-5` | Beyond | VLC architecture (planned) |
| `part-6` | Beyond | libVLC via vlcj (planned) |
| `part-7` | Beyond | Playlist / packaging (optional) |

Draft posts live in `zlu.github.io/_drafts/2026-11-22-javafx-ui-to-libvlc-part-*.md`.

```sh
git checkout part-1
mvn javafx:run
```
