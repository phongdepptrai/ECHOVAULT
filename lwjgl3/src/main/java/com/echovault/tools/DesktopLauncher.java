package com.echovault.tools;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Game;

public class DesktopLauncher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("EchoVault Animation Tool");
        config.setWindowedMode(1024, 768);

        new Lwjgl3Application(new Game() {
            @Override
            public void create() {
                setScreen(new ToolScreen());
            }
        }, config);
    }
}
