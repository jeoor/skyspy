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
import com.kayro.dungeon.render.HudRenderer;
import com.kayro.dungeon.render.WorldRenderer;
import com.kayro.dungeon.system.CameraController;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;
import com.kayro.dungeon.entity.WeaponType;

public class GameScreen extends ScreenAdapter {
    private static final Color PAUSE_PANEL = new Color(0.055f, 0.065f, 0.075f, 0.94f);
    private static final Color PAUSE_CARD = new Color(0.025f, 0.030f, 0.038f, 0.96f);
    private static final Color PAUSE_LINE = new Color(0.26f, 0.31f, 0.34f, 1f);
    private static final Color PAUSE_GOLD = new Color(0.96f, 0.70f, 0.28f, 1f);
    private static final Color PAUSE_TEAL = new Color(0.32f, 0.76f, 0.78f, 1f);

    private final DungeonForgeGame game;
    private final GameWorld world;
    private final OrthographicCamera worldCamera = new OrthographicCamera();
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private final Viewport worldViewport = new FitViewport(Constants.WORLD_VIEW_WIDTH, Constants.WORLD_VIEW_HEIGHT, worldCamera);
    private final Viewport hudViewport = new FitViewport(Constants.HUD_WIDTH, Constants.HUD_HEIGHT, hudCamera);
    private final CameraController cameraController;
    private final WorldRenderer worldRenderer = new WorldRenderer();
    private final HudRenderer hudRenderer = new HudRenderer();
    private final Vector2 mouseWorld = new Vector2();
    private final Vector2 mouseHud = new Vector2();
    private final Rectangle pauseResumeButton = new Rectangle(426f, 202f, 182f, 52f);
    private final Rectangle pauseMenuButton = new Rectangle(672f, 202f, 182f, 52f);
    private boolean paused;

    public GameScreen(DungeonForgeGame game) {
        this(game, WeaponType.SWORD);
    }

    public GameScreen(DungeonForgeGame game, WeaponType startingWeapon) {
        this.game = game;
        world = new GameWorld(game.sfx, startingWeapon);
        cameraController = new CameraController(worldCamera, worldViewport);
        cameraController.setBounds(world.map.worldWidth(), world.map.worldHeight());
        cameraController.snapTo(world.player.getCenter());
    }

    @Override
    public void render(float delta) {
        boolean resumeHovered = false;
        boolean menuHovered = false;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }
        if (paused) {
            mouseHud.set(Gdx.input.getX(), Gdx.input.getY());
            hudViewport.unproject(mouseHud);
            resumeHovered = pauseResumeButton.contains(mouseHud);
            menuHovered = pauseMenuButton.contains(mouseHud);
            boolean click = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || (click && menuHovered)) {
                game.showMainMenu();
                return;
            }
            if (click && resumeHovered) {
                paused = false;
            }
        }

        if (!paused) {
            mouseWorld.set(Gdx.input.getX(), Gdx.input.getY());
            worldViewport.unproject(mouseWorld);
            world.input.mouseWorld.set(mouseWorld);
            world.update(delta);
            game.recordFloor(world.floor);
            if (world.gameOver) {
                game.showGameOver(world.floor, world.kills, world.player.gold);
                return;
            }
            cameraController.setBounds(world.map.worldWidth(), world.map.worldHeight());
            cameraController.update(world.player.getCenter(), delta, world.cameraShake.x, world.cameraShake.y);
        }

        Gdx.gl.glClearColor(0.02f, 0.025f, 0.035f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        worldViewport.apply();
        worldRenderer.render(world, game.shapes, game.batch, game.font, game.assets, worldCamera);

        hudViewport.apply();
        game.batch.setProjectionMatrix(hudCamera.combined);
        game.shapes.setProjectionMatrix(hudCamera.combined);
        hudRenderer.render(world, game.batch, game.shapes, game.font, game.assets);
        if (paused) {
            renderPauseOverlay(resumeHovered, menuHovered);
        }
    }

    private void renderPauseOverlay(boolean resumeHovered, boolean menuHovered) {
        float panelX = 368f;
        float panelY = 156f;
        float panelW = 544f;
        float panelH = 408f;

        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(0f, 0f, 0f, 0.55f);
        game.shapes.rect(0f, 0f, Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        pauseRect(panelX, panelY, panelW, panelH, PAUSE_PANEL);
        pauseRect(panelX + 36f, panelY + 186f, 222f, 116f, PAUSE_CARD);
        pauseRect(panelX + 286f, panelY + 186f, 222f, 116f, PAUSE_CARD);
        pauseRect(panelX + 36f, panelY + 120f, 472f, 48f, PAUSE_CARD);
        pauseRect(pauseResumeButton.x, pauseResumeButton.y, pauseResumeButton.width, pauseResumeButton.height,
                resumeHovered ? new Color(0.82f, 0.48f, 0.18f, 1f) : new Color(0.62f, 0.32f, 0.12f, 1f));
        pauseRect(pauseMenuButton.x, pauseMenuButton.y, pauseMenuButton.width, pauseMenuButton.height,
                menuHovered ? new Color(0.34f, 0.38f, 0.42f, 1f) : new Color(0.20f, 0.24f, 0.28f, 1f));
        game.shapes.end();

        game.batch.begin();
        game.font.getData().setScale(1.7f);
        game.font.setColor(PAUSE_GOLD);
        game.font.draw(game.batch, "PAUSED", panelX, panelY + panelH - 42f, panelW, Align.center, false);

        game.font.getData().setScale(1.0f);
        game.font.setColor(0.70f, 0.78f, 0.82f, 1f);
        game.font.draw(game.batch, "Click a button, or use ESC / Enter.",
                panelX, panelY + panelH - 74f, panelW, Align.center, false);

        float leftX = panelX + 58f;
        float rightX = panelX + 308f;
        drawPauseSection(leftX, panelY + 282f, "Run");
        drawPauseRow(leftX, panelY + 246f, "Floor", String.valueOf(world.floor));
        drawPauseRow(leftX, panelY + 218f, "K / E", world.kills + " / " + world.enemies.size);

        drawPauseSection(rightX, panelY + 282f, "Resources");
        drawPauseIcon(game.assets.heartIcon, rightX, panelY + 236f, 20f);
        drawPauseIcon(game.assets.coin, rightX, panelY + 208f, 20f);
        drawPauseIcon(game.assets.potion, rightX + 78f, panelY + 208f, 20f);
        drawPauseIcon(game.assets.key, rightX + 156f, panelY + 208f, 20f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, world.player.hp + "/" + world.player.maxHp, rightX + 28f, panelY + 252f);
        game.font.draw(game.batch, String.valueOf(world.player.gold), rightX + 28f, panelY + 224f);
        game.font.draw(game.batch, String.valueOf(world.player.potions), rightX + 106f, panelY + 224f);
        game.font.draw(game.batch, String.valueOf(world.player.keys), rightX + 184f, panelY + 224f);

        game.font.setColor(0.72f, 0.80f, 0.84f, 1f);
        game.font.draw(game.batch, "WASD Move     J Attack     K Arrow     Q Potion",
                panelX + 58f, panelY + 150f, 428f, Align.center, false);

        game.font.getData().setScale(1.18f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Resume", pauseResumeButton.x, pauseResumeButton.y + 34f,
                pauseResumeButton.width, Align.center, false);
        game.font.draw(game.batch, "Menu", pauseMenuButton.x, pauseMenuButton.y + 34f,
                pauseMenuButton.width, Align.center, false);
        game.font.getData().setScale(1.0f);
        game.batch.end();
    }

    private void pauseRect(float x, float y, float width, float height, Color color) {
        game.shapes.setColor(color);
        game.shapes.rect(x, y, width, height);
        game.shapes.setColor(PAUSE_LINE);
        game.shapes.rectLine(x, y, x + width, y, 2f);
        game.shapes.rectLine(x + width, y, x + width, y + height, 2f);
        game.shapes.rectLine(x + width, y + height, x, y + height, 2f);
        game.shapes.rectLine(x, y + height, x, y, 2f);
    }

    private void drawPauseSection(float x, float y, String text) {
        game.font.setColor(PAUSE_TEAL);
        game.font.draw(game.batch, text, x, y);
    }

    private void drawPauseRow(float x, float y, String label, String value) {
        game.font.setColor(0.62f, 0.70f, 0.76f, 1f);
        game.font.draw(game.batch, label, x, y);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, value, x + 112f, y);
    }

    private void drawPauseIcon(TextureRegion region, float x, float y, float size) {
        if (region != null) {
            game.batch.draw(region, x, y, size, size);
        }
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, false);
        hudViewport.update(width, height, true);
    }
}
