package io.github.zlu.javafxplayer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Same chrome as {@link MiniPlayerApp}, playback through libVLC via vlcj.
 *
 * <p>Requires a system VLC install (libvlc). On macOS, the stock
 * {@code /Applications/VLC.app} is enough for native discovery.
 *
 * <p>Companion post: JavaFX chrome on libVLC (zlu.github.io).
 */
public class VlcMiniPlayerApp extends Application {

    private MediaPlayerFactory factory;
    private EmbeddedMediaPlayer mediaPlayer;

    private final ImageView videoView = new ImageView();
    private final Label status = new Label("No media");
    private final Label timeLabel = new Label("00:00 / 00:00");
    private final Button playPause = new Button("Play");
    private final Slider seek = new Slider(0, 100, 0);

    /** True while the user is dragging the seek thumb; blocks clock → slider writes. */
    private final AtomicBoolean seeking = new AtomicBoolean();

    private long lengthMs;

    @Override
    public void init() {
        factory = new MediaPlayerFactory();
        mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
        videoView.setPreserveRatio(true);
        videoView.setSmooth(true);
        mediaPlayer.videoSurface().set(new ImageViewVideoSurface(videoView));
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void lengthChanged(MediaPlayer player, long newLength) {
                Platform.runLater(() -> {
                    lengthMs = Math.max(0, newLength);
                    seek.setMax(lengthMs > 0 ? lengthMs : 100);
                    seek.setDisable(false);
                    playPause.setDisable(false);
                    timeLabel.setText(format(0) + " / " + format(lengthMs));
                });
            }

            @Override
            public void timeChanged(MediaPlayer player, long newTime) {
                Platform.runLater(() -> {
                    if (!seeking.get()) {
                        seek.setValue(newTime);
                    }
                    timeLabel.setText(format(newTime) + " / " + format(lengthMs));
                });
            }

            @Override
            public void playing(MediaPlayer player) {
                Platform.runLater(() -> playPause.setText("Pause"));
            }

            @Override
            public void paused(MediaPlayer player) {
                Platform.runLater(() -> playPause.setText("Play"));
            }

            @Override
            public void stopped(MediaPlayer player) {
                Platform.runLater(() -> playPause.setText("Play"));
            }

            @Override
            public void finished(MediaPlayer player) {
                Platform.runLater(() -> {
                    playPause.setText("Play");
                    seek.setValue(0);
                    timeLabel.setText(format(0) + " / " + format(lengthMs));
                });
            }

            @Override
            public void error(MediaPlayer player) {
                Platform.runLater(() -> status.setText("Player error (see console / VLC logs)"));
            }
        });
    }

    @Override
    public void start(Stage stage) {
        StackPane center = new StackPane(videoView);
        center.setStyle("-fx-background-color: #1a1a1a;");
        videoView.fitWidthProperty().bind(center.widthProperty());
        videoView.fitHeightProperty().bind(center.heightProperty());

        Button open = new Button("Open…");
        open.setOnAction(e -> openFile(stage));

        playPause.setDisable(true);
        playPause.setOnAction(e -> togglePlayPause());

        seek.setDisable(true);
        seek.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(seek, Priority.ALWAYS);

        seek.valueChangingProperty().addListener((obs, was, changing) -> {
            seeking.set(changing);
            if (!changing && mediaPlayer != null) {
                mediaPlayer.controls().setTime((long) seek.getValue());
            }
        });
        seek.setOnMouseReleased(e -> {
            if (mediaPlayer != null && !seek.isValueChanging()) {
                mediaPlayer.controls().setTime((long) seek.getValue());
            }
        });

        HBox top = new HBox(8, open, status);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(8));

        HBox bottom = new HBox(8, playPause, seek, timeLabel);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(center);
        root.setBottom(bottom);

        stage.setTitle("Mini Player (libVLC)");
        stage.setScene(new Scene(root, 800, 500));
        stage.setOnCloseRequest(e -> releaseNative());
        stage.show();

        // Optional: mvn javafx:run -Djavafx.args=/path/to/file.mp4
        var params = getParameters().getRaw();
        if (!params.isEmpty()) {
            File file = new File(params.get(0));
            if (file.isFile()) {
                load(file);
            } else {
                status.setText("Not a file: " + params.get(0));
            }
        }
    }

    private void openFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open media");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Media",
                        "*.mp4", "*.mkv", "*.avi", "*.mov", "*.webm",
                        "*.m4v", "*.mp3", "*.flac", "*.wav", "*.aiff"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        load(file);
    }

    private void load(File file) {
        status.setText(file.getName());
        lengthMs = 0;
        seek.setValue(0);
        seek.setDisable(true);
        playPause.setDisable(true);
        playPause.setText("Play");
        timeLabel.setText("00:00 / 00:00");

        // media().play starts asynchronously; length/time arrive via events.
        boolean ok = mediaPlayer.media().play(file.getAbsolutePath());
        if (!ok) {
            status.setText("Could not start: " + file.getName());
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer.status().isPlaying()) {
            mediaPlayer.controls().pause();
        } else {
            mediaPlayer.controls().play();
        }
    }

    private void releaseNative() {
        if (mediaPlayer != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (factory != null) {
            factory.release();
            factory = null;
        }
    }

    @Override
    public void stop() {
        releaseNative();
    }

    private static String format(long millis) {
        if (millis < 0) {
            return "00:00";
        }
        int seconds = (int) (millis / 1000);
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
