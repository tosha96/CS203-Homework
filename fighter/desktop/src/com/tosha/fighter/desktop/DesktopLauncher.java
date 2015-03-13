package com.tosha.fighter.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.tosha.fighter.Fighter;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.title = "Fighter";
                config.resizable = false;
                config.height = 480;
                config.width = 800;
		new LwjglApplication(new Fighter(), config);
	}
}
