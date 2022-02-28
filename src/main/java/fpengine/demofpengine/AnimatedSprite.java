package fpengine.demofpengine;


import java.util.ArrayList;

public class AnimatedSprite {
    public ArrayList<Sprite> Frames;
    double duration;
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

    AnimatedSprite(double duration)
    {
        Frames = new ArrayList<>();
        this.duration = duration;
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
    int getLength(){return Frames.size();}
}
