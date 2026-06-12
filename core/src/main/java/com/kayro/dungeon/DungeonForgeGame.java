package com.kayro.dungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kayro.dungeon.asset.Assets;
import com.kayro.dungeon.audio.Sfx;
import com.kayro.dungeon.entity.WeaponType;
import com.kayro.dungeon.screen.GameOverScreen;
import com.kayro.dungeon.screen.GameScreen;
import com.kayro.dungeon.screen.MainMenuScreen;

public class DungeonForgeGame extends Game {
    private static final String PREFS_NAME = "DungeonForge";
    private static final String HIGH_FLOOR_KEY = "highFloor";
    private static final String FORGE_SPARKS_KEY = "forgeSparks";

    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public BitmapFont font;
    public Assets assets;
    public Sfx sfx;
    private Preferences preferences;
    private int highFloor;
    private int forgeSparks;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        assets = new Assets();
        sfx = new Sfx();
        preferences = Gdx.app.getPreferences(PREFS_NAME);
        highFloor = preferences.getInteger(HIGH_FLOOR_KEY, 1);
        forgeSparks = preferences.getInteger(FORGE_SPARKS_KEY, 0);
        showMainMenu();
    }

    public void showMainMenu() {
        setScreen(new MainMenuScreen(this));
    }

    public void startNewGame() {
        setScreen(new GameScreen(this, WeaponType.randomUnlocked(forgeSparks)));
    }

    public void showGameOver(int floor, int kills, int gold) {
        recordFloor(floor);
        int earnedSparks = Math.max(1, floor * 2 + kills / 3 + gold / 25);
        forgeSparks += earnedSparks;
        preferences.putInteger(FORGE_SPARKS_KEY, forgeSparks);
        preferences.flush();
        setScreen(new GameOverScreen(this, floor, kills, gold, highFloor, earnedSparks, forgeSparks,
                WeaponType.unlockedCount(forgeSparks), WeaponType.values().length));
    }

    public void recordFloor(int floor) {
        if (floor <= highFloor) {
            return;
        }
        highFloor = floor;
        preferences.putInteger(HIGH_FLOOR_KEY, highFloor);
        preferences.flush();
    }

    public int highFloor() {
        return highFloor;
    }

    public int forgeSparks() {
        return forgeSparks;
    }

    public int unlockedWeaponCount() {
        return WeaponType.unlockedCount(forgeSparks);
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        batch.dispose();
        shapes.dispose();
        font.dispose();
        assets.dispose();
        sfx.dispose();
    }
}
