package com.kayro.dungeon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayro.dungeon.DungeonForgeGame;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.util.Difficulty;

public class MainMenuScreen extends ScreenAdapter {
    private static final Color PANEL = new Color(0.055f, 0.065f, 0.075f, 0.92f);
    private static final Color PANEL_DARK = new Color(0.025f, 0.030f, 0.038f, 0.94f);
    private static final Color LINE = new Color(0.26f, 0.31f, 0.34f, 1f);
    private static final Color GOLD = new Color(0.96f, 0.70f, 0.28f, 1f);
    private static final Color TEAL = new Color(0.32f, 0.76f, 0.78f, 1f);
    private static final Color RED = new Color(0.78f, 0.22f, 0.18f, 1f);

    private final DungeonForgeGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(Constants.HUD_WIDTH, Constants.HUD_HEIGHT, camera);
    private final Vector2 mouse = new Vector2();
    private final Rectangle startButton = new Rectangle(92f, 116f, 248f, 58f);
    private final Rectangle quitButton = new Rectangle(366f, 116f, 150f, 58f);
    private final Rectangle diffButton = new Rectangle(92f, 192f, 424f, 38f);
    private float timer;

    public MainMenuScreen(DungeonForgeGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        timer += Math.min(delta, 0.05f);
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);

        boolean startHovered = startButton.contains(mouse);
        boolean quitHovered = quitButton.contains(mouse);
        boolean diffHovered = diffButton.contains(mouse);
        boolean click = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        if (click && diffHovered) {
            game.difficulty = game.difficulty.next();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            game.difficulty = game.difficulty.next();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            game.difficulty = game.difficulty.prev();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || (click && startHovered)) {
            game.startNewGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (click && quitHovered)) {
            Gdx.app.exit();
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
        drawPanels(startHovered, quitHovered);
        drawSprites();
        drawText(startHovered, quitHovered);
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
        game.shapes.setColor(0f, 0f, 0f, 0.42f);
        game.shapes.rect(0f, 0f, Constants.HUD_WIDTH, 96f);
        game.shapes.rect(0f, Constants.HUD_HEIGHT - 112f, Constants.HUD_WIDTH, 112f);
        game.shapes.end();

        TextureRegion floor = game.assets.floor;
        TextureRegion wall = game.assets.wall;
        if (floor == null || wall == null) {
            return;
        }
        game.batch.begin();
        game.batch.setColor(0.36f, 0.38f, 0.40f, 0.34f);
        for (int x = 0; x < Constants.HUD_WIDTH; x += 32) {
            for (int y = 0; y < Constants.HUD_HEIGHT; y += 32) {
                TextureRegion region = y < 96 || y > Constants.HUD_HEIGHT - 96 || x < 64
                        || x > Constants.HUD_WIDTH - 96 ? wall : floor;
                game.batch.draw(region, x, y, 32f, 32f);
            }
        }
        game.batch.setColor(Color.WHITE);
        game.batch.end();
    }

    private void drawPanels(boolean startHovered, boolean quitHovered) {
        boolean diffHovered = diffButton.contains(mouse);
        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        rect(64f, 82f, 500f, 548f, PANEL_DARK);
        rect(604f, 126f, 300f, 386f, PANEL);
        rect(932f, 126f, 278f, 386f, PANEL);
        rect(604f, 542f, 606f, 88f, new Color(0.060f, 0.070f, 0.078f, 0.88f));

        rect(diffButton.x, diffButton.y, diffButton.width, diffButton.height,
                diffHovered ? new Color(0.10f, 0.12f, 0.14f, 0.96f) : new Color(0.06f, 0.07f, 0.08f, 0.94f));
        rect(startButton.x, startButton.y, startButton.width, startButton.height,
                startHovered ? new Color(0.82f, 0.48f, 0.18f, 1f) : new Color(0.62f, 0.32f, 0.12f, 1f));
        rect(quitButton.x, quitButton.y, quitButton.width, quitButton.height,
                quitHovered ? new Color(0.34f, 0.38f, 0.42f, 1f) : new Color(0.20f, 0.24f, 0.28f, 1f));

        game.shapes.setColor(LINE);
        game.shapes.rectLine(604f, 512f, 904f, 512f, 2f);
        game.shapes.rectLine(932f, 512f, 1210f, 512f, 2f);
        game.shapes.rectLine(104f, 298f, 524f, 298f, 2f);
        game.shapes.end();
    }

    private void drawSprites() {
        game.batch.begin();
        float bob = MathUtils.sin(timer * 2.4f) * 5f;
        drawRegion(game.assets.chestClosed, 412f, 392f + bob, 54f, 54f, GOLD);
        drawRegion(game.assets.player, 258f, 350f + bob, 64f, 64f, Color.WHITE);
        drawRegion(game.assets.slime, 156f, 250f - bob * 0.4f, 52f, 52f, new Color(0.92f, 1f, 0.90f, 1f));
        drawRegion(game.assets.goblin, 358f, 250f + bob * 0.25f, 52f, 52f, new Color(0.90f, 0.70f, 0.48f, 1f));
        drawRegion(game.assets.skeleton, 458f, 250f - bob * 0.2f, 56f, 56f, new Color(1f, 0.95f, 0.82f, 1f));
        drawRegion(game.assets.arrow, 820f, 560f, 36f, 18f, TEAL);
        drawRegion(game.assets.sword, 1014f, 560f, 38f, 38f, GOLD);
        game.batch.setColor(Color.WHITE);
        game.batch.end();
    }

    private void drawText(boolean startHovered, boolean quitHovered) {
        game.batch.begin();
        game.font.setColor(GOLD);
        game.font.getData().setScale(3.1f);
        game.font.draw(game.batch, "DUNGEONFORGE", 84f, 574f, 460f, Align.left, false);

        game.font.getData().setScale(1.0f);
        game.font.setColor(0.70f, 0.78f, 0.82f, 1f);
        game.font.draw(game.batch, "Real-time dungeon action.",
                88f, 522f, 420f, Align.left, true);

        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(1.35f);
        game.font.draw(game.batch, "Start Run", startButton.x, startButton.y + 38f,
                startButton.width, Align.center, false);
        game.font.draw(game.batch, "Quit", quitButton.x, quitButton.y + 38f,
                quitButton.width, Align.center, false);
        game.font.getData().setScale(1.0f);
        Difficulty diff = game.difficulty;
        game.font.getData().setScale(1.0f);
        game.font.setColor(diff.color);
        game.font.draw(game.batch, "◀  " + diff.label + "  ▶", diffButton.x, diffButton.y + 28f,
                diffButton.width * 0.5f, Align.center, false);
        game.font.setColor(0.62f, 0.70f, 0.76f, 1f);
        game.font.draw(game.batch, diff.description, diffButton.x + diffButton.width * 0.5f, diffButton.y + 28f,
                diffButton.width * 0.5f, Align.center, false);

        game.font.setColor(startHovered || quitHovered ? GOLD : new Color(0.62f, 0.70f, 0.76f, 1f));
        game.font.draw(game.batch, "Enter / Space to start    Esc to quit", 88f, 100f, 430f, Align.left, false);

        drawSection(624f, 486f, "Run Status", GOLD);
        drawLine(624f, 448f, "Best floor", String.valueOf(game.highFloor()));
        drawLine(624f, 410f, "Forge", game.forgeSparks() + " sparks");
        drawLine(624f, 372f, "Weapons", game.unlockedWeaponCount() + "/4");
        drawLine(624f, 334f, "Goal", "Key / boss / stairs");

        drawSection(952f, 486f, "Combat Read", TEAL);
        drawLine(952f, 448f, "Orange", "Charge");
        drawLine(952f, 410f, "Blue", "Shot");
        drawLine(952f, 372f, "Red", "Heavy strike");

        drawSection(624f, 610f, "Controls", Color.WHITE);
        game.font.setColor(0.74f, 0.82f, 0.86f, 1f);
        game.font.draw(game.batch, "WASD Move    J Attack    K Arrow    Q Potion    E Use",
                734f, 610f, 452f, Align.left, false);

        drawSection(104f, 274f, "Relics", TEAL);
        drawTag(104f, 238f, "Crit");
        drawTag(190f, 238f, "Vampire");
        drawTag(300f, 238f, "Pierce");
        drawTag(400f, 238f, "Grip");

        game.font.getData().setScale(1.0f);
        game.font.setColor(Color.WHITE);
        game.batch.end();
    }

    private void drawSection(float x, float y, String text, Color color) {
        game.font.getData().setScale(1.12f);
        game.font.setColor(color);
        game.font.draw(game.batch, text, x, y);
        game.font.getData().setScale(1.0f);
    }

    private void drawLine(float x, float y, String label, String value) {
        game.font.setColor(0.62f, 0.70f, 0.76f, 1f);
        game.font.draw(game.batch, label, x, y);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, value, x + 112f, y);
    }

    private void drawTag(float x, float y, String text) {
        game.font.getData().setScale(0.92f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, text, x, y, 86f, Align.left, false);
        game.font.getData().setScale(1.0f);
    }

    private void rect(float x, float y, float width, float height, Color color) {
        game.shapes.setColor(color);
        game.shapes.rect(x, y, width, height);
        game.shapes.setColor(LINE);
        game.shapes.rectLine(x, y, x + width, y, 2f);
        game.shapes.rectLine(x + width, y, x + width, y + height, 2f);
        game.shapes.rectLine(x + width, y + height, x, y + height, 2f);
        game.shapes.rectLine(x, y + height, x, y, 2f);
    }

    private void drawRegion(TextureRegion region, float centerX, float centerY, float width, float height, Color tint) {
        if (region == null) {
            return;
        }
        game.batch.setColor(tint);
        game.batch.draw(region, centerX - width * 0.5f, centerY - height * 0.5f, width, height);
        game.batch.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
