package fpengine.demofpengine;

public class Player {
    public double posX;
    public double posY;
    public double velX;
    public double velY;
    public int slot = 1;
    public int hurt = 0;
    public int Health = 100;
    public int maxHealth = 100;
    public double playerAngle;
    public double FOV;
    public boolean strafing = false;
    Player()
    {

    }
    void updatePosition()
    {
        posX += velX;
        posY += velY;
        if(EngineNew.plansza.getMap((int) posX,(int) posY) == 'X' || EngineNew.plansza.getMap((int) posX,(int) posY) == 'H')
        {
            posX -= velX;
            posY -= velY;
        }
    }
}
