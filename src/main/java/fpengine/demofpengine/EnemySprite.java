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
    public static int numberOfEnemies = 0;
    int status = 0;
    private int health;
    private AnimatedSprite dieSprite;
    private AnimatedSprite painSprite;
    private AnimatedSprite shootSprite;
    private ArrayList<AnimatedSprite> walkSprite;
    private Media painSound;
    private Media deathSound;
    private Media weaponEnemySound;
    boolean ai = true;
    boolean recentlyShoot = false;
    EnemySprite(double posX, double posY)
    {
        super(posX,posY);
        numberOfEnemies++;
        health = 50;
        for(int i = 1; i < 9; i++)//caly obrot postaci
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
        for(int i = 1; i <= 7; i++)//caly obrot postaci
        {
            walkSprite.add(new AnimatedSprite(0.100));
            for(int j = 1; j <= 4; j++)
            {
                walkSprite.get(i - 1).add(new Sprite(new File("sprites\\wsjheerpack\\mguard_w" + j + "_" + i + ".bmp").toURI().toString(),
                        defaultSprite.get(0).getPositionX(), defaultSprite.get(0).getPositionY(), Color.rgb(152,0, 136)));
            }
        }
        painSound = new Media(new File("sounds\\EnemyPain.wav").toURI().toString());
        deathSound = new Media(new File("sounds\\Death" + (int)(Math.random() * (2 - 1 + 1) + 1) + ".wav").toURI().toString());
        weaponEnemySound = new Media(new File("sounds\\MachineGunEnemy.wav").toURI().toString());
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
    public void getDMG(int dmg,MediaPlayer mediaPlayer)
    {
        if(ai)
        {
            health = health - dmg;
            if(health <= 0)
            {
                ai = false;
                status = 3;
                dieSprite.anim(false);
                setToDie();
                mediaPlayer = new MediaPlayer(deathSound);
                mediaPlayer.play();
            }
            else
            {
                status = 2;
                painSprite.anim(true);
                hurting();
                mediaPlayer = new MediaPlayer(painSound);
                mediaPlayer.play();
            }
        }
    }
    public void shoot(MediaPlayer mediaPlayer)
    {
        if(ai && !getSynchroStatus())
        {
            status = 1;
            shootSprite.anim(true);

            //bardzo nieeleganckie - lepiej by bylo jakby samo animowanie sprite'u to ogarnialo
            getTimerStatusUpdate(shootSprite.getLength(), shootSprite.getDuration(), false);
            recentlyShoot = true;
            setRecentlyShootTimer();//ja pierdole to jest jeszcze glupsze xDDDD
            //ale ogolnie chodzi o to by strzelal zalozmy co 3 sekundy

            mediaPlayer = new MediaPlayer(weaponEnemySound);
            mediaPlayer.play();
        }
    }
    public void getTimerStatusUpdate(int CycleCount, double duration, boolean AiChange)
    {
        if(AiChange)
            ai = false;//wylaczmy ai zeby np.: nam debil nie strzelal jak dostaje obrazenia
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( CycleCount );
        AtomicInteger i = new AtomicInteger();
        KeyFrame kf = new KeyFrame(Duration.seconds(duration),
                event -> {
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

    public void setToDie()
    {
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

    @Override
    public Sprite getCurrentSprite()
    {
        if(status == 0 && ai)
        {
            return defaultSprite.get(0);
        }
        else if(status == 1 && ai)
        {
            return shootSprite.getFrame(shootSprite.pointer);
        }
        else if(status == 2 && ai)
        {
            return painSprite.getFrame(painSprite.pointer);
        }
        else if (status == 3 || !ai)
        {
            return dieSprite.getFrame(dieSprite.pointer);
        }
        return null;
    }
    public boolean getSynchroStatus()
    {
        if(status == 0)
        {
            return false;
        }
        else if(status == 1)
        {
            return shootSprite.synchSprite;
        }
        else if(status == 2)
        {
            return painSprite.synchSprite;
        }
        else if (status == 3 || ai == false)
        {
            return dieSprite.synchSprite;
        }
        return false;
    }
}
