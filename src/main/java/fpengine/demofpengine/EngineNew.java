package fpengine.demofpengine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


//Oparte o: https://lodev.org/cgtutor/raycasting.html
public class EngineNew {
    double playerX = 2.0;
    double playerY = 2.0;
    double playerAngle;

    double FOV = -3.14159/4.0;// 45 stopni

    //double FOV = -11.0 * Math.PI / 30.0;//66 stopni

    double Depth = 8.0;//maksymalny draw distance
    Map plansza;

    private boolean left,right,forward,backward,strafeLeft,strafeRight,action,oldAction;

    AnimatedSprite weapon;
    Sprite wall;
    Sprite door;
    Sprite bWall;
    int sizeOfBlock = 4;
    double Height;
    double Width;
    GraphicsContext gc;

    int pointerWeapon = 0;

    EngineNew(GraphicsContext context)
    {
        oldAction = false;
        try {
            plansza = new Map("Maps\\plansza1.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        gc = context;
        Height = gc.getCanvas().getHeight();
        Width = gc.getCanvas().getWidth();
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Consolas",7));

        weapon = new AnimatedSprite(0.110);
        for(int i = 0; i < 5;i++)
        {
            weapon.add(
                    new Sprite("C:\\Users\\pkow1\\IdeaProjects\\DemoFPEngine\\sprites\\handshotgun" + i + ".gif",300,300,
                    (Width - 300)/2,
                    Height - 300
                    )
            );
        }
        wall = new Sprite("C:\\Users\\pkow1\\IdeaProjects\\DemoFPEngine\\sprites\\Stone.gif");
        door = new Sprite("C:\\Users\\pkow1\\IdeaProjects\\DemoFPEngine\\sprites\\door.gif");
        bWall = new Sprite("C:\\Users\\pkow1\\IdeaProjects\\DemoFPEngine\\sprites\\Blue_wall.gif");

    }
    void draw(double elapsedTime)
    {
        for(int x = 0; x < Width; x+=sizeOfBlock) {
            double RayAngle = (playerAngle - FOV / 2.0) + (x / Width) * FOV;//kat naszego promienia wystrzeliwanego co cala szerokosc obrazu
            double DistanceToWall = 0.0;
            double sampleX = 0.0;

            //zmienne czy walnelismy sciane albo drzwi
            boolean hitWall = false;
            boolean hitDoor = false;
            boolean hitBlueWall = false;

            double EyeX = Math.sin(RayAngle);//wektor jednostkowy dla naszego promienia w przestrzeni playera
            double EyeY = Math.cos(RayAngle);


            //nasz krok przesuniecia po indeksach tabeli ala mapy/planszy
            int stepx;
            int stepy;

            //nasz krok przesuniecia w oparciu o kat patrzenia naszej postaci przesuniety o pole w indeksie
            double Sx = Math.sqrt(1.0 + (EyeY / EyeX) * (EyeY / EyeX));
            double Sy = Math.sqrt(1.0 + (EyeX / EyeY) * (EyeX / EyeY));

            //dlugosc promienia jednowymiarowego w osiach X i Y
            double rayLenght1Dx;
            double rayLenght1Dy;

            //nasze indeksy do sprawdzenia czy znajduje sie w nich sciana
            int mapCheckerX = (int) playerX;
            int mapCheckerY = (int) playerY;

            //sprawdzenie w ktora strone sie patrzymy
            //jesli w lewo na naszej tabeli to indeksy sie beda nam zmniejszac
            if (EyeX < 0) {
                stepx = -1;
                rayLenght1Dx = (playerX - mapCheckerX) * Sx;
            } else {//jak w prawo to zwiekszac
                stepx = 1;
                rayLenght1Dx = (mapCheckerX + 1.0 - playerX) * Sx;
            }
            //jesli patrzymy w gore to zmniejszac
            if (EyeY < 0) {
                stepy = -1;
                rayLenght1Dy = (playerY - mapCheckerY) * Sy;
            } else {//jak w dol to zwiekszac
                stepy = 1;
                rayLenght1Dy = (mapCheckerY + 1.0 - playerY) * Sy;
            }

            //liczymy nasza odleglosc dopoki nie trafimy na sciane/drzwi albo nasz nasz draw distance sie skonczy
            while (!hitWall && !hitDoor && !hitBlueWall && DistanceToWall < Depth) {
                //bierzemy najmniejszy promien
                if (rayLenght1Dx < rayLenght1Dy) {
                    mapCheckerX += stepx;//i nasz promien idzie w osi X
                    DistanceToWall = rayLenght1Dx;
                    rayLenght1Dx += Sx;
                } else {
                    mapCheckerY += stepy;//albo w osi Y jesli promien jednowymiarowy Y byl mniejszy
                    DistanceToWall = rayLenght1Dy;
                    rayLenght1Dy += Sy;
                }
                //jak znajdujemy sie na mapie (a nie gdzies poza nia)
                if (mapCheckerX >= 0 && mapCheckerX < plansza.getWidth() && mapCheckerY >= 0 && mapCheckerY < plansza.getHeight()) {
                    if (plansza.getMap(mapCheckerX, mapCheckerY) == '#')//to sprawdzamy czy trafilismy w sciane
                    {
                        hitWall = true;

                    }
                    if (plansza.getMap(mapCheckerX, mapCheckerY) == 'd') {//czy w drzwi
                        hitDoor = true;
                    }
                    if (plansza.getMap(mapCheckerX, mapCheckerY) == 'H') {
                        hitBlueWall = true;
                    }
                }
                double blockMidX = (double)mapCheckerX + 0.5;
                double blockMidY = (double)mapCheckerY + 0.5;

                double testPointX = playerX + EyeX * DistanceToWall;
                double testPointY = playerY + EyeY * DistanceToWall;

                double testAngle =  Math.atan2((testPointY - blockMidY),(testPointX - blockMidX));
                if (testAngle >= -3.14159f * 0.25f && testAngle < 3.14159f * 0.25f)
                    sampleX = testPointY - mapCheckerY;
                if (testAngle >= 3.14159f * 0.25f && testAngle < 3.14159f * 0.75f)
                    sampleX = testPointX - mapCheckerX;
                if (testAngle < -3.14159f * 0.25f && testAngle >= -3.14159f * 0.75f)
                    sampleX = testPointX - mapCheckerX;
                if (testAngle >= 3.14159f * 0.75f || testAngle < -3.14159f * 0.75f)
                    sampleX = testPointY - mapCheckerY;
            }

            int ceiling = (int) (Height / 2.0 - Height / DistanceToWall);//im dalej sciana tym wiekszy sufit
            int floor = (int) (Height - ceiling);//jak jest duzy sufit to i podloga musi byc duza - basicaly odbicie lustrzane

            for (int y = 0; y < Height; y += sizeOfBlock)//idziemy po wysokosci
            {
                if (y <= ceiling) {
                    gc.setFill(Color.BLACK);//sufit malujemy na czarno
                } else if (y < floor) {
                    double sampleY = ((double)y - (double)ceiling) / ((double)floor - (double)ceiling);
                    Color kolor = wall.getSampleColor(sampleX,sampleY);
                    if(hitBlueWall)
                        kolor = bWall.getSampleColor(sampleX,sampleY);

                    if (hitDoor)
                        kolor = door.getSampleColor(sampleX,sampleY);

                    if(DistanceToWall > Depth - 0.125)
                        kolor = Color.BLACK;

                    gc.setFill(kolor);
                    gc.fillRect(x, y, sizeOfBlock, sizeOfBlock);
                } else {
                    double floorDepth = 1.0 - (((double) y - Height / 2.0) / (Height / 2.0));

                    if (floorDepth < 0.25) {
                        gc.setFill(Color.DARKGREEN);
                    } else if (floorDepth < 0.5) {
                        gc.setFill(Color.DARKGREEN.darker());
                    } else if (floorDepth < 0.75) {
                        gc.setFill(Color.DARKGREEN.darker().darker());
                    } else if (floorDepth < 0.875)//idealne
                    {
                        gc.setFill(Color.BLACK);
                    }
                    gc.fillRect(x, y, sizeOfBlock, sizeOfBlock);
                }
            }
        }
        gc.setFill(Color.RED);
        gc.fillText(plansza.export(),0,5);
        gc.drawImage(weapon.getFrame(pointerWeapon).getSprite(),weapon.getFrame(pointerWeapon).getPositionX(),weapon.getFrame(pointerWeapon).getPositionY());//bronkie rysujemy co klatke
    }
    public void move(double fps)
    {
        int oldPLayerX = (int) playerX;
        int oldPlayerY = (int) playerY;

        if(!oldAction)//to gwarantuje mi ze jak nacisniemy przycisk akcji to zostanie on wykonany raz
            if(action)
            {
                shoot(fps);
            }

        if(right)
        {
            playerAngle -= 1.5 * fps;
        }
        if(left)
        {
            playerAngle += 1.5 * fps;
        }
        if(forward)
        {
            playerX += Math.sin(playerAngle) * 4 * fps;
            playerY += Math.cos(playerAngle) * 4 * fps;


            if(plansza.getMap((int) playerX,(int) playerY) == '#' || plansza.getMap((int) playerX,(int) playerY) == 'H')
            {
                playerX -= Math.sin(playerAngle) * 4 * fps;
                playerY -= Math.cos(playerAngle) * 4 * fps;
            }
        }
        if(backward)
        {
            playerX -= Math.sin(playerAngle) * 4 * fps;
            playerY -= Math.cos(playerAngle) * 4 * fps;

            if(plansza.getMap((int) playerX,(int) playerY) == '#' || plansza.getMap((int) playerX,(int) playerY) == 'H')
            {
                playerX += Math.sin(playerAngle) * 4 * fps;
                playerY += Math.cos(playerAngle) * 4 * fps;
            }
        }
        if(strafeLeft)
        {
            playerX += Math.cos(playerAngle) * 4 * fps;
            playerY -= Math.sin(playerAngle) * 4 * fps;


            if(plansza.getMap((int) playerX,(int) playerY) == '#' || plansza.getMap((int) playerX,(int) playerY) == 'H')
            {
                playerX -= Math.cos(playerAngle) * 4 * fps;
                playerY += Math.sin(playerAngle) * 4 * fps;
            }
        }
        if(strafeRight)
        {

            playerX -= Math.cos(playerAngle) * 4 * fps;
            playerY += Math.sin(playerAngle) * 4 * fps;


            if(plansza.getMap((int) playerX,(int) playerY) == '#' || plansza.getMap((int) playerX,(int) playerY) == 'H')
            {
                playerX += Math.cos(playerAngle) * 4 * fps;
                playerY -= Math.sin(playerAngle) * 4 * fps;
            }
        }
        if(plansza.getMap((int) playerX,(int) playerY) == 'd')
            openDoor((int) playerX, (int) playerY);
        plansza.setMap(oldPLayerX, oldPlayerY, '.');
        plansza.setMap((int) playerX, (int) playerY, 'P');
        oldAction = action;//zapisujemy poprzednia wartosc w celu identyfikacji czy nie robimy akcji kilka razy pod rzad
    }
    void openDoor(int X, int Y)
    {
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( 4 );
        AtomicInteger i = new AtomicInteger();
        KeyFrame kf = new KeyFrame(Duration.seconds(1),
                event -> {
            plansza.setMap(X, Y, '.');
            if(i.incrementAndGet() > 3)
                plansza.setMap(X, Y, 'd');
                });
        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();

    }
    void shoot(double elapsedTime) {//ALE mozemy zmmienic indeks broni ktory rysujemy co te klatke
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( weapon.getLength() );
        KeyFrame kf = new KeyFrame(Duration.seconds(weapon.getDuration()),
                event -> {
                    pointerWeapon++;
                    if(pointerWeapon >= weapon.getLength())
                        pointerWeapon = 0;
                });
        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();
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
    public void ACTION(boolean input)
    {
        action = input;
    }

}
