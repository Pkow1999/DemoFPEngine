package fpengine.demofpengine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class Engine {
    double playerX = 1.0;
    double playerY = 1.0;
    double playerAngle;

    double FOV = 1.0/2.0;// Math.PI/4.0

    float Depth = 16.0f;//maksymalny draw distance
    Map plansza;

    private boolean left,right,forward,backward,strafeLeft,strafeRight;

    int sizeOfBlock = 5;
    double Height;
    double Width;

    GraphicsContext gc;
    Engine(GraphicsContext context)
    {
        try {
            plansza = new Map("Maps\\plansza1.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        gc = context;
        Height = gc.getCanvas().getHeight();
        Width = gc.getCanvas().getHeight();
    }
    void draw()//dygresja co do petli: cost efficient byloby na przyklad nie rysowanie co klatki 1 piksela tylko na przyklad jakiegos bloku
    {
        for(int i = 0;i < gc.getCanvas().getWidth(); i+= sizeOfBlock)//pętla nam leci przez całą szerokosc naszego ekranu
        {
            double RayAngle = (playerAngle -  FOV/2.0) + (i/Width) * FOV;

            float DistanceToWall = 0.0f;//jest to float bo nie potrzebuje duzej precyzji, zwiekszam to o 0.0625
            boolean hitWall = false;
            boolean isBoundary = false;

            boolean hitDoor = false;

            double EyeX = Math.sin(RayAngle);//wektor jednostkowy dla naszego promienia w przestrzeni playera
            double EyeY = Math.cos(RayAngle);

            while ((!hitWall && !hitDoor) && DistanceToWall < Depth)//dopoki nie uderzymy w sciane nasz promien sie zwieksza
            {
                DistanceToWall += 0.0625;//0.0625 zeby komputer sie nie dlawil
                //konwersja na inty bo chodzimy po indeksach
                int nTestX = (int) (playerX + EyeX*DistanceToWall);
                int nTestY = (int) (playerY + EyeY*DistanceToWall);

                if(nTestX < 0 || nTestX > plansza.getWidth() || nTestY < 0 || nTestY > plansza.getHeight())//poza obszarem
                {
                    hitWall = true;
                    DistanceToWall = Depth;
                }
                else
                {
                    if(plansza.map[nTestY][nTestX] == '#')
                    {
                        hitWall = true;
                        ArrayList<Pair<Double, Double>> p = new ArrayList<>();

                        for(int tx = 0; tx < 2;tx++)
                            for(int ty = 0; ty < 2; ty++)
                            {
                                double vy = (double) nTestY + ty - playerY;
                                double vx = (double) nTestX + tx - playerX;
                                double d = Math.sqrt(vx * vx + vy * vy);
                                double dot = (EyeX * vx / d) + (EyeY * vy / d);
                                p.add(new Pair<>(d, dot));
                            }

                        p.sort(Comparator.comparing(Pair::getKey));

                        double bound = 0.005;
                        if(Math.acos(p.get(0).getValue()) < bound)
                            isBoundary = true;
                        if(Math.acos(p.get(1).getValue()) < bound)
                            isBoundary = true;
                    }
                    else if(plansza.map[nTestY][nTestX] == 'd')
                    {
                        hitDoor = true;
                    }
                }
            }

            int ceiling = (int) (Height /2.0 - Height / DistanceToWall);//im dalej sciana tym wiekszy sufit
            int floor = (int) (Height - ceiling);//jak jest duzy sufit to i podloga musi byc duza



            for(int j = 0; j < Height;j+= sizeOfBlock)//wysokosc
            {
                if(j <= ceiling)
                {
                    gc.setFill(Color.BLACK);
                    gc.fillRect(i, j, sizeOfBlock, sizeOfBlock);
                }
                else if(j <= floor)
                {
                    Color kolor = Color.GRAY;
                    if(hitDoor)
                        kolor = Color.ORANGE;
                    Color boundryKolor = Color.DARKGREEN;
                    double colorDepth = 3.0;

                    while (colorDepth > 1.0)
                    {
                        if(DistanceToWall < Depth/colorDepth)
                        {
                            gc.setFill(kolor);
                            if(isBoundary)
                                gc.setFill(boundryKolor);
                            break;
                        }
                        else {
                            kolor = kolor.darker();
                            boundryKolor = boundryKolor.darker();
                            colorDepth =  colorDepth - 1.0;
                        }
                    }
                    gc.fillRect(i,j,sizeOfBlock,sizeOfBlock);
                }
                else {
                    double floorDepth = 1.0 - ( ((double)j -  Height/2.0 ) / ( Height / 2.0 ) );

                    if(floorDepth < 0.25)
                    {
                        gc.setFill(Color.DARKGREEN);
                    }
                    else if (floorDepth < 0.5)
                    {
                        gc.setFill(Color.DARKGREEN.darker());
                    }
                    else if(floorDepth < 0.75)
                    {
                        gc.setFill(Color.DARKGREEN.darker().darker());
                    }
                    else if(floorDepth < 0.875)//idealne
                    {
                        gc.setFill(Color.BLACK);
                    }
                    gc.fillRect(i, j, sizeOfBlock, sizeOfBlock);

                }
            }

        }
    }
    public void move(double fps)
    {
        if(left)
        {
            playerAngle -= 0.1 * fps;
        }
        if(right)
        {
            playerAngle += 0.1 * fps;
        }
        if(forward)
        {
            playerX += Math.sin(playerAngle) * 0.2;
            playerY += Math.cos(playerAngle) * 0.2;


            if(plansza.map[(int) playerY][(int) playerX] == '#')
            {
                playerX -= Math.sin(playerAngle) * 0.2;
                playerY -= Math.cos(playerAngle) * 0.2;
            }
            if(plansza.map[(int) playerY][(int) playerX] == 'd')
            {
                plansza.map[(int) playerY][(int) playerX] = '.';
            }
        }
        if(backward)
        {
            playerX -= Math.sin(playerAngle) * 0.2;
            playerY -= Math.cos(playerAngle) * 0.2;

            if(plansza.map[(int) playerY][(int) playerX] == '#')
            {
                playerX += Math.sin(playerAngle) * 0.2;
                playerY += Math.cos(playerAngle) * 0.2;
            }
        }
        if(strafeRight)
        {
            playerX += Math.cos(playerAngle) * 0.2;
            playerY -= Math.sin(playerAngle) * 0.2;


            if(plansza.map[(int) playerY][(int) playerX] == '#')
            {
                playerX -= Math.cos(playerAngle) * 0.2;
                playerY += Math.sin(playerAngle) * 0.2;
            }
        }
        if(strafeLeft)
        {

            playerX -= Math.cos(playerAngle) * 0.2;
            playerY += Math.sin(playerAngle) * 0.2;


            if(plansza.map[(int) playerY][(int) playerX] == '#')
            {
                playerX += Math.cos(playerAngle) * 0.2;
                playerY -= Math.sin(playerAngle) * 0.2;
            }
        }
        if(plansza.map[(int) playerY][(int) playerX] == 'd')
        {
            plansza.map[(int) playerY][(int) playerX] = '.';
        }
    }
    public void LEFT(boolean input)
    {
        left = input;
    }
    public void RIGHT(boolean input)
    {
        right = input;
    }
    public void UP(boolean input)
    {
        forward = input;
    }
    public void DOWN(boolean input)
    {
        backward = input;
    }
    public void STRAFELEFT(boolean input)
    {
        strafeLeft = input;
    }
    public void STRAFERIGHT(boolean input)
    {
        strafeRight = input;
    }

}
