package fpengine.demofpengine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EnemySprite extends ObjectSprite {
    /**Statyczna zmienna przechowywująca ilość przeciwników na mapie*/
    public static int numberOfEnemies = 0;

    /**Zmienna przechowywująca aktualną ilość zdrowia przeciwnika*/
    private int health;

    /**Zmienna przechowywująca każdą klatkę animacji śmierci przeciwnika*/
    private AnimatedSprite dieSprite;

    /**Zmienna przechowywująca każdą klatkę animacji dostawania obrażeń przeciwnika*/
    private AnimatedSprite painSprite;

    /**Zmienna przechowywująca każdą klatkę animacji strzelania przeciwnika*/
    private AnimatedSprite shootSprite;

    /**Tablica przechowywująca każdą klatkę animacji chodzenia przeciwnika w ośmiu kierunkach*/
    private ArrayList<AnimatedSprite> walkSprite;

    Sprite drop;

    /**Zmienna przechowywująca referencję do dźwięku bólu przeciwnika*/
    private Media painSound;

    /**Zmienna przechowywująca referencję do dźwięku śmierci przeciwnika*/
    private Media deathSound;

    /**Zmienna przechowywująca referencję do dźwięku wystrzału z broni przeciwnika*/
    private Media weaponEnemySound;

    /**Zmienna przechowywująca czy nasz przeciwnik nie dawno wykonal atak i przysługuje mu cooldown na niego*/
    boolean recentlyShoot = false;

    /**Zmienna przechowywująca obrażenia zadawane przez tego przeciwnika*/
    int myDmg = 8;
    /**Konstruktor przyjmujący pozycję przeciwnika*/
    EnemySprite(double posX, double posY)
    {
        super(posX,posY);
        status = 0;
        ai = true;
        numberOfEnemies++;
        health = 50;
        for(int i = 1; i <= 8; i++)//caly obrot postaci
        {
            defaultSprite.add(new Sprite(new File("sprites\\wsjheerpack\\mguard_s_1.bmp").toURI().toString(),getPositionY(),getPositionY(),Color.rgb(152,0,136)));
        }

        dieSprite = new AnimatedSprite(0.150);
        for(int i = 1; i <= 4;i++)
        {
            dieSprite.add(
                    new Sprite(new File("sprites\\wsjheerpack\\mguard_die" + i + ".bmp").toURI().toString(),
                            defaultSprite.get(0).getPositionX(),defaultSprite.get(0).getPositionY(),
                            Color.rgb(152,0,136)

                    )
            );
        }

        painSprite = new AnimatedSprite(0.100);
        for(int i = 1; i <= 2;i++)
        {
            painSprite.add(
                    new Sprite(new File("sprites\\wsjheerpack\\mguard_pain" + i + ".bmp").toURI().toString(),
                            defaultSprite.get(0).getPositionX(),defaultSprite.get(0).getPositionY(),
                            Color.rgb(152,0,136)

                    )
            );
        }

        shootSprite = new AnimatedSprite(0.2);
        for(int i = 1; i <= 3;i++)
        {
            shootSprite.add(
                    new Sprite(new File("sprites\\wsjheerpack\\mguard_shoot" + i + ".bmp").toURI().toString(),
                            defaultSprite.get(0).getPositionX(),defaultSprite.get(0).getPositionY(),
                            Color.rgb(152,0,136)

                    )
            );
        }
        shootSprite.add(
                new Sprite(new File("sprites\\wsjheerpack\\mguard_shoot" + 2 + ".bmp").toURI().toString(),
                        defaultSprite.get(0).getPositionX(),defaultSprite.get(0).getPositionY(),
                        Color.rgb(152,0,136)

                )
        );
        walkSprite = new ArrayList<>();
        for(int i = 1; i <= 8; i++)//caly obrot postaci
        {
            walkSprite.add(new AnimatedSprite(0.200));
            for(int j = 1; j <= 4; j++)
            {
                walkSprite.get(i - 1).add(new Sprite(new File("sprites\\wsjheerpack\\mguard_w" + j + "_" + i + ".bmp").toURI().toString(),
                        defaultSprite.get(0).getPositionX(), defaultSprite.get(0).getPositionY(), Color.rgb(152,0, 136)));
            }
        }
        painSound = new Media(new File("sounds\\EnemyPain.wav").toURI().toString());
        deathSound = new Media(new File("sounds\\Death" + (int)(Math.random() * (2 - 1 + 1) + 1) + ".wav").toURI().toString());
        weaponEnemySound = new Media(new File("sounds\\MachineGunEnemy.wav").toURI().toString());
        drop = new Sprite(new File("sprites\\mp7hl2\\mp7hl2pickup.bmp").toURI().toString(), Color.rgb(152,0,136));

    }
    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    /**Funkcja obsługująca dostawanie obrażeń przez przeciwnika*/
    public void getDMG(int dmg,MediaPlayer mediaPlayer)
    {
        if(ai)
        {
            health = health - dmg;
            if(health <= 0)//nasz przeciwnik nie zyje
            {
                ai = false;
                velocityX = 0;
                velocityY = 0;
                status = 4;
                dieSprite.anim(false);
                setToDie();//ustawiamy czyszczenie, zeby po paru sekundach sprite nam zniknal
                mediaPlayer = new MediaPlayer(deathSound);
                mediaPlayer.play();
            }
            else//zyje, dostal obrazenia
            {
                status = 3;
                painSprite.anim(true);
                hurting();
                mediaPlayer = new MediaPlayer(painSound);
                mediaPlayer.play();
            }
        }
    }
    /**Funkcja obsługująca strzelanie sprite'u*/
    public void shoot(MediaPlayer mediaPlayer)
    {
        if(ai && !getSynchroStatus())//jesli ai jest wlaczone, oraz nie wykonuje zadnej czynnosci
        {
            recentlyShoot = true;
            status = 2;
            shootSprite.anim(true);

            //bardzo nieeleganckie - lepiej by bylo jakby samo animowanie sprite'u to ogarnialo
            getTimerStatusUpdate(shootSprite.getLength(), shootSprite.getDuration(), false);
            setRecentlyShootTimer();//ja pierdole to jest jeszcze glupsze xDDDD
            //ale ogolnie chodzi o to by strzelal zalozmy co 3 sekundy

            mediaPlayer = new MediaPlayer(weaponEnemySound);
            mediaPlayer.play();
        }
    }

    /**Funkcja obsługująca chodzenie przeciwnika*/
    public void walk()
    {
        if(ai && !getSynchroStatus() && (velocityX > 0 || velocityY > 0))
        {
            status = 1;
            walkSprite.get(0).anim(true);
        }
        else
        {
            status = 0;
        }
    }

    /**Funkcja obsługująca timer który zwraca ai przeciwnikowi*/
    public void getTimerStatusUpdate(int CycleCount, double duration, boolean AiChange)
    {
        if(AiChange)
            ai = false;//wylaczmy ai zeby np.: nam debil nie strzelal jak dostaje obrazenia
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( CycleCount );
        AtomicInteger i = new AtomicInteger();
        KeyFrame kf = new KeyFrame(Duration.seconds(duration),
                event -> {
                    velocityX = 0;
                    velocityY = 0;
                    if(i.incrementAndGet() > CycleCount - 1) {
                        status = 0;
                        if(AiChange)
                            ai = true;
                    }
                });
        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();
    }

    public void hurting()
    {
        getTimerStatusUpdate(painSprite.getLength(),painSprite.getDuration(),true);
    }

    /**Funkcja obsługująca timer w celu ustawienia przeciwnika do usunięcia po nastu-sekundach*/
    public void setToDie()
    {
        numberOfEnemies--;
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( 10 );
        AtomicInteger i = new AtomicInteger();
        KeyFrame kf = new KeyFrame(Duration.seconds(1),
                event -> {
                    if(i.incrementAndGet() > 9)
                        toRemove = true;
                });
        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();
    }

    /**Funkcja obsługująca cooldown strzelania*/
    public void setRecentlyShootTimer()
    {
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( 4 );
        AtomicInteger i = new AtomicInteger();
        KeyFrame kf = new KeyFrame(Duration.seconds(1),
                event -> {
                    if(i.incrementAndGet() > 3)
                        recentlyShoot = false;
                });
        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();
    }


    /**Funkcja obsługująca zwracanie odpowiedniej klatki grafiki w zależności od trybu działania przeciwnika*/
    @Override
    public Sprite getCurrentSprite()
    {
        if(status == 0 && ai)
        {
            return defaultSprite.get(0);
        }
        else if(status == 1 && ai)
        {
            return walkSprite.get(0).getFrame(walkSprite.get(0).pointer);
        }
        else if(status == 2 && ai)
        {
            return shootSprite.getFrame(shootSprite.pointer);
        }
        else if(status == 3 && ai)
        {
            return painSprite.getFrame(painSprite.pointer);
        }
        else if (status == 4 || !ai)
        {
            return dieSprite.getFrame(dieSprite.pointer);
        }
        return null;
    }

    /**Funkcja zwracająca czy jakaś animacja jest wykonywana*/
    public boolean getSynchroStatus()
    {
        if(status == 0)
        {
            return false;
        }
        else if(status == 1)
        {
            return false;//zwracamy false przy chodzeniu bo chcemy zeby wykonywal inne akcje jak chodzi, w innym wypadku moze nam sie zablkowac i przestac strzelac
        }
        else if(status == 2)
        {
            return shootSprite.synchSprite;
        }
        else if(status == 3)
        {
            return painSprite.synchSprite;
        }
        else if (status == 4 || ai == false)//jest na koncu wiec jak dostanie obrazenia to nie wezmie tego pod uwage, ale moze sie zdarzyc ze jakims cudem umrze i chcemy by uzywal dobrego sprite'u
        {
            return dieSprite.synchSprite;
        }
        return false;
    }
}
