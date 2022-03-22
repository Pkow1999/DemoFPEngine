package fpengine.demofpengine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;//WAZNE Info: FillRect bierze punkt x i punkt y a nastepnie DLUGOSC (nie punkty koncowe)
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

//Oparte o: https://lodev.org/cgtutor/raycasting.html
//oraz to: https://github.com/OneLoneCoder/videos/blob/master/OneLoneCoder_ComandLineFPS_2.cpp
public class EngineNew {//DYGRESJA - PixelWriter jest zdecydowanie szybszy niz fillrect ale nie obsluguje transparentnosci i malowania po blokach
    Player player;
    //double FOV = -11.0 * Math.PI / 30.0;//66 stopni

    double Depth = 16.0;//maksymalny draw distance
    static Map plansza;

    private boolean left,right,forward,backward,strafeLeft,strafeRight,action,oldAction;
    boolean side = false;
    Sprite wall;
    Sprite door;
    Sprite bWall;
    Sprite bloodSplatter;
    ArrayList<Color> floorColor;
    ArrayList<ObjectSprite> listOfObjects;

    int sizeOfBlock = 4;
    final double Height;
    final double Width;

    GraphicsContext gc;
    double Buffer[];
    Media doorSound;
    Media playerHurt;
    MediaPlayer mediaPlayer;
    ArrayList<Weapon> weaponArrayList;
    ArrayList<Pair<Integer,Integer>> hurtPosition;
    EngineNew(GraphicsContext context)
    {
        player = new Player();
        player.posX = 2.0;
        player.posY = 2.0;
        player.FOV =-Math.PI/4.0;// 45 stopni

        oldAction = false;
        hurtPosition = new ArrayList<>();
        try {
            plansza = new Map("Maps\\plansza1.txt");
        } catch (IOException e) {
            e.printStackTrace();
            e.getMessage();
            System.out.println("Error, loading map");
        }
       // plansza = new Map(16,16,true,(int)playerX,(int)playerY);
        gc = context;
        Height = gc.getCanvas().getHeight();
        Width = gc.getCanvas().getWidth();
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Consolas",7));

        Weapon weapon0 = new Weapon();


        AnimatedSprite pistol = new AnimatedSprite(0.100);
        weapon0.dmg = 50;
        for(int i = 1; i < 6;i++)
        {
            pistol.add(
                    new Sprite(new File("sprites\\weapons\\pistol\\pistol" + i + ".bmp").toURI().toString(),300,300,
                            (Width - 300)/2,
                            Height - 300,
                            Color.rgb(152,0,136)

                    )//Pistol Sprites

                    //by Z. Franz
            );
        }
        Weapon weapon1 = new Weapon(pistol,"sounds\\Pistol.wav",30,120,8);
        weapon1.dmg = 20;
        AnimatedSprite mg = new AnimatedSprite(0.040);

        for(int i = 1; i < 4;i++)
        {
            mg.add(
                    new Sprite(new File("sprites\\weapons\\machinegun\\mg" + i + ".bmp").toURI().toString(),300,300,
                            (Width - 300)/2,
                            Height - 300,
                            Color.rgb(152,0,136)

                    )//Machine Gun Sprites

                    //by Z. Franz
            );
        }
        Weapon weapon2 = new Weapon(mg, "sounds\\MachineGun.wav", 30, 120, 6);
        weapon2.dmg = 18;


        weaponArrayList = new ArrayList<>();
        weaponArrayList.add(weapon0);
        weaponArrayList.add(weapon1);
        weaponArrayList.add(weapon2);
        floorColor = new ArrayList<>();
        floorColor.add(Color.DARKGREEN);
        floorColor.add(floorColor.get(0).darker());
        floorColor.add(floorColor.get(1).darker());

        playerHurt = new Media(new File("sounds\\PlayerPain1.wav").toURI().toString());
        doorSound = new Media(new File("sounds\\Door.wav").toURI().toString());

        wall = new Sprite(new File("sprites\\wall.png").toURI().toString(),null);
        door = new Sprite(new File("sprites\\door.png").toURI().toString(),null);
        bWall = new Sprite(new File("sprites\\Blue_wall.gif").toURI().toString(),null);
        bloodSplatter = new Sprite(new File("sprites\\blood.png").toURI().toString(),null);


        Sprite barrel = new Sprite(new File("sprites\\barrel.png").toURI().toString(), Color.BLACK);
        Sprite lamp = new Sprite(new File("sprites\\greenlight.png").toURI().toString(), Color.BLACK);

        listOfObjects = new ArrayList<>();
        listOfObjects.add(new ObjectSprite(barrel,2.5, 4.5 ));
        listOfObjects.add(new ObjectSprite(barrel,2.5, 2.5 ));
        listOfObjects.add(new ObjectSprite(lamp,5,6 ));


        for(Pair XY : plansza.enemiesPosition)
        {
            int X = (int) XY.getKey();
            int Y = (int) XY.getValue();
            listOfObjects.add(new EnemySprite(X,  Y + 0.5));
        }
        Buffer = new double[(int) Width];//blok pamieci ktory przechowuje w pamieci dystans do scinay w kazdym z odcinkow bloku ktory generujemy
    }
    void drawMap()
    {
        for(int x = 0; x < Width; x+=sizeOfBlock) {
            double RayAngle = (player.playerAngle - player.FOV / 2.0) + (x / Width) * player.FOV;//kat naszego promienia wystrzeliwanego co cala szerokosc obrazu
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
            int mapCheckerX = (int) player.posX;
            int mapCheckerY = (int) player.posY;

            //sprawdzenie w ktora strone sie patrzymy
            //jesli w lewo na naszej tabeli to indeksy sie beda nam zmniejszac
            if (EyeX < 0) {
                stepx = -1;
                rayLenght1Dx = (player.posX - mapCheckerX) * Sx;
            } else {//jak w prawo to zwiekszac
                stepx = 1;
                rayLenght1Dx = (mapCheckerX + 1.0 - player.posX) * Sx;
            }
            //jesli patrzymy w gore to zmniejszac
            if (EyeY < 0) {
                stepy = -1;
                rayLenght1Dy = (player.posY - mapCheckerY) * Sy;
            } else {//jak w dol to zwiekszac
                stepy = 1;
                rayLenght1Dy = (mapCheckerY + 1.0 - player.posY) * Sy;
            }

            //liczymy nasza odleglosc dopoki nie trafimy na sciane/drzwi albo nasz nasz draw distance sie skonczy
            while (!hitWall && !hitDoor && !hitBlueWall && DistanceToWall < Depth) {
                //bierzemy najmniejszy promien
                if (rayLenght1Dx < rayLenght1Dy) {
                    mapCheckerX += stepx;//i nasz promien idzie w osi X
                    DistanceToWall = rayLenght1Dx;
                    rayLenght1Dx += Sx;
                    side =false;
                } else {
                    mapCheckerY += stepy;//albo w osi Y jesli promien jednowymiarowy Y byl mniejszy
                    DistanceToWall = rayLenght1Dy;
                    rayLenght1Dy += Sy;
                    side = true;
                }
                //jak znajdujemy sie na mapie (a nie gdzies poza nia)
                if (mapCheckerX >= 0 && mapCheckerX < plansza.getWidth() && mapCheckerY >= 0 && mapCheckerY < plansza.getHeight()) {
                    if (plansza.getMap(mapCheckerX, mapCheckerY) == 'X')//to sprawdzamy czy trafilismy w sciane
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

                double testPointX = player.posX + EyeX * DistanceToWall;
                double testPointY = player.posY + EyeY * DistanceToWall;

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
            {
                if(y <= ceiling)
                {
                    gc.setFill(Color.DARKGRAY);
                    gc.fillRect(x, y, sizeOfBlock, sizeOfBlock);
                }
                else if (y > ceiling && y <= floor)
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

    public void drawObjects(double elapsedTime)
    {
        boolean drop = false;
        sortObjects();//to sortowanie... dziala.. sprite'y sie nie nakladaja ale boje sie o performence poprzez kopiowanie calej tablicy
        //jak wymysle/znajde sposob by jej nie kopiowac to poprawie ale nie jest to duzy priorytem
        //choc wymaga wiecej testowania bo nie wiem jak sie zachowuje przy duzej ilosci sprite'ow
        for(ObjectSprite sprite : listOfObjects)
        {


            sprite.positionX -= sprite.velocityX * elapsedTime;
            sprite.positionY -= sprite.velocityY * elapsedTime;
            if(plansza.getMap((int)sprite.getPositionX(), (int)sprite.getPositionY()) == 'X')
            {

                sprite.positionX += sprite.velocityX * elapsedTime;
                sprite.positionY += sprite.velocityY * elapsedTime;
            }
            double vecX = sprite.getPositionX() - player.posX;
            double vecY = sprite.getPositionY() - player.posY;
            double DistanceFromPlayer = Math.sqrt(vecX * vecX + vecY * vecY);


            double EyeX = Math.sin(player.playerAngle);
            double EyeY = Math.cos(player.playerAngle);
            double objectAngle = Math.atan2(EyeY, EyeX) - Math.atan2(vecY,vecX);


            if(objectAngle < -Math.PI)
                objectAngle += 2.0 * Math.PI;
            if(objectAngle > Math.PI)
                objectAngle -= 2.0 * Math.PI;

            boolean inPlayerFov = Math.abs(objectAngle) < Math.abs(player.FOV);//jak jest normalny fov to nie ma pop upu od srodka sprite'a

            if(sprite.ai)
            {
                aiComputation(DistanceFromPlayer,(EnemySprite) sprite, Math.atan2(vecX,vecY));
            }
            if(inPlayerFov && DistanceFromPlayer >= 0.5 && DistanceFromPlayer < Depth)
            {
                int objectCeiling = (int) ((Height / 2.0) - (Height / DistanceFromPlayer));
                int objectFloor = (int)Height - objectCeiling;
                int objectHeight = objectFloor - objectCeiling;
                double objectAspectRatio = sprite.getHeight() / sprite.getWidth();
                int objectWidth = (int)(objectHeight / objectAspectRatio);
                double middleOfObject = (2.0 * (objectAngle / (player.FOV * 2.0)) + 0.5) * Width;

                //sprobujmy zrobic to samo dla ludzi
                int ratio = 1;
                if(objectWidth > Width/2)
                    ratio = 2;
                //dziala i nie wyglada az tak tragicznie
                //kurcze na ludzi to dziala mam wrazenie znacznie lepiej (nie zauwazylem zadnego dropu) wiec musze sie przyjrzec tym beczkom
                //moze format? (png/gif)
                for(double lx = 0; lx < objectWidth; lx+=sizeOfBlock * ratio)
                    for(double ly = 0; ly < objectHeight; ly+=sizeOfBlock * ratio)
                    {
                        double sampleX = lx / objectWidth;
                        double sampleY = ly / objectHeight;
                        int objectColumn = (int)( middleOfObject + lx - (objectWidth / 2.0) ) ;
                        if(objectColumn >= 0 && objectColumn < Width)
                        {
                            if(Buffer[objectColumn] >= DistanceFromPlayer)
                            {
                                gc.setFill(sprite.getCurrentSprite().getSampleColor(sampleX,sampleY));
                                gc.fillRect(objectColumn,objectCeiling + ly - 1,sizeOfBlock * ratio,sizeOfBlock * ratio);
                                Buffer[objectColumn] = DistanceFromPlayer;
                                // gc.getPixelWriter().setColor(objectColumn, (int) (objectCeiling + ly),sprite.getSampleColor(sampleX,sampleY));

                                if(Math.abs(objectAngle) <Math.abs( player.FOV / 8.0) && action) //jesli jest w naszym polu widzenia i jest wykonywana akcja strzelania
                                {
                                    //funny thing - status 0 to zwykle stanie a status 1 strzelanie
                                    //ten status jest po to aby nie zalapalo nam przez przypadek kilku dmgu w trakcie jednego strzalu
                                    //tzn dmg dostaje tylko jak nic nie robi albo strzela
                                    //w innym wypadku gra pokazuje animacje dostawania bolu (ktora jest na tyle krotka ze czlowiek nie powinien zwrocic uwagi na to ze ktos jest niewrazliwy)
                                    //(albo jest martwy co w sumie sprawia ze i tak nie powinien dostawac zadnego dmg)
                                    if(sprite.status < 3 && weaponArrayList.get(player.slot).synchronization && weaponArrayList.get(player.slot).weaponSprite.pointer == 0 && DistanceFromPlayer < weaponArrayList.get(player.slot).distance)//jesli nasz przeciwnik nie wykonuje zadnej animacji oraz bron nie wykonuje zadnej animacji oraz jestesmy w odpowiednim dystansie
                                    {//sprite status o wartosci maxValue to stale sprite'y
                                        int dmg = (int) (weaponArrayList.get(player.slot).dmg / (DistanceFromPlayer/3.0 ));
                                        EnemySprite enemy = (EnemySprite)  sprite;
                                        enemy.getDMG(dmg, mediaPlayer);//go kill
                                    }
                                }
                            }
                        }
                    }
            }
        }
        listOfObjects.removeIf(n->(n.toRemove));
    }


    //TODO - pojawil sie nowy bug z zadawaniem kilkunastu obrazen kiedy idziesz w kierunku wroga
    public void aiComputation(double DistanceToPlayer,  EnemySprite sprite, double enemyAngle) {
        boolean wall = false;
        double EyeX = -Math.sin(enemyAngle);
        double EyeY = -Math.cos(enemyAngle);
        double distance = 0;


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
        int mapCheckerX = (int) sprite.getPositionX();
        int mapCheckerY = (int) sprite.getPositionY();

        //sprawdzenie w ktora strone sie patrzymy
        //jesli w lewo na naszej tabeli to indeksy sie beda nam zmniejszac
        if (EyeX < 0) {
            stepx = -1;
            rayLenght1Dx = (sprite.getPositionX() - mapCheckerX) * Sx;
        } else {//jak w prawo to zwiekszac
            stepx = 1;
            rayLenght1Dx = (mapCheckerX + 1.0 - sprite.getPositionX()) * Sx;
        }
        //jesli patrzymy w gore to zmniejszac
        if (EyeY < 0) {
            stepy = -1;
            rayLenght1Dy = (sprite.getPositionY() - mapCheckerY) * Sy;
        } else {//jak w dol to zwiekszac
            stepy = 1;
            rayLenght1Dy = (mapCheckerY + 1.0 - sprite.getPositionY()) * Sy;
        }

        //liczymy nasza odleglosc dopoki nie trafimy na sciane/drzwi albo nasz nasz draw distance sie skonczy
        while (!wall && distance < DistanceToPlayer) {
            //bierzemy najmniejszy promien
            if (rayLenght1Dx < rayLenght1Dy) {
                mapCheckerX += stepx;//i nasz promien idzie w osi X
                distance = rayLenght1Dx;
                rayLenght1Dx += Sx;
            } else {
                mapCheckerY += stepy;//albo w osi Y jesli promien jednowymiarowy Y byl mniejszy
                distance = rayLenght1Dy;
                rayLenght1Dy += Sy;
            }
            //jak znajdujemy sie na mapie (a nie gdzies poza nia)
            if (mapCheckerX >= 0 && mapCheckerX < plansza.getWidth() && mapCheckerY >= 0 && mapCheckerY < plansza.getHeight()) {
                if (plansza.getMap(mapCheckerX, mapCheckerY) == 'X' || plansza.getMap(mapCheckerX, mapCheckerY) == 'd' || plansza.getMap(mapCheckerX, mapCheckerY) == 'H')//to sprawdzamy czy trafilismy w sciane
                {
                    wall = true;
                }
            }
            else wall = true;
        }

        if(!wall && DistanceToPlayer >= 4 && DistanceToPlayer < 10 )
        {

            sprite.velocityX = Math.sin(enemyAngle);
            sprite.velocityY = Math.cos(enemyAngle);
            sprite.walk();
        }
        else if (!sprite.recentlyShoot && DistanceToPlayer <= 4.0 && !wall)
        {
            sprite.shoot(mediaPlayer);
            Random random = new Random();
            double chance = random.nextInt((int) (player.strafing ? 100 - 10 * DistanceToPlayer : 100 - 6 * DistanceToPlayer));
            double rng = random.nextInt(100);
            System.out.println("CHANCE: " + chance);
            System.out.println("RNG: " + rng);
            System.out.println();
            if (rng < chance)
            {
                player.Health -= sprite.myDmg;
                player.hurt += 100;
                hurtPosition.add(new Pair<>(random.nextInt((int) Width), random.nextInt((int) Height)));
                mediaPlayer = new MediaPlayer(playerHurt);
                mediaPlayer.play();
            }
        }
        else if(wall)
        {
            sprite.velocityY = 0;
            sprite.velocityX = 0;
            sprite.walk();
        }
    }
    public void drawStatic()
    {
        if(player.hurt > 0)
        {
            for(int i = 0; i < hurtPosition.size(); i++)
            {
                gc.drawImage(bloodSplatter.getSprite(), hurtPosition.get(i).getKey(), hurtPosition.get(i).getValue());
            }
            player.hurt--;
            if(player.hurt%100 == 0 )
                hurtPosition.remove(0);
        }
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Consolas",7));
        gc.fillText(plansza.export(),0,5);
        gc.setFont(Font.font("Consolas",20));
        gc.fillText(String.valueOf(EnemySprite.numberOfEnemies), Width - 30, 40);

        double ramka = 5;
        double px = 20;
        double py = Height - 40;
        double lx = 180;
        double ly = 20;

        gc.setFill(Color.DARKGRAY);
        gc.fillRoundRect(px,py,lx,ly,5,5);
        gc.setFill(Color.GRAY);

        gc.fillRect(px + ramka,py + ramka,lx - 2 * ramka,ly - 2* ramka);//5 pikseli to obwodka wiec punkt poczatkowy to jest +5 a koncowy -2 razy wartosc obwodki

        gc.setFill(Color.RED);
        gc.fillRect(px + ramka,py + ramka,lx * (double) player.Health / player.maxHealth - 2 * ramka, ly - 2*ramka);

        gc.fillText("Ammo:" + String.valueOf(weaponArrayList.get(player.slot).currentAmmo) + "/" + String.valueOf(weaponArrayList.get(player.slot).maxAmmo), Width - 150, Height - 25);
        gc.drawImage(weaponArrayList.get(player.slot).weaponSprite.getFrame(weaponArrayList.get(player.slot).weaponSprite.pointer).getSprite(),weaponArrayList.get(player.slot).weaponSprite.getFrame(weaponArrayList.get(player.slot).weaponSprite.pointer).getPositionX(),weaponArrayList.get(player.slot).weaponSprite.getFrame(weaponArrayList.get(player.slot).weaponSprite.pointer).getPositionY());//bronkie rysujemy co klatke
    }
    public void move(double elapsedTime)
    {
        int oldPLayerX = (int) player.posX;
        int oldPlayerY = (int) player.posY;
        if(!oldAction || player.slot > 1)//to gwarantuje mi ze jak nacisniemy przycisk akcji to zostanie on wykonany raz, ogien ciagly jest mozliwy dla pistoletu maszynowego
            if(!weaponArrayList.get(player.slot).synchronization)//nie jest wykonywana animacja
                if(action)
                {
                    shoot();
                }

        if(right)
        {
            player.playerAngle -= 1.5 * elapsedTime;
        }
        if(left)
        {
            player.playerAngle += 1.5 * elapsedTime;
        }
        if(forward)
        {
//            player.posX += Math.sin(player.playerAngle) * 4 * elapsedTime;
//            player.posY += Math.cos(player.playerAngle) * 4 * elapsedTime;

            player.velX += Math.sin(player.playerAngle) * 4 * elapsedTime;
            player.velY += Math.cos(player.playerAngle) * 4 * elapsedTime;
//
//            if(plansza.getMap((int) player.posX,(int) player.posY) == 'X' || plansza.getMap((int) player.posX,(int) player.posY) == 'H')
//            {
//                player.posX -= Math.sin(player.playerAngle) * 4 * elapsedTime;
//                player.posY -= Math.cos(player.playerAngle) * 4 * elapsedTime;
//            }
        }
        if(backward)
        {
//            player.posX -= Math.sin(player.playerAngle) * 4 * elapsedTime;
//            player.posY -= Math.cos(player.playerAngle) * 4 * elapsedTime;
//

            player.velX += -Math.sin(player.playerAngle) * 4 * elapsedTime;
            player.velY += -Math.cos(player.playerAngle) * 4 * elapsedTime;

//            if(plansza.getMap((int) player.posX,(int) player.posY) == 'X' || plansza.getMap((int) player.posX,(int) player.posY) == 'H')
//            {
//                player.posX += Math.sin(player.playerAngle) * 4 * elapsedTime;
//                player.posY += Math.cos(player.playerAngle) * 4 * elapsedTime;
//            }
        }
        if(strafeLeft)
        {
//            player.posX += Math.cos(player.playerAngle) * 4 * elapsedTime;
//            player.posY -= Math.sin(player.playerAngle) * 4 * elapsedTime;

            player.velX += Math.cos(player.playerAngle) * 4 * elapsedTime;
            player.velY += -Math.sin(player.playerAngle) * 4 * elapsedTime;

//            if(plansza.getMap((int) player.posX,(int) player.posY) == 'X' || plansza.getMap((int) player.posX,(int) player.posY) == 'H')
//            {
//                player.posX -= Math.cos(player.playerAngle) * 4 * elapsedTime;
//                player.posY += Math.sin(player.playerAngle) * 4 * elapsedTime;
//            }
        }
        if(strafeRight)
        {

//            player.posX -= Math.cos(player.playerAngle) * 4 * elapsedTime;
//            player.posY += Math.sin(player.playerAngle) * 4 * elapsedTime;


            player.velX += -Math.cos(player.playerAngle) * 4 * elapsedTime;
            player.velY += Math.sin(player.playerAngle) * 4 * elapsedTime;

//            if(plansza.getMap((int) player.posX,(int) player.posY) == 'X' || plansza.getMap((int) player.posX,(int) player.posY) == 'H')
//            {
//                player.posX += Math.cos(player.playerAngle) * 4 * elapsedTime;
//                player.posY -= Math.sin(player.playerAngle) * 4 * elapsedTime;
//            }
        }
        player.updatePosition();
        player.velX = 0;
        player.velY = 0;
        if(plansza.getMap((int) player.posX,(int) player.posY) == 'd')
            openDoor((int) player.posX, (int) player.posY);
        plansza.setMap(oldPLayerX, oldPlayerY, '.');
        plansza.setMap((int) player.posX, (int) player.posY, 'P');
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
        if(weaponArrayList.get(player.slot).currentAmmo > 0)
            weaponArrayList.get(player.slot).shoot(mediaPlayer);
    }
    public void LEFT(boolean input) {left = input;}
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
        player.strafing = input;
    }
    public void STRAFERIGHT(boolean input)
    {
        strafeRight = input;
        player.strafing = input;
    }
    public void ACTION(boolean input)
    {
        action = input;
    }

    public void SLOT(int code) {
         if(code - 49 < weaponArrayList.size() && code - 49 >= 0)
             player.slot = code - 49;
    }
    void sortObjects()
    {
        ArrayList<Pair<Double, Integer>> indexArray = new ArrayList<>();
        for(int i = 0; i < listOfObjects.size(); i++)
        {
            double dist = Math.pow(player.posX - listOfObjects.get(i).getPositionX(), 2) + Math.pow(player.posY - listOfObjects.get(i).getPositionY(),2);
            indexArray.add(new Pair<>(dist,i));
        }
        indexArray.sort(Comparator.comparingDouble(Pair<Double,Integer>::getKey).thenComparingInt(Pair::getValue));
        ArrayList<ObjectSprite> copy = new ArrayList<>(listOfObjects);
        for (int i = 0; i < listOfObjects.size(); i++)
        {
            listOfObjects.set(i, copy.get(indexArray.get(copy.size() - i - 1).getValue()));
        }
    }
}
