package fpengine.demofpengine;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Comparator;

public class Sprite {
    private int Width;
    private int Height;
    private double positionX;
    private double positionY;
    private WritableImage sprite;
    private double velocityX;
    private double velocityY;
    private boolean toRemove;
    private boolean animationRemoveTag;
    private final Color transparent = Color.rgb(152,0,136);
    public Sprite(String url, Color color) {
        Image img = new Image(url);
        velocityX = 0.0;
        velocityY = 0.0;
        toRemove = false;
        animationRemoveTag = false;
        processImg(img, color);
    }

    Sprite(String url, int width,int height, Color color) {
        Image img = new Image(url,width,height,true,true);
        velocityX = 0.0;
        velocityY = 0.0;
        toRemove = false;
        animationRemoveTag = false;
        processImg(img,color);
    }
    Sprite(String url, double positionX, double positionY, Color color) {
        Image img = new Image(url);
        this.positionX = positionX;
        this.positionY = positionY;
        velocityX = 0.0;
        velocityY = 0.0;
        toRemove = false;
        animationRemoveTag = false;
        processImg(img,color);
    }
    Sprite(String url,int width, int height, double positionX, double positionY, Color color) {
        Image img = new Image(url,width,height,true,true);
        this.positionX = positionX;
        this.positionY = positionY;
        velocityX = 0.0;
        velocityY = 0.0;
        toRemove = false;
        animationRemoveTag = false;
        processImg(img,color);
    }

//usuwa wszystkie fioletowe piksele
    void processImg(Image img, Color color) {
        Width = (int) img.getWidth();
        Height = (int) img.getHeight();
        sprite = new WritableImage(Width, Height);
        for(int i = 0; i < Width; i++)
            for(int j = 0; j < Height; j++)
            {
                if(!img.getPixelReader().getColor(i,j).equals(color)) {
                    sprite.getPixelWriter().setColor(i,j,img.getPixelReader().getColor(i,j));
                }
            }
    }


    public Color getColor(int pixelX, int pixelY) {
        return sprite.getPixelReader().getColor(pixelX,pixelY);
    }
    public Color getSampleColor(double sampleX, double sampleY) {
        try {//huh X MOZE sie rownac jeden i wtedy mamy problem
            double posX = sampleX * Width;
            double posY = sampleY * Height;//modyfikujac ta wartosc moge ustawiac wysokosc scian
            if(posX < 0 || posY < 0 || posX >= Width || posY >= Height)//CHYBA rozwiazane - wymaga wiecej testowania
                return Color.BLACK;
            return sprite.getPixelReader().getColor((int)posX,(int) posY);
        }
        catch (IndexOutOfBoundsException e)
        {
            System.out.println(e.toString());
            System.out.println("X: " +  sampleX );
            System.out.println("Y: " + sampleY);
            return Color.BLACK;
        }
    }
    public WritableImage getSprite(){return sprite;}
    public int getWidth() {return Width;}
    public int getHeight(){return Height;}
    public double getPositionX(){return positionX;}
    public double getPositionY(){return positionY;}
    public void setPositionX(double pX){positionX = pX;}
    public  void setPositionY(double pY){positionY = pY;}
    public void setVelocity(double vx, double vy) { velocityX = vx; velocityY = vy;}
    public double getVelocityX(){return velocityX;}
    public double getVelocityY(){return velocityY;}
    public void setRemoveTag(boolean removeTag){toRemove = removeTag;}
    public boolean getRemoveTag(){return toRemove;}
    public boolean getAnimationRemoveTag(){return animationRemoveTag;}
    public void setAnimationRemoveTag(boolean tag){animationRemoveTag = tag;}
    public Rectangle2D getBoundary() {
        return new Rectangle2D(positionX,positionY, Width, Height);
    }
    public boolean intersects(Sprite s) {
        return s.getBoundary().intersects( this.getBoundary() );
    }

}
