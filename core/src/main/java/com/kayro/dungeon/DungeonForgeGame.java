package com.kayro.dungeon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.kayro.dungeon.asset.Assets;
import com.kayro.dungeon.audio.Sfx;
import com.kayro.dungeon.entity.WeaponType;
import com.kayro.dungeon.screen.EndingScreen;
import com.kayro.dungeon.screen.GameOverScreen;
import com.kayro.dungeon.screen.GameScreen;
import com.kayro.dungeon.screen.MainMenuScreen;
import com.kayro.dungeon.util.Difficulty;
import com.kayro.dungeon.world.GameWorld;

public class DungeonForgeGame extends Game {
    private static final String PREFS_NAME = "DungeonForge";
    private static final String HIGH_FLOOR_KEY = "highFloor";
    private static final String FORGE_SPARKS_KEY = "forgeSparks";
    private static final String DEATH_COUNT_KEY = "deathCount";
    private static final String REVIEW_AVAILABLE_KEY = "reviewAvailable";
    private static final String REVIEW_DONE_KEY = "reviewDone";

    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public BitmapFont font;
    public Assets assets;
    public Sfx sfx;
    public Difficulty difficulty = Difficulty.NORMAL;
    private FreeTypeFontGenerator fontGenerator;
    private Preferences preferences;
    private int highFloor;
    private int forgeSparks;
    private int deathCount;
    private boolean reviewAvailable;
    private boolean reviewDone;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = createFont();
        assets = new Assets();
        sfx = new Sfx();
        preferences = Gdx.app.getPreferences(PREFS_NAME);
        highFloor = preferences.getInteger(HIGH_FLOOR_KEY, 1);
        forgeSparks = preferences.getInteger(FORGE_SPARKS_KEY, 0);
        deathCount = preferences.getInteger(DEATH_COUNT_KEY, 0);
        reviewAvailable = preferences.getBoolean(REVIEW_AVAILABLE_KEY, false);
        reviewDone = preferences.getBoolean(REVIEW_DONE_KEY, false);
        showMainMenu();
    }

    public void showMainMenu() {
        setScreen(new MainMenuScreen(this));
    }

    public void startNewGame() {
        setScreen(new GameScreen(this, WeaponType.randomUnlocked(forgeSparks)));
    }

    public void startReviewMode() {
        setScreen(new GameScreen(this, WeaponType.SWORD, true));
    }

    public void showGameOver(int floor, int kills, int gold) {
        showGameOver(floor, kills, gold, null);
    }

    public void showGameOver(int floor, int kills, int gold, GameWorld backgroundWorld) {
        recordFloor(floor);
        deathCount++;
        int earnedSparks = Math.max(1, floor * 2 + kills / 3 + gold / 25);
        forgeSparks += earnedSparks;
        preferences.putInteger(FORGE_SPARKS_KEY, forgeSparks);
        preferences.putInteger(DEATH_COUNT_KEY, deathCount);
        preferences.flush();
        setScreen(new GameOverScreen(this, floor, kills, gold, highFloor, earnedSparks, forgeSparks,
                WeaponType.unlockedCount(forgeSparks), WeaponType.values().length, deathCount, backgroundWorld));
    }

    public void showEnding(int floor, int kills, boolean trueEnding) {
        showEnding(floor, kills, trueEnding, null);
    }

    public void showEnding(int floor, int kills, boolean trueEnding, GameWorld backgroundWorld) {
        recordFloor(floor);
        if (trueEnding && !reviewDone) {
            reviewAvailable = true;
            preferences.putBoolean(REVIEW_AVAILABLE_KEY, true);
            preferences.flush();
        }
        setScreen(new EndingScreen(this, floor, kills, trueEnding, false, backgroundWorld));
    }

    public void showReviewComplete() {
        reviewAvailable = false;
        reviewDone = true;
        preferences.putBoolean(REVIEW_AVAILABLE_KEY, false);
        preferences.putBoolean(REVIEW_DONE_KEY, true);
        preferences.flush();
        setScreen(new EndingScreen(this, 5, 0, true, true));
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

    public int deathCount() {
        return deathCount;
    }

    public boolean reviewAvailable() {
        return reviewAvailable && !reviewDone;
    }

    private BitmapFont createFont() {
        try {
            fontGenerator = new FreeTypeFontGenerator(
                    Gdx.files.internal("fonts/SmileySans-Oblique-2.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 26;
            parameter.incremental = true;
            parameter.spaceX = 0;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            BitmapFont generated = fontGenerator.generateFont(parameter);
            generated.getData().markupEnabled = false;
            return generated;
        } catch (RuntimeException ex) {
            Gdx.app.error("SkySpy", "Failed to load SmileySans font, using default font.", ex);
            return new BitmapFont();
        }
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        batch.dispose();
        shapes.dispose();
        font.dispose();
        if (fontGenerator != null) {
            fontGenerator.dispose();
        }
        assets.dispose();
        sfx.dispose();
    }
}
