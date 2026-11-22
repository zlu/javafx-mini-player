package io.github.zlu.javafxplayer;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Part I of the JavaFX mini-player series: one Stage, one Scene, one Label.
 * Later parts replace the center placeholder with real chrome and, eventually, video.
 */
public class HelloPlayerApp extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("JavaFX mini-player — Part I");
        title.setStyle("-fx-font-size: 18px;");

        StackPane root = new StackPane(title);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 640, 360);
        stage.setTitle("Mini Player");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
