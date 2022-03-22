package fpengine.demofpengine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;

public class Weapon {
    AnimatedSprite weaponSprite;
    Media weaponSound;
    int currentAmmo;
    int maxAmmo = 30;
    double distance;
    boolean synchronization = false;
    int dmg;
    Weapon()
    {
        weaponSprite = new AnimatedSprite(0.200);
        for(int i = 0; i < 5;i++)
        {
            weaponSprite.add(
                    new Sprite(new File("sprites\\weapons\\shotgun\\handshotgun" + i + ".gif").toURI().toString(),300,300,
                            (Controller.width - 300)/2,
                            Controller.height - 300,
                            Color.rgb(152,0,136)

                    )//Hand Drawn Shotgun Sprites

                    //by Z. Franz
            );
        }
        weaponSound = new Media(new File("sounds\\Shotgun.mp3").toURI().toString());
        currentAmmo = 30;
        distance = 4.5;
    }

    Weapon(AnimatedSprite sprite, String soundPathName, int currentAmmo, int maxAmmo, double effectiveDistance)
    {
        weaponSprite = sprite;
        weaponSound = new Media(new File(soundPathName).toURI().toString());
        this.currentAmmo = currentAmmo;
        this.maxAmmo = maxAmmo;
        this.distance = effectiveDistance;
    }
    void shoot(MediaPlayer mediaPlayer)
    {
        synchronization = true;//mamy wlaczona animacje nie mozna jej zatrzymac
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount( weaponSprite.getLength() );
        KeyFrame kf = new KeyFrame(Duration.seconds(weaponSprite.getDuration()),
                event -> {
            if(weaponSprite.pointer < weaponSprite.getLength() - 1)
            {
                weaponSprite.pointer++;
            }
            else
            {
                synchronization = false;
                weaponSprite.pointer = 0;
            }
                });
        gameLoop.getKeyFrames().add( kf );
        gameLoop.play();
        mediaPlayer = new MediaPlayer(weaponSound);
        mediaPlayer.play();
        currentAmmo--;
    }
}
