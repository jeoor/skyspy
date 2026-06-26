package com.kayro.dungeon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.kayro.dungeon.DungeonForgeGame;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;

public class EndingScreen extends BaseMenuScreen {
    private final int floor;
    private final int kills;
    private final boolean trueEnding;
    private final boolean reviewComplete;
    private final Rectangle retryButton = new Rectangle(426f, 238f, 182f, 46f);
    private final Rectangle menuButton = new Rectangle(672f, 238f, 182f, 46f);

    public EndingScreen(DungeonForgeGame game, int floor, int kills, boolean trueEnding) {
        this(game, floor, kills, trueEnding, false, null);
    }

    public EndingScreen(DungeonForgeGame game, int floor, int kills, boolean trueEnding, boolean reviewComplete) {
        this(game, floor, kills, trueEnding, reviewComplete, null);
    }

    public EndingScreen(DungeonForgeGame game, int floor, int kills, boolean trueEnding,
                        boolean reviewComplete, GameWorld backgroundWorld) {
        super(game, backgroundWorld);
        this.floor = floor;
        this.kills = kills;
        this.trueEnding = trueEnding;
        this.reviewComplete = reviewComplete;
    }

    @Override
    public void render(float delta) {
        updateMouse();
        boolean retryHovered = retryButton.contains(mouse);
        boolean menuHovered = menuButton.contains(mouse);
        boolean click = clicked();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R) || (click && retryHovered)) {
            game.startNewGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || (click && menuHovered)) {
            game.showMainMenu();
            return;
        }

        beginFrame();
        drawBackground();
        drawSceneAndButtons(retryHovered, menuHovered);
        drawText(retryHovered, menuHovered);
    }

    private void drawSceneAndButtons(boolean retryHovered, boolean menuHovered) {
        game.shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        drawTreatmentRoom();
        drawButton(retryButton, retryHovered);
        drawButton(menuButton, menuHovered);
        game.shapes.end();
    }

    private void drawTreatmentRoom() {
        if (!trueEnding && !reviewComplete) {
            return;
        }
        float roomX = 382f;
        float roomY = 176f;
        float roomW = 516f;
        float roomH = 292f;
        game.shapes.setColor(1f, 1f, 1f, 0.46f);
        game.shapes.rect(roomX, roomY, roomW, roomH);
        game.shapes.setColor(0.78f, 0.86f, 0.92f, 0.45f);
        game.shapes.rectLine(roomX, roomY, roomX + roomW, roomY, 2f);
        game.shapes.rectLine(roomX + roomW, roomY, roomX + roomW, roomY + roomH, 2f);
        game.shapes.rectLine(roomX + roomW, roomY + roomH, roomX, roomY + roomH, 2f);
        game.shapes.rectLine(roomX, roomY + roomH, roomX, roomY, 2f);
        game.shapes.setColor(1f, 0.96f, 0.76f, reviewComplete ? 0.46f : 0.34f);
        game.shapes.rect(roomX + 318f, roomY + 150f, 112f, 80f);
        game.shapes.setColor(1f, 0.98f, 0.84f, reviewComplete ? 0.20f : 0.14f);
        game.shapes.triangle(roomX + 430f, roomY + 230f, roomX + 214f, roomY, roomX + 450f, roomY);

        game.shapes.setColor(0.16f, 0.20f, 0.28f, 0.42f);
        drawChair(roomX + 142f, roomY + 78f);
        drawChair(roomX + 340f, roomY + 78f);
        game.shapes.setColor(0.20f, 0.24f, 0.32f, 0.54f);
        game.shapes.circle(roomX + 184f, roomY + 130f, 10f, 10);
        game.shapes.rect(roomX + 177f, roomY + 90f, 14f, 34f);
        if (trueEnding && !reviewComplete) {
            game.shapes.setColor(1f, 1f, 1f, 0.40f);
            game.shapes.ellipse(roomX + 196f, roomY + 86f, 24f, 10f);
        }
    }

    private void drawChair(float x, float y) {
        game.shapes.rect(x, y, 54f, 12f);
        game.shapes.rect(x + 6f, y + 12f, 42f, 38f);
        game.shapes.rect(x + 6f, y - 26f, 5f, 26f);
        game.shapes.rect(x + 43f, y - 26f, 5f, 26f);
    }

    private void drawText(boolean retryHovered, boolean menuHovered) {
        game.batch.begin();
        game.font.getData().setScale(1.55f);
        game.font.setColor(TEXT);
        game.font.draw(game.batch, titleLine(),
                0f, Constants.HUD_HEIGHT - 58f, Constants.HUD_WIDTH, Align.center, false);

        game.font.getData().setScale(0.90f);
        game.font.setColor(DIM);
        game.font.draw(game.batch, mainLine(), 0f, Constants.HUD_HEIGHT - 126f,
                Constants.HUD_WIDTH, Align.center, false);
        game.font.getData().setScale(1.0f);
        game.font.draw(game.batch, secondLine(), 0f, 390f,
                Constants.HUD_WIDTH, Align.center, false);
        game.font.draw(game.batch, "层" + floor + "   击" + kills,
                0f, 340f, Constants.HUD_WIDTH, Align.center, false);

        drawButtonLabel(retryButton, "重试", retryHovered);
        drawButtonLabel(menuButton, "菜单", menuHovered);
        game.font.setColor(DIM);
        game.font.draw(game.batch, "R重试     回车菜单",
                0f, 38f, Constants.HUD_WIDTH, Align.center, false);
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(1.0f);
        game.batch.end();
    }

    private String mainLine() {
        if (reviewComplete) {
            return "整合完成。";
        }
        return trueEnding ? "我看见我了。" : "房间回来了。";
    }

    private String secondLine() {
        if (reviewComplete) {
            return "不用再打了。";
        }
        return trueEnding ? "猫也在。" : "仍有地方是白的。";
    }

    private String titleLine() {
        if (reviewComplete) {
            return "整合";
        }
        return trueEnding ? "我看见我" : "光还在";
    }
}
