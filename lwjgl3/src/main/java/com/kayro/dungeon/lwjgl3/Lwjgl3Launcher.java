package com.kayro.dungeon.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.kayro.dungeon.DungeonForgeGame;

import java.io.File;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }

        // 设置工作目录为 exe 所在目录（兼容 jpackage 打包）
        try {
            String exePath = Lwjgl3Launcher.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            File exeDir = new File(exePath).getParentFile();
            if (exeDir != null && new File(exeDir, "assets").exists()) {
                System.setProperty("user.dir", exeDir.getAbsolutePath());
            }
        } catch (Exception e) {
            // 忽略，使用默认工作目录
        }

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("DungeonForge");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(new DungeonForgeGame(), config);
    }
}
