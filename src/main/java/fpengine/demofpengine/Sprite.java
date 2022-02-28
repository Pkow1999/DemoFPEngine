package fpengine.demofpengine;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Sprite {
    private int Width;
    private int Height;
    private double positionX;
    private double positionY;
    private WritableImage sprite;
    private final Color transparent = Color.rgb(152,0,136);
    public Sprite(String url) {
        Image img = new Image(url);
        processImg(img);
    }

    Sprite(String url, int width,int height) {
        Image img = new Image(url,width,height,true,true);
        processImg(img);
    }
    Sprite(String url, double positionX, double positionY) {
        Image img = new Image(url);
        this.positionX = positionX;
        this.positionY = positionY;
        processImg(img);
    }
    Sprite(String url,int width, int height, double positionX, double positionY) {
        Image img = new Image(url,width,height,true,true);
        this.positionX = positionX;
        this.positionY = positionY;
        processImg(img);
    }

//usuwa wszystkie fioletowe piksele
    void processImg(Image img) {
        Width = (int) img.getWidth();
        Height = (int) img.getHeight();
        sprite = new WritableImage(Width, Height);
        for(int i = 0; i < Width; i++)
            for(int j = 0; j < Height; j++)
            {
                if(img.getPixelReader().getColor(i,j).equals(transparent)) {
                    sprite.getPixelWriter().setColor(i,j,Color.TRANSPARENT);
                }
                else{
                    sprite.getPixelWriter().setColor(i,j,img.getPixelReader().getColor(i,j));
                }
            }
    }


    public Color getColor(int pixelX, int pixelY) {
        return sprite.getPixelReader().getColor(pixelX,pixelY);
    }
    public Color getSampleColor(double sampleX, double sampleY) {
        if(sampleX < 0 || sampleY < 0 || sampleX > Width || sampleY > Height)
            return Color.BLACK;
        double posX = sampleX * Width;
        double posY = sampleY * Height;
        return sprite.getPixelReader().getColor((int)posX,(int) posY);
    }
    public WritableImage getSprite(){return sprite;}
    public int getWidth() {return Width;}
    public int getHeight(){return Height;}
    public double getPositionX(){return positionX;}
    public double getPositionY(){return positionY;}
    public void setPositionX(double pX){positionX = pX;}
    public  void setPositionY(double pY){positionY = pY;}
    public Rectangle2D getBoundary() {
        return new Rectangle2D(positionX,positionY, Width, Height);
    }
    public boolean intersects(Sprite s) {
        return s.getBoundary().intersects( this.getBoundary() );
    }
}
