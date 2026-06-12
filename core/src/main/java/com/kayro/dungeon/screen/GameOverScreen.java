package com.kayro.dungeon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Align;
import com.kayro.dungeon.DungeonForgeGame;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.util.Difficulty;

public class GameOverScreen extends ScreenAdapter {
    private final DungeonForgeGame game;
    private final int floor;
    private final int kills;
    private final int gold;
    private final int highFloor;
    private final int earnedSparks;
    private final int forgeSparks;
    private final int unlockedWeapons;
    private final int totalWeapons;
    private final Difficulty difficulty;

    public GameOverScreen(DungeonForgeGame game, int floor, int kills, int gold, int highFloor,
                          int earnedSparks, int forgeSparks, int unlockedWeapons, int totalWeapons) {
        this.game = game;
        this.floor = floor;
        this.kills = kills;
        this.gold = gold;
        this.highFloor = highFloor;
        this.earnedSparks = earnedSparks;
        this.forgeSparks = forgeSparks;
        this.unlockedWeapons = unlockedWeapons;
        this.totalWeapons = totalWeapons;
        this.difficulty = game.difficulty;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.startNewGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.showMainMenu();
            return;
        }

        Gdx.gl.glClearColor(0.04f, 0.01f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.font.getData().setScale(2.0f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Run Ended", 0f, Constants.HUD_HEIGHT - 180f, Constants.HUD_WIDTH, Align.center, false);
        game.font.getData().setScale(1.0f);
        game.font.setColor(difficulty.color);
        game.font.draw(game.batch, difficulty.label,
                0f, Constants.HUD_HEIGHT - 230f, Constants.HUD_WIDTH, Align.center, false);
        game.font.setColor(0.78f, 0.82f, 0.88f, 1f);
        game.font.draw(game.batch, "Floor " + floor + "   Kills " + kills + "   Gold " + gold,
                0f, Constants.HUD_HEIGHT - 260f, Constants.HUD_WIDTH, Align.center, false);
        game.font.draw(game.batch, "Best floor " + highFloor,
                0f, Constants.HUD_HEIGHT - 295f, Constants.HUD_WIDTH, Align.center, false);
        game.font.setColor(Color.GOLD);
        game.font.draw(game.batch, "+" + earnedSparks + " Forge Sparks   Total " + forgeSparks,
                0f, Constants.HUD_HEIGHT - 330f, Constants.HUD_WIDTH, Align.center, false);
        game.font.setColor(0.78f, 0.82f, 0.88f, 1f);
        game.font.draw(game.batch, "Weapons unlocked " + unlockedWeapons + "/" + totalWeapons,
                0f, Constants.HUD_HEIGHT - 365f, Constants.HUD_WIDTH, Align.center, false);
        game.font.draw(game.batch, "Press R to restart   Enter for menu",
                0f, Constants.HUD_HEIGHT - 415f, Constants.HUD_WIDTH, Align.center, false);
        game.batch.end();
    }
}
