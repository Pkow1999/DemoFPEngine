package fpengine.demofpengine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;



//Oparte o: https://lodev.org/cgtutor/raycasting.html
public class EngineNew {
    double playerX = 1.0;
    double playerY = 1.0;
    double playerAngle;

    double FOV = -Math.PI/4.0;// 45 stopni

    //double FOV = -11.0 * Math.PI / 30.0;//66 stopni

    double Depth = 16;//maksymalny draw distance
    Map plansza;

    private boolean left,right,forward,backward,strafeLeft,strafeRight,action,oldAction;

    AnimatedImage weapon;

    int sizeOfBlock = 5;
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

        weapon = new AnimatedImage(.1);
        for(int i = 0; i < 5;i++)
        {
            weapon.add(new Image("C:\\Users\\pkow1\\IdeaProjects\\DemoFPEngine\\sprites\\handshotgun"+i+".png",300,300,true,true));
        }
    }
    void draw(double elapsedTime)
    {
        for(int x = 0; x < Width; x+=sizeOfBlock) {
            double RayAngle = (playerAngle - FOV / 2.0) + (x / Width) * FOV;//kat naszego promienia wystrzeliwanego co cala szerokosc obrazu
            double DistanceToWall = 0.0;

            //zmienne czy walnelismy sciane albo drzwi
            boolean hitWall = false;
            boolean hitDoor = false;
            boolean isBoundary = false;

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
            while (!hitWall && !hitDoor && DistanceToWall < Depth) {
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
                    if (plansza.map[mapCheckerY][mapCheckerX] == '#')//to sprawdzamy czy trafilismy w sciane
                    {
                        hitWall = true;
                        ArrayList<Pair<Double, Double>> p = new ArrayList<>();

                        for (int tx = 0; tx < 2; tx++)
                            for (int ty = 0; ty < 2; ty++) {
                                double vy = (double) mapCheckerY + ty - playerY;
                                double vx = (double) mapCheckerX + tx - playerX;
                                double d = Math.sqrt(vx * vx + vy * vy);
                                double dot = (EyeX * vx / d) + (EyeY * vy / d);
                                p.add(new Pair<>(d, dot));
                            }

                        p.sort(Comparator.comparing(Pair::getKey));

                        double bound = 0.005;
                        if (Math.acos(p.get(0).getValue()) < bound)
                            isBoundary = true;
                        if (Math.acos(p.get(1).getValue()) < bound)
                            isBoundary = true;
                    }
                    if (plansza.map[mapCheckerY][mapCheckerX] == 'd')//czy w drzwi
                        hitDoor = true;
                }//inaczej moglibysmy zobaczyc jakies smieci poza zaalokowana pamiecia
                //w przeciwnym wypadku lecimy dalej
            }

            int ceiling = (int) (Height / 2.0 - Height / DistanceToWall);//im dalej sciana tym wiekszy sufit
            int floor = (int) (Height - ceiling);//jak jest duzy sufit to i podloga musi byc duza - basicaly odbicie lustrzane

            for (int y = 0; y < Height; y += sizeOfBlock)//idziemy po wysokosci
            {
                if (y <= ceiling) {
                    gc.setFill(Color.BLACK);//sufit malujemy na czarno
                } else if (y < floor) {
                    Color kolor = Color.GRAY;
                    if (hitDoor)
                        kolor = Color.ORANGE;
                    Color boundryKolor = Color.DARKGREEN;
                    double colorDepth = 3.0;

                    while (colorDepth > 1.0) {
                        if (DistanceToWall < Depth / colorDepth) {
                            gc.setFill(kolor);
                            if (isBoundary)
                                gc.setFill(boundryKolor);
                            break;
                        } else {
                            kolor = kolor.darker();
                            boundryKolor = boundryKolor.darker();
                            colorDepth = colorDepth - 1.0;
                        }
                    }
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
        gc.setFill(Color.WHITE);
        gc.fillText(plansza.export(),0,5);

        gc.drawImage(weapon.Frames.get(pointerWeapon),(Width - weapon.Frames.get(pointerWeapon).getWidth())/2,Height - weapon.Frames.get(pointerWeapon).getHeight());//bronkie rysujemy co klatke
    }
    void shoot(double elapsedTime) {//ALE mozemy zmmienic indeks broni ktory rysujemy co te klatke
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( weapon.getLength() );
        KeyFrame kf = new KeyFrame(Duration.seconds(0.1),//mnozymy ilosc czasu wygenerowania 1 klatki o nasze ratio aby spowolnic animacje
                event -> {//2 razy wyglada dobrze dla 30 klatek
                    pointerWeapon++;
                    if(pointerWeapon >= weapon.getLength())
                        pointerWeapon = 0;
        });
        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();
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


            if(plansza.map[(int) playerY][(int) playerX] == '#')
            {
                playerX -= Math.sin(playerAngle) * 4 * fps;
                playerY -= Math.cos(playerAngle) * 4 * fps;
            }
            if(plansza.map[(int) playerY][(int) playerX] == 'd')
            {
                plansza.map[(int) playerY][(int) playerX] = '.';
            }
        }
        if(backward)
        {
            playerX -= Math.sin(playerAngle) * 4 * fps;
            playerY -= Math.cos(playerAngle) * 4 * fps;

            if(plansza.map[(int) playerY][(int) playerX] == '#')
            {
                playerX += Math.sin(playerAngle) * 4 * fps;
                playerY += Math.cos(playerAngle) * 4 * fps;
            }
        }
        if(strafeLeft)
        {
            playerX += Math.cos(playerAngle) * 4 * fps;
            playerY -= Math.sin(playerAngle) * 4 * fps;


            if(plansza.map[(int) playerY][(int) playerX] == '#')
            {
                playerX -= Math.cos(playerAngle) * 4 * fps;
                playerY += Math.sin(playerAngle) * 4 * fps;
            }
        }
        if(strafeRight)
        {

            playerX -= Math.cos(playerAngle) * 4 * fps;
            playerY += Math.sin(playerAngle) * 4 * fps;


            if(plansza.map[(int) playerY][(int) playerX] == '#')
            {
                playerX += Math.cos(playerAngle) * 4 * fps;
                playerY -= Math.sin(playerAngle) * 4 * fps;
            }
        }
        plansza.map[oldPlayerY][oldPLayerX] = '.';
        plansza.map[(int) playerY][(int) playerX] = 'P';

        if(plansza.map[(int) playerY][(int) playerX] == 'd')
        {
            plansza.map[(int) playerY][(int) playerX] = '.';

        }
        oldAction = action;//zapisujemy poprzednia wartosc w celu identyfikacji czy nie robimy akcji kilka razy pod rzad
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
