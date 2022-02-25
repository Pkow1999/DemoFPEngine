package fpengine.demofpengine;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Sprite {

    Image img;
    double positionX;
    double positionY;
    double width;
    double height;
    public Sprite(String url)
    {
        Image img = new Image(url);
        width = img.getWidth();
        height = img.getHeight();
    }
    public void render(GraphicsContext gc)
    {
        gc.drawImage(img, positionX,positionY);
    }
    public Rectangle2D getBoundary()
    {
        return new Rectangle2D(positionX,positionY,width,height);
    }
    public boolean intersects(Sprite s)
    {
        return s.getBoundary().intersects( this.getBoundary() );
    }
}
