package com.khovanskiy.snake.client;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import java.time.LocalDate;

/**
 * @author victor
 */
public class DesktopClientApplication {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 800;
        config.height = 600;
        config.title = "Змейка " + LocalDate.now().getYear() + ".1";
        new LwjglApplication(new GameClient(), config);
    }
}
