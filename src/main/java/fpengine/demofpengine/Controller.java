package fpengine.demofpengine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller{
    @FXML
    private VBox vbox;
    @FXML
    private Canvas canvas;
    private GraphicsContext gc;

    @FXML
    final static int width = 800;
    @FXML
    final static int height = 600;
    final static int tickrate = 100;//in miliseconds

    Engine engine;

    Thread main;

    long start;
    long finish;
    double elapsedTime = 1.0;

    @FXML
    private void initialize()
    {
        vbox.setPrefWidth(Controller.width);
        vbox.setPrefHeight(Controller.height);

        canvas.setFocusTraversable(true);
        gc = canvas.getGraphicsContext2D();
        engine = new Engine(gc);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Controller.this.runWithoutThread();
            }
        });

    }

    private void update(GraphicsContext gc)
    {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    public void KeyPressed(Canvas canvas)
    {
        canvas.setOnKeyPressed(key -> {
            if(key.getCode().equals(KeyCode.LEFT))
            {
                engine.LEFT(true);
            }
            else if(key.getCode().equals(KeyCode.RIGHT))
            {
                engine.RIGHT(true);
            }
            else if(key.getCode().equals(KeyCode.W))
            {
                engine.UP(true);
            }
            else if(key.getCode().equals(KeyCode.S))
            {
                engine.DOWN(true);
            }
            else if(key.getCode().equals(KeyCode.A))
            {
                engine.STRAFELEFT(true);
            }
            else if(key.getCode().equals(KeyCode.D))
            {
                engine.STRAFERIGHT(true);
            }
        });
    }
    public void KeyReleased(Canvas canvas)
    {
        canvas.setOnKeyReleased(key -> {
            if(key.getCode().equals(KeyCode.LEFT))
            {
                engine.LEFT(false);
            }
            else if(key.getCode().equals(KeyCode.RIGHT))
            {
                engine.RIGHT(false);
            }
            else if(key.getCode().equals(KeyCode.W))
            {
                engine.UP(false);
            }
            else if(key.getCode().equals(KeyCode.S))
            {
                engine.DOWN(false);
            }
            else if(key.getCode().equals(KeyCode.A))
            {
                engine.STRAFELEFT(false);
            }
            else if(key.getCode().equals(KeyCode.D))
            {
                engine.STRAFERIGHT(false);
            }
        });
    }
    public void runWithoutThread()
    {

        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( Timeline.INDEFINITE );

        start = System.currentTimeMillis();
        KeyFrame kf = new KeyFrame(
                Duration.seconds(0.040),
                new EventHandler<ActionEvent>()
                {
                    public void handle(ActionEvent event)
                    {
                        finish = System.currentTimeMillis();
                        elapsedTime = finish - start;
                        start = finish;
                        elapsedTime /= 1000;
                        KeyPressed(canvas);
                        KeyReleased(canvas);
                        Stage stage = (Stage) canvas.getScene().getWindow();
                        stage.setTitle("FPS: " + 1.0/elapsedTime);
                        update(gc);
                        engine.move(1.0/elapsedTime/30);
                        engine.draw();
                    }
                });

        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();
    }
}