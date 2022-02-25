package fpengine.demofpengine;

import javafx.scene.image.Image;

import java.util.ArrayList;

public class AnimatedImage {
    public ArrayList<Image> Frames;
    double duration;
    AnimatedImage()
    {
        Frames = new ArrayList<>();
    }

    AnimatedImage(ArrayList<Image> framesImg)
    {
        this.Frames = new ArrayList<>(framesImg);
    }

    AnimatedImage(ArrayList<Image> framesImg, double duration)
    {
        this.Frames = new ArrayList<>(framesImg);
        this.duration = duration;
    }

    AnimatedImage(AnimatedImage animImg)
    {
        this.Frames = new ArrayList<>(animImg.Frames);
        this.duration = animImg.duration;
    }

    AnimatedImage(double duration)
    {
        Frames = new ArrayList<>();
        this.duration = duration;
    }
    AnimatedImage(Image img, double duration)
    {
        Frames = new ArrayList<>();
        Frames.add(img);
        this.duration = duration;
    }
    void add(Image img)
    {
        Frames.add(img);
    }
    int getLength(){return Frames.size();}
}
