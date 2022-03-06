package fpengine.demofpengine;

import java.util.ArrayList;

public class ObjectSprite {
    protected ArrayList<Sprite> defaultSprite;
    protected double positionX;
    protected double positionY;
    protected double velocityX = 0;
    protected double velocityY = 0;
    boolean toRemove = false;
    ObjectSprite(Sprite sprite, double posX, double posY)
    {
        defaultSprite = new ArrayList<>();
        defaultSprite.add(sprite);
        positionX = posX;
        positionY = posY;
    }
    ObjectSprite(double posX, double posY)
    {
        defaultSprite = new ArrayList<>();
        positionX = posX;
        positionY = posY;
    }
    public Sprite getCurrentSprite()
    {
        if(defaultSprite.size() > 0)
            return defaultSprite.get(0);
        return null;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }


    public double getHeight()
    {
        return defaultSprite.get(0).getHeight();
    }

    public double getWidth()
    {
        return defaultSprite.get(0).getWidth();
    }

}
