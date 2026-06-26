package com.kayro.dungeon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayro.dungeon.DungeonForgeGame;
import com.kayro.dungeon.entity.WeaponType;
import com.kayro.dungeon.render.WorldRenderer;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;

public abstract class BaseMenuScreen extends ScreenAdapter {
    protected static final Color CLEAR = new Color(0.90f, 0.95f, 1f, 1f);
    protected static final Color TEXT = new Color(0.07f, 0.10f, 0.17f, 1f);
    protected static final Color DIM = new Color(0.42f, 0.50f, 0.66f, 1f);
    protected static final Color WHITE = new Color(0.98f, 0.99f, 1f, 1f);
    protected static final Color BUTTON = new Color(1f, 1f, 1f, 0.74f);
    protected static final Color BUTTON_HOVER = new Color(1f, 1f, 1f, 0.96f);
    protected static final Color BUTTON_LINE = new Color(0.68f, 0.76f, 0.88f, 0.88f);
    protected static final Color WASH = new Color(0.92f, 0.97f, 1f, 0.76f);

    protected final DungeonForgeGame game;
    protected final OrthographicCamera camera = new OrthographicCamera();
    protected final Viewport viewport = new FitViewport(Constants.HUD_WIDTH, Constants.HUD_HEIGHT, camera);
    private final OrthographicCamera previewCamera = new OrthographicCamera();
    private final Viewport previewViewport = new FitViewport(Constants.WORLD_VIEW_WIDTH, Constants.WORLD_VIEW_HEIGHT, previewCamera);
    private final WorldRenderer previewRenderer = new WorldRenderer();
    protected final Vector2 mouse = new Vector2();
    private GameWorld previewWorld;

    protected BaseMenuScreen(DungeonForgeGame game) {
        this(game, null);
    }

    protected BaseMenuScreen(DungeonForgeGame game, GameWorld backgroundWorld) {
        this.game = game;
        this.previewWorld = backgroundWorld;
    }

    protected void beginFrame() {
        Gdx.gl.glClearColor(CLEAR.r, CLEAR.g, CLEAR.b, CLEAR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        applyViewport();
    }

    protected void applyViewport() {
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapes.setProjectionMatrix(camera.combined);
    }

    protected void updateMouse() {
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);
    }

    protected boolean clicked() {
        return Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
    }

    protected void drawBackground() {
        drawPreviewWorld();
        drawOverlayWash(0.10f);
    }

    private void drawPreviewWorld() {
        ensurePreviewWorld();
        Vector2 center = previewWorld.player.getCenter();
        previewCamera.position.set(center.x, center.y + 28f, 0f);
        previewCamera.update();
        previewViewport.apply();
        previewRenderer.render(previewWorld, game.shapes, game.batch, game.font, game.assets, previewCamera);
        applyViewport();
    }

    private void ensurePreviewWorld() {
        if (previewWorld != null) {
            return;
        }
        previewWorld = new GameWorld(null, WeaponType.SWORD, game.difficulty,
                game.deathCount(), game.highFloor(), false);
    }

    protected void drawOverlayWash(float alpha) {
        if (alpha > 0f) {
            game.shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
            game.shapes.setColor(WASH.r, WASH.g, WASH.b, alpha);
            game.shapes.rect(0f, 0f, Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
            game.shapes.end();
        }

        game.batch.begin();
        drawVignette(0.16f);
        game.batch.setColor(Color.WHITE);
        game.batch.end();
    }

    private void drawVignette(float alpha) {
        if (game.assets.uiVignette != null) {
            game.batch.setColor(1f, 1f, 1f, alpha);
            game.batch.draw(game.assets.uiVignette, 0f, 0f, Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        }
    }

    protected void drawButton(Rectangle button, boolean hovered) {
        game.shapes.setColor(hovered ? BUTTON_HOVER : BUTTON);
        game.shapes.rect(button.x, button.y, button.width, button.height);
        game.shapes.setColor(BUTTON_LINE);
        game.shapes.rectLine(button.x, button.y, button.x + button.width, button.y, 2f);
        game.shapes.rectLine(button.x + button.width, button.y, button.x + button.width, button.y + button.height, 2f);
        game.shapes.rectLine(button.x + button.width, button.y + button.height, button.x, button.y + button.height, 2f);
        game.shapes.rectLine(button.x, button.y + button.height, button.x, button.y, 2f);
    }

    protected void drawButtonLabel(Rectangle button, String label, boolean hovered) {
        game.font.setColor(hovered ? TEXT : DIM);
        game.font.draw(game.batch, label, button.x, button.y + button.height * 0.64f,
                button.width, Align.center, false);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        previewViewport.update(width, height, false);
    }
}
