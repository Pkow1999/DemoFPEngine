package fpengine.demofpengine;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;

public class AnimatedSprite{
    private ArrayList<Sprite> Frames;
    int pointer = 0;
    private double duration;
    boolean synchSprite = false;
    AnimatedSprite()
    {
        Frames = new ArrayList<>();
    }

    AnimatedSprite(ArrayList<Sprite> framesImg)
    {
        this.Frames = new ArrayList<>(framesImg);
    }

    AnimatedSprite(ArrayList<Sprite> framesImg, double duration)
    {
        this.Frames = new ArrayList<>(framesImg);
        this.duration = duration;
    }

    AnimatedSprite(AnimatedSprite animImg)
    {
        this.Frames = new ArrayList<>(animImg.Frames);
        this.duration = animImg.duration;
    }

    AnimatedSprite(double seconds)
    {
        Frames = new ArrayList<>();
        this.duration = seconds;
    }
    AnimatedSprite(Sprite img, double seconds)
    {
        Frames = new ArrayList<>();
        Frames.add(img);
        this.duration = seconds;
    }
    void add(Sprite img)
    {
        Frames.add(img);
    }
    double getDuration(){return duration;}
    void setDuration(double seconds){this.duration = seconds;}
    Sprite getFrame(int frameIndex) {
        return Frames.get(frameIndex);
    }
    int getLength(){return Frames.size();}
    void anim(boolean repeat)
    {
        synchSprite = true;
            Timeline gameLoop = new Timeline();
            gameLoop.setCycleCount(Frames.size());
            KeyFrame kf = new KeyFrame(Duration.seconds(duration),
                    event -> {
                if(pointer < Frames.size() - 1)
                {
                    pointer++;
                }
                else {
                    if(repeat)
                        pointer = 0;
                    synchSprite = false;
                }
                    });
            gameLoop.getKeyFrames().add( kf );
            gameLoop.play();
    }

}
