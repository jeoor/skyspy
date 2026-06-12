package com.kayro.dungeon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayro.dungeon.DungeonForgeGame;
import com.kayro.dungeon.util.Constants;

public class GameOverScreen extends ScreenAdapter {
    private static final Color PANEL = new Color(0.055f, 0.065f, 0.075f, 0.94f);
    private static final Color CARD = new Color(0.025f, 0.030f, 0.038f, 0.96f);
    private static final Color LINE = new Color(0.26f, 0.31f, 0.34f, 1f);
    private static final Color GOLD = new Color(0.96f, 0.70f, 0.28f, 1f);
    private static final Color TEAL = new Color(0.32f, 0.76f, 0.78f, 1f);

    private final DungeonForgeGame game;
    private final int floor;
    private final int kills;
    private final int gold;
    private final int highFloor;
    private final int earnedSparks;
    private final int forgeSparks;
    private final int unlockedWeapons;
    private final int totalWeapons;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(Constants.HUD_WIDTH, Constants.HUD_HEIGHT, camera);
    private final Vector2 mouse = new Vector2();
    private final Rectangle restartButton = new Rectangle(426f, 174f, 182f, 52f);
    private final Rectangle menuButton = new Rectangle(672f, 174f, 182f, 52f);

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
    }

    @Override
    public void render(float delta) {
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);
        boolean restartHovered = restartButton.contains(mouse);
        boolean menuHovered = menuButton.contains(mouse);
        boolean click = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) || (click && restartHovered)) {
            game.startNewGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || (click && menuHovered)) {
            game.showMainMenu();
            return;
        }

        Gdx.gl.glClearColor(0.018f, 0.022f, 0.028f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapes.setProjectionMatrix(camera.combined);

        drawDungeonBackdrop();
        drawPanel(restartHovered, menuHovered);
        drawText();
    }

    private void drawDungeonBackdrop() {
        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(0.018f, 0.022f, 0.028f, 1f);
        game.shapes.rect(0f, 0f, Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        game.shapes.setColor(0.055f, 0.064f, 0.072f, 1f);
        for (int x = 0; x < Constants.HUD_WIDTH; x += 64) {
            game.shapes.rect(x, 0f, 2f, Constants.HUD_HEIGHT);
        }
        for (int y = 0; y < Constants.HUD_HEIGHT; y += 64) {
            game.shapes.rect(0f, y, Constants.HUD_WIDTH, 2f);
        }
        game.shapes.setColor(0f, 0f, 0f, 0.52f);
        game.shapes.rect(0f, 0f, Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        game.shapes.end();

        TextureRegion floorRegion = game.assets.floor;
        TextureRegion wallRegion = game.assets.wall;
        if (floorRegion == null || wallRegion == null) {
            return;
        }
        game.batch.begin();
        game.batch.setColor(0.36f, 0.38f, 0.40f, 0.24f);
        for (int x = 0; x < Constants.HUD_WIDTH; x += 32) {
            for (int y = 0; y < Constants.HUD_HEIGHT; y += 32) {
                TextureRegion region = y < 96 || y > Constants.HUD_HEIGHT - 96 || x < 64
                        || x > Constants.HUD_WIDTH - 96 ? wallRegion : floorRegion;
                game.batch.draw(region, x, y, 32f, 32f);
            }
        }
        game.batch.setColor(Color.WHITE);
        game.batch.end();
    }

    private void drawPanel(boolean restartHovered, boolean menuHovered) {
        float panelX = 368f;
        float panelY = 128f;
        float panelW = 544f;
        float panelH = 464f;

        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        uiRect(panelX, panelY, panelW, panelH, PANEL);
        uiRect(panelX + 36f, panelY + 176f, 222f, 104f, CARD);
        uiRect(panelX + 286f, panelY + 176f, 222f, 104f, CARD);
        uiRect(panelX + 36f, panelY + 112f, 472f, 46f, CARD);
        uiRect(restartButton.x, restartButton.y, restartButton.width, restartButton.height,
                restartHovered ? new Color(0.82f, 0.48f, 0.18f, 1f) : new Color(0.62f, 0.32f, 0.12f, 1f));
        uiRect(menuButton.x, menuButton.y, menuButton.width, menuButton.height,
                menuHovered ? new Color(0.34f, 0.38f, 0.42f, 1f) : new Color(0.20f, 0.24f, 0.28f, 1f));
        game.shapes.end();

        game.batch.begin();
        drawRegion(game.assets.emptyHeartGauge, panelX + 152f, panelY + 300f, 240f, 96f, Color.WHITE);
        game.batch.end();
    }

    private void drawText() {
        float panelX = 368f;
        float panelY = 128f;
        float panelW = 544f;

        game.batch.begin();
        game.font.getData().setScale(1.7f);
        game.font.setColor(GOLD);
        game.font.draw(game.batch, "RUN ENDED", panelX, panelY + 426f, panelW, Align.center, false);

        game.font.getData().setScale(1.0f);
        game.font.setColor(0.70f, 0.78f, 0.82f, 1f);
        game.font.draw(game.batch, "The forge keeps what the dungeon takes.",
                panelX, panelY + 396f, panelW, Align.center, false);

        float leftX = panelX + 58f;
        float rightX = panelX + 308f;
        drawSection(leftX, panelY + 260f, "Run");
        drawRow(leftX, panelY + 226f, "Floor", String.valueOf(floor));
        drawRow(leftX, panelY + 198f, "Kills", String.valueOf(kills));
        drawIcon(game.assets.coin, leftX, panelY + 174f, 18f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, String.valueOf(gold), leftX + 28f, panelY + 190f);

        drawSection(rightX, panelY + 260f, "Forge");
        drawRow(rightX, panelY + 226f, "Best", String.valueOf(highFloor));
        drawRow(rightX, panelY + 198f, "Sparks", "+" + earnedSparks);
        drawRow(rightX, panelY + 170f, "Weapons", unlockedWeapons + "/" + totalWeapons);

        game.font.setColor(GOLD);
        game.font.draw(game.batch, "Total Sparks " + forgeSparks,
                panelX + 58f, panelY + 140f, 428f, Align.center, false);

        game.font.getData().setScale(1.18f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Restart", restartButton.x, restartButton.y + 34f,
                restartButton.width, Align.center, false);
        game.font.draw(game.batch, "Menu", menuButton.x, menuButton.y + 34f,
                menuButton.width, Align.center, false);

        game.font.getData().setScale(1.0f);
        game.font.setColor(0.62f, 0.70f, 0.76f, 1f);
        game.font.draw(game.batch, "R restart    Enter menu", panelX, panelY + 36f, panelW, Align.center, false);
        game.batch.end();
    }

    private void uiRect(float x, float y, float width, float height, Color color) {
        game.shapes.setColor(color);
        game.shapes.rect(x, y, width, height);
        game.shapes.setColor(LINE);
        game.shapes.rectLine(x, y, x + width, y, 2f);
        game.shapes.rectLine(x + width, y, x + width, y + height, 2f);
        game.shapes.rectLine(x + width, y + height, x, y + height, 2f);
        game.shapes.rectLine(x, y + height, x, y, 2f);
    }

    private void drawSection(float x, float y, String text) {
        game.font.setColor(TEAL);
        game.font.draw(game.batch, text, x, y);
    }

    private void drawRow(float x, float y, String label, String value) {
        game.font.setColor(0.62f, 0.70f, 0.76f, 1f);
        game.font.draw(game.batch, label, x, y);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, value, x + 92f, y);
    }

    private void drawIcon(TextureRegion region, float x, float y, float size) {
        if (region != null) {
            game.batch.draw(region, x, y, size, size);
        }
    }

    private void drawRegion(TextureRegion region, float x, float y, float width, float height, Color tint) {
        if (region == null) {
            return;
        }
        game.batch.setColor(tint);
        game.batch.draw(region, x, y, width, height);
        game.batch.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
