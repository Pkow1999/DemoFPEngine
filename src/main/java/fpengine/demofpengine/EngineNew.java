package fpengine.demofpengine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

//Oparte o: https://lodev.org/cgtutor/raycasting.html
//oraz to: https://github.com/OneLoneCoder/videos/blob/master/OneLoneCoder_ComandLineFPS_2.cpp
public class EngineNew {//DYGRESJA - PixelWriter jest zdecydowanie szybszy niz fillrect ale nie obsluguje transparentnosci i malowania po blokach
    double playerX = 2.0;
    double playerY = 2.0;
    double playerAngle;

    double FOV = -Math.PI/4.0;// 45 stopni

    //double FOV = -11.0 * Math.PI / 30.0;//66 stopni

    double Depth = 16.0;//maksymalny draw distance
    Map plansza;

    private boolean left,right,forward,backward,strafeLeft,strafeRight,action,oldAction;

    Sprite wall;
    Sprite door;
    Sprite bWall;
    ArrayList<Color> floorColor;
    ArrayList<Sprite> listOfObjects;
    ArrayList<AnimatedSprite> listOfEnemies;

    int sizeOfBlock = 4;
    final double Height;
    final double Width;

    GraphicsContext gc;
    double Buffer[];
    Media doorSound;
    MediaPlayer mediaPlayer;
    Weapon weapon0;
    Weapon weapon1;
    ArrayList<Weapon> weaponArrayList;
    int slot = 1;
    EngineNew(GraphicsContext context)
    {
        oldAction = false;
        try {
            plansza = new Map("Maps\\plansza1.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
       // plansza = new Map(16,16,true,(int)playerX,(int)playerY);
        gc = context;
        Height = gc.getCanvas().getHeight();
        Width = gc.getCanvas().getWidth();
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Consolas",7));

        weapon0 = new Weapon();


        AnimatedSprite pistol = new AnimatedSprite(0.100);

        for(int i = 1; i < 6;i++)
        {
            pistol.add(
                    new Sprite(new File("sprites\\weapons\\pistol\\pistol" + i + ".bmp").toURI().toString(),300,300,
                            (Controller.width - 300)/2,
                            Controller.height - 300,
                            Color.rgb(152,0,136)

                    )//Machine Gun Sprites

                    //by Z. Franz
            );
        }
        weapon1 = new Weapon(pistol,"sounds\\Pistol.wav",30,120,8);

        weaponArrayList = new ArrayList<>();
        weaponArrayList.add(weapon0);
        weaponArrayList.add(weapon1);

        doorSound = new Media(new File("C:\\Users\\pkow1\\IdeaProjects\\DemoFPEngine\\sounds\\Door.wav").toURI().toString());
        floorColor = new ArrayList<>();
        floorColor.add(Color.DARKGREEN);
        floorColor.add(floorColor.get(0).darker());
        floorColor.add(floorColor.get(1).darker());

        wall = new Sprite(new File("sprites\\wall.png").toURI().toString(),null);
        door = new Sprite(new File("sprites\\door.png").toURI().toString(),null);
        bWall = new Sprite(new File("sprites\\Blue_wall.gif").toURI().toString(),null);

        listOfObjects = new ArrayList<>();
        //listOfObjects.add(new Sprite(new File("sprites\\barrel.png").toURI().toString(),4.5,3.5 ,Color.BLACK));
        listOfObjects.add(new Sprite(new File("sprites\\barrel.png").toURI().toString(),14.5,7.5, Color.BLACK));
        listOfObjects.add(new Sprite(new File("sprites\\greenlight.png").toURI().toString(),3,6.5, Color.BLACK));

        AnimatedSprite enemy = new AnimatedSprite(0.15);
        enemy.add(new Sprite (new File ("sprites\\mguard_s_1.bmp").toURI().toString(),13.5,6.5, Color.rgb(152,0,136)) ) ;

        for(int i = 1; i < 5;i++)
        {
           enemy.add(
                    new Sprite(new File("sprites\\mguard_die" + i + ".bmp").toURI().toString(),300,300,
                            enemy.getFrame(0).getPositionX(),enemy.getFrame(0).getPositionY(),
                            Color.rgb(152,0,136)

                    )
            );
        }

        AnimatedSprite enemy2 = new AnimatedSprite(0.15);
        enemy2.add(new Sprite (new File ("sprites\\mguard_s_1.bmp").toURI().toString(),4.5,3.5, Color.rgb(152,0,136)) ) ;

        for(int i = 1; i < 5;i++)
        {
            enemy2.add(
                    new Sprite(new File("sprites\\mguard_die" + i + ".bmp").toURI().toString(),300,300,
                            enemy2.getFrame(0).getPositionX(),enemy2.getFrame(0).getPositionY(),
                            Color.rgb(152,0,136)

                    )
            );
        }

        AnimatedSprite enemy1 = new AnimatedSprite(0.15);
        enemy1.add(new Sprite (new File ("sprites\\mguard_s_1.bmp").toURI().toString(),13.5,4, Color.rgb(152,0,136)) ) ;

        for(int i = 1; i < 5;i++)
        {
            enemy1.add(
                    new Sprite(new File("sprites\\mguard_die" + i + ".bmp").toURI().toString(),300,300,
                            enemy1.getFrame(0).getPositionX(),enemy1.getFrame(0).getPositionY(),
                            Color.rgb(152,0,136)

                    )
            );
        }

        listOfEnemies = new ArrayList<>();
        listOfEnemies.add(enemy);
        listOfEnemies.add(enemy1);
        listOfEnemies.add(enemy2);
        Buffer = new double[(int) Width];
    }
    void drawMap()
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
            double EyeY = Math.cos(RayAngle);//trzeba pamietac ze ten Y odnosi sie do naszej przestrzeni 2 wymiarowej w tablicy czyli gora/dol
            //oba daja nam pelne wspolrzedne biegunowe


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

            int ceiling = (int) ( (Height / 2.0) - (Height / DistanceToWall) );//im dalej sciana tym wiekszy sufit
            int floor = (int) (Height - ceiling);//jak jest duzy sufit to i podloga musi byc duza - basicaly odbicie lustrzane
            for(int i = 0; i < sizeOfBlock; i++)
                Buffer[x + i] = DistanceToWall;
            for (int y = 0; y < Height; y += sizeOfBlock)//idziemy po wysokosci
            {//sufitem sie nie zajmujemy bo robi to za nas funkcja clear
                if (y > ceiling && y <= floor)
                {
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
                      //  gc.getPixelWriter().setColor(x,y,kolor);
                }
                else if(y > floor) // podloga
                {
                    double floorDepth = 1.0 - (((double) y - Height / 2.0) / (Height / 2.0));

                    if (floorDepth < 0.25) {
                        gc.setFill(floorColor.get(0));
                        //gc.getPixelWriter().setColor(x,y,floorColor.get(0));
                    } else if (floorDepth < 0.5) {
                        gc.setFill(floorColor.get(1));
                        //gc.getPixelWriter().setColor(x,y,floorColor.get(1));
                    } else {
                        gc.setFill(floorColor.get(2));
                        //gc.getPixelWriter().setColor(x,y,floorColor.get(2));
                    }

//                    else if (floorDepth < 0.875)//idealne
//                    {
//                        gc.setFill(Color.BLACK);
//                        //gc.getPixelWriter().setColor(x,y,Color.BLACK);
//                    }
                    gc.fillRect(x, y, sizeOfBlock, sizeOfBlock);

                }
            }
        }
    }

    // TODO: 02.03.2022 dalej jest problem z przenikajacymi sprite'ami
    public void drawObjects()
    {
        for(Sprite sprite : listOfObjects)
        {
            double vecX = sprite.getPositionX() - playerX;
            double vecY = sprite.getPositionY() - playerY;
            double DistanceFromPlayer = Math.sqrt(vecX * vecX + vecY * vecY);


            double EyeX = Math.sin(playerAngle);
            double EyeY = Math.cos(playerAngle);
            double objectAngle = Math.atan2(EyeY, EyeX) - Math.atan2(vecY,vecX);


            if(objectAngle < -Math.PI)
                objectAngle += 2.0 * Math.PI;
            if(objectAngle > Math.PI)
                objectAngle -= 2.0 * Math.PI;

            boolean inPlayerFov = Math.abs(objectAngle) <Math.abs( FOV / 2.0);
            if(inPlayerFov && DistanceFromPlayer >= 0.9 && DistanceFromPlayer < Depth)
            {
                int objectCeiling = (int) ((Height / 2.0) - (Height / DistanceFromPlayer));
                int objectFloor = (int)Height - objectCeiling;
                int objectHeight = objectFloor - objectCeiling;
                double objectAspectRatio = (double) sprite.getHeight() / (double) sprite.getWidth();
                double objectWidth = objectHeight / objectAspectRatio;
                double middleOfObject = (2.0 * (objectAngle / (FOV * 2.0)) + 0.5) * Width;
                for(double lx = 0; lx < objectWidth; lx+=sizeOfBlock)
                    for(double ly = 0; ly < objectHeight; ly+=sizeOfBlock)
                    {
                        double sampleX = lx / objectWidth;
                        double sampleY = ly / objectHeight;
                        int objectColumn = (int)( middleOfObject + lx - (objectWidth / 2.0) ) ;
                        if(objectColumn >= 0 && objectColumn < Width)
                        {
                            if(Buffer[objectColumn] >= DistanceFromPlayer)
                            {
                                gc.setFill(sprite.getSampleColor(sampleX,sampleY));
                                gc.fillRect(objectColumn,objectCeiling + ly - 1,sizeOfBlock,sizeOfBlock);
                                Buffer[objectColumn] = DistanceFromPlayer;
                               // gc.getPixelWriter().setColor(objectColumn, (int) (objectCeiling + ly),sprite.getSampleColor(sampleX,sampleY));

                                if(Math.abs(objectAngle) <Math.abs( FOV / 8.0) && action)
                                    sprite.setRemoveTag(true);
                            }
                        }
                    }
            }
        }
        listOfObjects.removeIf(Sprite::getRemoveTag);
    }

    public void drawEnemies()
    {
        //sortEnemies();
        for(AnimatedSprite sprite : listOfEnemies)
        {
            double vecX = sprite.getFrame(sprite.pointer).getPositionX() - playerX;
            double vecY = sprite.getFrame(sprite.pointer).getPositionY() - playerY;
            double DistanceFromPlayer = Math.sqrt(vecX * vecX + vecY * vecY);


            double EyeX = Math.sin(playerAngle);
            double EyeY = Math.cos(playerAngle);
            double objectAngle = Math.atan2(EyeY, EyeX) - Math.atan2(vecY,vecX);


            if(objectAngle < -Math.PI)
                objectAngle += 2.0 * Math.PI;
            if(objectAngle > Math.PI)
                objectAngle -= 2.0 * Math.PI;

            boolean inPlayerFov = Math.abs(objectAngle) <Math.abs( FOV / 2.0);
            if(inPlayerFov && DistanceFromPlayer >= 0.9 && DistanceFromPlayer < Depth)
            {
                int objectCeiling = (int) ((Height / 2.0) - (Height / DistanceFromPlayer));
                int objectFloor = (int)Height - objectCeiling;
                int objectHeight = objectFloor - objectCeiling;
                double objectAspectRatio = (double) sprite.getFrame(sprite.pointer).getHeight() / (double) sprite.getFrame(sprite.pointer).getWidth();
                int objectWidth = (int)(objectHeight / objectAspectRatio);
                double middleOfObject = (2.0 * (objectAngle / (FOV * 2.0)) + 0.5) * Width;
                for(double lx = 0; lx < objectWidth; lx+=sizeOfBlock)
                    for(double ly = 0; ly < objectHeight; ly+=sizeOfBlock)
                    {
                        double sampleX = lx / objectWidth;
                        double sampleY = ly / objectHeight;
                        int objectColumn = (int)( middleOfObject + lx - (objectWidth / 2.0) ) ;
                        if(objectColumn >= 0 && objectColumn < Width)
                        {
                            if(Buffer[objectColumn] >= DistanceFromPlayer)
                            {
                                gc.setFill(sprite.getFrame(sprite.pointer).getSampleColor(sampleX,sampleY));
                                gc.fillRect(objectColumn,objectCeiling + ly - 1,sizeOfBlock,sizeOfBlock);
                                Buffer[objectColumn] = DistanceFromPlayer;
                                // gc.getPixelWriter().setColor(objectColumn, (int) (objectCeiling + ly),sprite.getSampleColor(sampleX,sampleY));

                                if(Math.abs(objectAngle) <Math.abs( FOV / 8.0) && action && !sprite.synchSprite && weaponArrayList.get(slot).synchronization && DistanceFromPlayer < weaponArrayList.get(slot).distance) {
                                    sprite.anim();
                                }
                            }
                        }
                    }
            }
        }
        listOfEnemies.removeIf(n->(n.getFrame(0).getRemoveTag()));
    }
    public void drawStatic()
    {
        gc.setFill(Color.RED);
        gc.fillText(plansza.export(),0,5);
        gc.drawImage(weaponArrayList.get(slot).weaponSprite.getFrame(weaponArrayList.get(slot).weaponSprite.pointer).getSprite(),weaponArrayList.get(slot).weaponSprite.getFrame(weaponArrayList.get(slot).weaponSprite.pointer).getPositionX(),weaponArrayList.get(slot).weaponSprite.getFrame(weaponArrayList.get(slot).weaponSprite.pointer).getPositionY());//bronkie rysujemy co klatke
    }
    public void move(double fps)
    {
        int oldPLayerX = (int) playerX;
        int oldPlayerY = (int) playerY;

        //if(!oldAction)//to gwarantuje mi ze jak nacisniemy przycisk akcji to zostanie on wykonany raz
            if(!weaponArrayList.get(slot).synchronization)//nie jest wykonywana animacja
                if(action)
                {
                    shoot();
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
        gameLoop.setCycleCount( 3 );
        AtomicInteger i = new AtomicInteger();
        KeyFrame kf = new KeyFrame(Duration.seconds(1),
                event -> {
            plansza.setMap(X, Y, '.');
            if(i.incrementAndGet() > 2)
                plansza.setMap(X, Y, 'd');
                });
        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();
        mediaPlayer = new MediaPlayer(doorSound);
        mediaPlayer.play();
    }
    void shoot() {
        if(weaponArrayList.get(slot).currentAmmo > 0)
            weaponArrayList.get(slot).shoot(mediaPlayer);
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

    public void SLOT(int code) {
         if(code - 49 < weaponArrayList.size() && code - 49 >= 0)
             slot = code - 49;
    }
}
