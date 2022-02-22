package fpengine.demofpengine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import java.io.IOException;



//Oparte o: https://lodev.org/cgtutor/raycasting.html
public class EngineNew {
    double playerX = 1.0;
    double playerY = 1.0;
    double playerAngle;

    //double FOV = Math.PI/4.0;// 45 stopni

    double FOV = 11.0 * Math.PI / 30.0;//66 stopni

    double Depth = 8.25;//maksymalny draw distance
    Map plansza;

    private boolean left,right,forward,backward,strafeLeft,strafeRight;

    int sizeOfBlock = 5;
    double Height;
    double Width;

    GraphicsContext gc;
    EngineNew(GraphicsContext context)
    {
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
    }
    void draw()
    {
        for(int x = 0; x < Width; x+=sizeOfBlock)
        {
            double RayAngle = (playerAngle -  FOV/2.0) + (x/Width) * FOV;//kat naszego promienia wystrzeliwanego co cala szerokosc obrazu
            double DistanceToWall = 0.0;

            //zmienne czy walnelismy sciane albo drzwi
            boolean hitWall = false;
            boolean hitdoor = false;

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
            if(EyeX < 0) {
                stepx = -1;
                rayLenght1Dx = (playerX - mapCheckerX) * Sx;
            }
            else {//jak w prawo to zwiekszac
                stepx = 1;
                rayLenght1Dx = ( mapCheckerX + 1.0 - playerX) * Sx;
            }
            //jesli patrzymy w gore to zmniejszac
            if(EyeY < 0){
                stepy = -1;
                rayLenght1Dy = (playerY - mapCheckerY) * Sy;
            }
            else {//jak w dol to zwiekszac
                stepy = 1;
                rayLenght1Dy = (mapCheckerY + 1.0 - playerY) * Sy;
            }

            //liczymy nasza odleglosc dopoki nie trafimy na sciane/drzwi albo nasz nasz draw distance sie skonczy
            while (!hitWall && !hitdoor && DistanceToWall < Depth)
            {
                //bierzemy najmniejszy promien
                if(rayLenght1Dx < rayLenght1Dy) {
                    mapCheckerX += stepx;//i nasz promien idzie w osi X
                    DistanceToWall = rayLenght1Dx;
                    rayLenght1Dx += Sx;
                }
                else {
                    mapCheckerY += stepy;//albo w osi Y jesli promien jednowymiarowy Y byl mniejszy
                    DistanceToWall = rayLenght1Dy;
                    rayLenght1Dy += Sy;
                }
                //jak znajdujemy sie na mapie (a nie gdzies poza nia)
                if(mapCheckerX >= 0 && mapCheckerX < plansza.getWidth() && mapCheckerY >= 0 && mapCheckerY < plansza.getHeight()) {
                    if(plansza.map[mapCheckerY][mapCheckerX] == '#')//to sprawdzamy czy trafilismy w sciane
                        hitWall = true;
                    if(plansza.map[mapCheckerY][mapCheckerX] == 'd')//czy w drzwi
                        hitdoor = true;
                }//inaczej moglibysmy zobaczyc jakies smieci poza zaalokowana pamiecia
                //w przeciwnym wypadku lecimy dalej
            }

            int ceiling = (int) (Height /2.0 - Height / DistanceToWall);//im dalej sciana tym wiekszy sufit
            int floor = (int) (Height - ceiling);//jak jest duzy sufit to i podloga musi byc duza - basicaly odbicie lustrzane

            for(int y = 0; y < Height;y+= sizeOfBlock)//idziemy po wysokosci
            {
                if(y <= ceiling) {
                    gc.setFill(Color.BLACK);//sufit malujemy na czarno
                }
                else if(y < floor) {
                    if(hitdoor){
                        gc.setFill(Color.YELLOW);
                    }
                    else if(hitWall){
                        gc.setFill(Color.BLUE);
                    }
                }
                else{
                    gc.setFill(Color.GREEN);
                }
                gc.fillRect(x,y,sizeOfBlock,sizeOfBlock);
            }
        }
        gc.setFill(Color.WHITE);
        gc.fillText(plansza.export(),0,5);
    }
    public void move(double fps)
    {
        int oldPLayerX = (int) playerX;
        int oldPlayerY = (int) playerY;
        if(left)
        {
            playerAngle -= 0.1;
        }
        if(right)
        {
            playerAngle += 0.1;
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
        plansza.map[oldPlayerY][oldPLayerX] = '.';
        plansza.map[(int) playerY][(int) playerX] = 'P';
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
