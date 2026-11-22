package io.github.zlu.javafxplayer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

/**
 * Desktop mini-player: open a local file and play it with JavaFX Media.
 *
 * <p>Roles of the three media types:
 * <ul>
 *   <li>{@link Media}: source URI ({@code file:} URL here)</li>
 *   <li>{@link MediaPlayer}: play / pause / seek and time properties</li>
 *   <li>{@link MediaView}: scene-graph node that draws decoded frames</li>
 * </ul>
 *
 * <p>Format support is platform-dependent. Prefer a short H.264/AAC MP4 that
 * already plays in the OS media player.
 *
 * <p>Companion post: Play a local MP4 with JavaFX Media (zlu.github.io).
 */
public class MiniPlayerApp extends Application {

    private MediaPlayer mediaPlayer;
    private final MediaView mediaView = new MediaView();
    private final Label status = new Label("No media");
    private final Label timeLabel = new Label("00:00 / 00:00");
    private final Button playPause = new Button("Play");
    private final Slider seek = new Slider(0, 100, 0);

    /** True while the user is dragging the seek thumb; blocks clock → slider writes. */
    private boolean seeking;

    @Override
    public void start(Stage stage) {
        // Keep aspect ratio; size from the center pane (not the stage) so chrome
        // (top/bottom bars) does not shrink the fitted video incorrectly.
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);

        StackPane center = new StackPane(mediaView);
        center.setStyle("-fx-background-color: #1a1a1a;");
        mediaView.fitWidthProperty().bind(center.widthProperty());
        mediaView.fitHeightProperty().bind(center.heightProperty());

        Button open = new Button("Open…");
        open.setOnAction(e -> openFile(stage));

        playPause.setDisable(true);
        playPause.setOnAction(e -> togglePlayPause());

        seek.setDisable(true);
        seek.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(seek, Priority.ALWAYS);

        // While dragging: do not let currentTimeProperty overwrite the thumb.
        // When the drag ends: seek the player to the slider value.
        seek.valueChangingProperty().addListener((obs, was, changing) -> {
            seeking = changing;
            if (!changing && mediaPlayer != null) {
                mediaPlayer.seek(Duration.millis(seek.getValue()));
            }
        });
        // Click-to-seek (no drag): valueChanging may stay false; still apply on release.
        seek.setOnMouseReleased(e -> {
            if (mediaPlayer != null && !seek.isValueChanging()) {
                mediaPlayer.seek(Duration.millis(seek.getValue()));
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

        stage.setTitle("Mini Player");
        stage.setScene(new Scene(root, 800, 500));
        // Free native decoder state when the window closes.
        stage.setOnCloseRequest(e -> disposePlayer());
        stage.show();
    }

    private void openFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open media");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Video", "*.mp4", "*.m4v", "*.aif", "*.aiff", "*.wav")
        );
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        load(file);
    }

    /**
     * Replace any current player with one for {@code file}.
     * Duration / controls become usable only after {@link MediaPlayer#setOnReady}.
     */
    private void load(File file) {
        disposePlayer();
        status.setText(file.getName());

        Media media = new Media(file.toURI().toString());
        // Codec / container failures often land here rather than as a Java exception.
        media.setOnError(() -> Platform.runLater(() ->
                status.setText("Media error: " + media.getError())));

        MediaPlayer player = new MediaPlayer(media);
        mediaPlayer = player;
        player.setOnError(() -> Platform.runLater(() -> {
            if (mediaPlayer == player) {
                status.setText("Player error: " + player.getError());
            }
        }));

        // Properties such as totalDuration are invalid until READY.
        player.setOnReady(() -> {
            if (mediaPlayer != player) {
                return;
            }
            Duration total = player.getTotalDuration();
            seek.setMax(total.toMillis());
            seek.setDisable(false);
            playPause.setDisable(false);
            playPause.setText("Pause");
            timeLabel.setText(format(Duration.ZERO) + " / " + format(total));
            player.play();
        });

        // Drive the slider and time label from the clock, unless the user is dragging.
        // Use the local player ref: dispose() nulls the field before stop callbacks finish.
        player.currentTimeProperty().addListener((obs, oldT, newT) -> {
            if (mediaPlayer != player) {
                return;
            }
            if (!seeking) {
                seek.setValue(newT.toMillis());
            }
            timeLabel.setText(format(newT) + " / " + format(player.getTotalDuration()));
        });

        player.setOnEndOfMedia(() -> {
            if (mediaPlayer != player) {
                return;
            }
            player.stop();
            playPause.setText("Play");
        });

        mediaView.setMediaPlayer(player);
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) {
            return;
        }
        MediaPlayer.Status s = mediaPlayer.getStatus();
        if (s == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPause.setText("Play");
        } else {
            mediaPlayer.play();
            playPause.setText("Pause");
        }
    }

    /**
     * Stop and dispose the current player. Call before opening another file
     * and on window close; dropping the Java reference alone is not enough.
     */
    private void disposePlayer() {
        MediaPlayer old = mediaPlayer;
        // Clear the field first so late stop/currentTime callbacks no-op safely.
        mediaPlayer = null;
        mediaView.setMediaPlayer(null);
        if (old != null) {
            old.stop();
            old.dispose();
        }
        playPause.setDisable(true);
        playPause.setText("Play");
        seek.setDisable(true);
        seek.setValue(0);
        timeLabel.setText("00:00 / 00:00");
    }

    private static String format(Duration d) {
        if (d == null || d.isUnknown() || d.isIndefinite()) {
            return "00:00";
        }
        int seconds = (int) Math.floor(d.toSeconds());
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    public static void main(String[] args) {
        // Do not construct MiniPlayerApp yourself; launch boots the toolkit
        // and then calls start(Stage) on the JavaFX Application Thread.
        launch(args);
    }
}
