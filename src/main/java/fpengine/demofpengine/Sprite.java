package fpengine.demofpengine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Sprite {
    int width;
    int height;
    double normWidth = 1.0;
    double normHeight = 1.0;
    WritableImage sprite;
    Color transparent = Color.rgb(152,0,136);
    public Sprite(String url)
    {
        Image img = new Image(url);
        processImg(img);
    }

    Sprite(String url, int w,int h)
    {
        Image img = new Image(url,w,h,true,true);
        processImg(img);
    }

    void processImg(Image img)
    {
        width = (int) img.getWidth();
        height = (int) img.getHeight();
        sprite = new WritableImage(width, height);
        for(int i = 0; i < width; i++)
            for(int j = 0;j < height;j++)
            {
                if(img.getPixelReader().getColor(i,j).equals(transparent)) {
                    sprite.getPixelWriter().setColor(i,j,Color.TRANSPARENT);
                }
                else{
                    sprite.getPixelWriter().setColor(i,j,img.getPixelReader().getColor(i,j));
                }
            }
    }
    public Color getColor(int positionX, int positionY) {
//        int posX = positionX % width;
//        int posY = positionY % height;
        return sprite.getPixelReader().getColor(positionX,positionY);
    }
    public Color getSampleColor(double sampleX, double sampleY)
    {
        if(sampleX < 0 || sampleY < 0 || sampleX > width || sampleY > height)
            return Color.BLACK;
        double posX = sampleX * width;
        double posY = sampleY * height;
        return sprite.getPixelReader().getColor((int)posX,(int) posY);
    }
//    public Rectangle2D getBoundary()
//    {
//        return new Rectangle2D(positionX,positionY,width,height);
//    }
//    public boolean intersects(Sprite s)
//    {
//        return s.getBoundary().intersects( this.getBoundary() );
//    }
}
