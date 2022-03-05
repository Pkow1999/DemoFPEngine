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

public class EnemySprite {
    public static int numberOfEnemies = 0;
    int status = 0;
    private int health;
    private double positionX;
    private double positionY;
    private double velocityX = 0;
    private double velocityY = 0;
    private ArrayList<Sprite> standSprite;
    private AnimatedSprite dieSprite;
    private AnimatedSprite painSprite;
    private AnimatedSprite shootSprite;
    private ArrayList<AnimatedSprite> walkSprite;
    private Media painSound;
    private Media deathSound;
    private Media weaponEnemySound;
    boolean toRemove = false;
    EnemySprite(double posX, double posY)
    {
        numberOfEnemies++;
        health = 50;
        positionX = posX;
        positionY = posY;
        standSprite = new ArrayList<>();
        for(int i = 1; i < 9; i++)//caly obrot postaci
        {
            standSprite.add(new Sprite(new File("sprites\\wsjheerpack\\mguard_s_1.bmp").toURI().toString(),positionX,positionY,Color.rgb(152,0,136)));
        }

        dieSprite = new AnimatedSprite(0.150);
        for(int i = 1; i <= 4;i++)
        {
            dieSprite.add(
                    new Sprite(new File("sprites\\wsjheerpack\\mguard_die" + i + ".bmp").toURI().toString(),
                            standSprite.get(0).getPositionX(),standSprite.get(0).getPositionY(),
                            Color.rgb(152,0,136)

                    )
            );
        }

        painSprite = new AnimatedSprite(0.100);
        for(int i = 1; i <= 2;i++)
        {
            painSprite.add(
                    new Sprite(new File("sprites\\wsjheerpack\\mguard_pain" + i + ".bmp").toURI().toString(),
                            standSprite.get(0).getPositionX(),standSprite.get(0).getPositionY(),
                            Color.rgb(152,0,136)

                    )
            );
        }

        shootSprite = new AnimatedSprite(0.100);
        for(int i = 1; i <= 3;i++)
        {
            shootSprite.add(
                    new Sprite(new File("sprites\\wsjheerpack\\mguard_shoot" + i + ".bmp").toURI().toString(),
                            standSprite.get(0).getPositionX(),standSprite.get(0).getPositionY(),
                            Color.rgb(152,0,136)

                    )
            );
        }
        walkSprite = new ArrayList<>();
        for(int i = 1; i <= 7; i++)//caly obrot postaci
        {
            walkSprite.add(new AnimatedSprite(0.100));
            for(int j = 1; j <= 4; j++)
            {
                walkSprite.get(i - 1).add(new Sprite(new File("sprites\\wsjheerpack\\mguard_w" + j + "_" + i + ".bmp").toURI().toString(),
                        standSprite.get(0).getPositionX(), standSprite.get(0).getPositionY(), Color.rgb(152,0, 136)));
            }
        }
        painSound = new Media(new File("sounds\\EnemyPain.wav").toURI().toString());
        deathSound = new Media(new File("sounds\\Death" + (int)(Math.random() * (2 - 1 + 1) + 1) + ".wav").toURI().toString());
        weaponEnemySound = new Media(new File("sounds\\Pistol.wav").toURI().toString());
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
        health = health - dmg;
        if(health <= 0)
        {
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
    public void hurting()
    {
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( 2 );
        AtomicInteger i = new AtomicInteger();
        KeyFrame kf = new KeyFrame(Duration.seconds(0.1),
                event -> {
                    if(i.incrementAndGet() > 1)
                        status = 0;
                });
        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();
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
    public Sprite getCurrentSprite()
    {
        if(status == 0)
        {
            return standSprite.get(0);
        }
        else if(status == 1)
        {
            return shootSprite.getFrame(shootSprite.pointer);
        }
        else if(status == 2)
        {
            return painSprite.getFrame(painSprite.pointer);
        }
        else
        {
            return dieSprite.getFrame(dieSprite.pointer);
        }
    }
    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public double getHeight()
    {
        return standSprite.get(0).getHeight();
    }

    public double getWidth()
    {
        return standSprite.get(0).getWidth();
    }
}
