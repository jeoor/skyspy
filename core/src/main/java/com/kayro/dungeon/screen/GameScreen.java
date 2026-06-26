package com.kayro.dungeon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kayro.dungeon.DungeonForgeGame;
import com.kayro.dungeon.render.HudRenderer;
import com.kayro.dungeon.render.MinimapRenderer;
import com.kayro.dungeon.render.WorldRenderer;
import com.kayro.dungeon.system.CameraController;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;
import com.kayro.dungeon.entity.ShopItem;
import com.kayro.dungeon.entity.WeaponType;

public class GameScreen extends ScreenAdapter {
    private static final Color PAUSE_GOLD = new Color(0.76f, 0.52f, 0.16f, 1f);
    private static final Color PAUSE_TEAL = new Color(0.08f, 0.48f, 0.56f, 1f);
    private static final Color SHOP_WASH = new Color(0.92f, 0.97f, 1f, 0.36f);
    private static final Color SHOP_PANEL = new Color(1f, 1f, 1f, 0.78f);
    private static final Color SHOP_DISABLED = new Color(0.92f, 0.94f, 0.98f, 0.54f);
    private static final Color SHOP_DISABLED_TEXT = new Color(0.56f, 0.60f, 0.70f, 0.90f);
    private static final Color TEXT_SHADOW = new Color(0.94f, 0.97f, 1f, 0.76f);
    private static final float HUD_MARGIN = 44f;
    private static final float MINIMAP_WIDTH = 174f;
    private static final float MINIMAP_HEIGHT = 112f;
    private static final float SHOP_PANEL_W = 540f;
    private static final float SHOP_PANEL_H = 470f;
    private static final float SHOP_PANEL_X = (Constants.HUD_WIDTH - SHOP_PANEL_W) * 0.5f;
    private static final float SHOP_PANEL_Y = 118f;
    private static final float SHOP_ROW_X = SHOP_PANEL_X + 44f;
    private static final float SHOP_ROW_W = SHOP_PANEL_W - 88f;
    private static final float SHOP_ROW_H = 48f;
    private static final float SHOP_ROW_START_Y = SHOP_PANEL_Y + 304f;
    private static final float SHOP_ROW_GAP = 56f;
    private static final ShopItem[] SHOP_ITEMS = ShopItem.values();

    private final DungeonForgeGame game;
    private final GameWorld world;
    private final OrthographicCamera worldCamera = new OrthographicCamera();
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private final Viewport worldViewport = new FitViewport(Constants.WORLD_VIEW_WIDTH, Constants.WORLD_VIEW_HEIGHT, worldCamera);
    private final Viewport hudViewport = new FitViewport(Constants.HUD_WIDTH, Constants.HUD_HEIGHT, hudCamera);
    private final CameraController cameraController;
    private final WorldRenderer worldRenderer = new WorldRenderer();
    private final HudRenderer hudRenderer = new HudRenderer();
    private final MinimapRenderer minimapRenderer = new MinimapRenderer();
    private final PauseMenuOverlay pauseOverlay;
    private final Vector2 mouseWorld = new Vector2();
    private final Vector2 mouseHud = new Vector2();
    private final Rectangle pauseResumeButton = new Rectangle(426f, 202f, 182f, 52f);
    private final Rectangle pauseMenuButton = new Rectangle(672f, 202f, 182f, 52f);
    private final Rectangle[] shopButtons = new Rectangle[5];
    private final Rectangle shopCloseButton = new Rectangle(
            SHOP_PANEL_X + 160f, SHOP_PANEL_Y + 24f, 220f, 44f);
    private boolean paused;

    public GameScreen(DungeonForgeGame game) {
        this(game, WeaponType.SWORD);
    }

    public GameScreen(DungeonForgeGame game, WeaponType startingWeapon) {
        this(game, startingWeapon, false);
    }

    public GameScreen(DungeonForgeGame game, WeaponType startingWeapon, boolean reviewMode) {
        this.game = game;
        world = new GameWorld(game.sfx, startingWeapon, game.difficulty,
                game.deathCount(), game.highFloor(), reviewMode);
        pauseOverlay = new PauseMenuOverlay(game);
        cameraController = new CameraController(worldCamera, worldViewport);
        cameraController.setBounds(world.map.worldWidth(), world.map.worldHeight());
        cameraController.snapTo(world.player.getCenter());
        for (int i = 0; i < shopButtons.length; i++) {
            shopButtons[i] = new Rectangle(SHOP_ROW_X, SHOP_ROW_START_Y - i * SHOP_ROW_GAP,
                    SHOP_ROW_W, SHOP_ROW_H);
        }
    }

    @Override
    public void render(float delta) {
        boolean resumeHovered = false;
        boolean menuHovered = false;

        if (world.shopOpen) {
            mouseHud.set(Gdx.input.getX(), Gdx.input.getY());
            hudViewport.unproject(mouseHud);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                    || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                world.shopOpen = false;
            }
            boolean shopClick = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
            if (shopClick && shopCloseButton.contains(mouseHud)) {
                world.shopOpen = false;
            }
            if (shopClick) {
                for (int i = 0; i < SHOP_ITEMS.length && i < shopButtons.length; i++) {
                    if (shopButtons[i].contains(mouseHud)) {
                        world.tryBuy(SHOP_ITEMS[i]);
                        break;
                    }
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !world.shopOpen) {
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

        if (!paused && !world.shopOpen) {
            mouseWorld.set(Gdx.input.getX(), Gdx.input.getY());
            worldViewport.unproject(mouseWorld);
            world.input.mouseWorld.set(mouseWorld);
            world.update(delta);
            game.recordFloor(world.floor);
            if (world.runComplete) {
                game.showEnding(world.floor, world.kills, world.trueEnding, world);
                return;
            }
            if (world.reviewComplete) {
                game.showReviewComplete();
                return;
            }
            if (world.gameOver) {
                game.showGameOver(world.floor, world.kills, world.player.gold, world);
                return;
            }
            cameraController.setBounds(world.map.worldWidth(), world.map.worldHeight());
            cameraController.update(world.player.getCenter(), delta, world.cameraShake.x, world.cameraShake.y);
        }

        Gdx.gl.glClearColor(17f / 255f, 45f / 255f, 50f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        worldViewport.apply();
        worldRenderer.render(world, game.shapes, game.batch, game.font, game.assets, worldCamera);

        hudViewport.apply();
        game.batch.setProjectionMatrix(hudCamera.combined);
        game.shapes.setProjectionMatrix(hudCamera.combined);
        hudRenderer.render(world, game.batch, game.shapes, game.font, game.assets, !paused && !world.shopOpen);
        minimapRenderer.render(world, game.shapes, Constants.HUD_WIDTH - HUD_MARGIN - MINIMAP_WIDTH,
                HUD_MARGIN, MINIMAP_WIDTH, MINIMAP_HEIGHT);
        if (!paused && !world.shopOpen) {
            renderStoryOverlay();
        }
        if (paused) {
            renderPauseOverlay(resumeHovered, menuHovered);
        }
        if (world.shopOpen) {
            renderShopOverlay();
        }
    }

    private void renderStoryOverlay() {
        float flashAlpha = world.storyFlashAlpha();
        if (flashAlpha > 0f) {
            game.shapes.begin(ShapeRenderer.ShapeType.Filled);
            game.shapes.setColor(0.94f, 0.97f, 1f, flashAlpha);
            game.shapes.rect(0f, 0f, Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
            game.shapes.end();
        }
        if (!world.hasStoryLine()) {
            return;
        }
        game.batch.begin();
        game.font.getData().setScale(1.08f);
        game.font.setColor(TEXT_SHADOW.r, TEXT_SHADOW.g, TEXT_SHADOW.b, world.storyAlpha() * 0.80f);
        game.font.draw(game.batch, world.storyLine(), 1f, Constants.HUD_HEIGHT * 0.57f - 1f,
                Constants.HUD_WIDTH, Align.center, false);
        game.font.setColor(0.28f, 0.38f, 0.56f, world.storyAlpha());
        game.font.draw(game.batch, world.storyLine(), 0f, Constants.HUD_HEIGHT * 0.57f,
                Constants.HUD_WIDTH, Align.center, false);
        game.font.getData().setScale(1.0f);
        game.font.setColor(Color.WHITE);
        game.batch.end();
    }

    private void renderPauseOverlay(boolean resumeHovered, boolean menuHovered) {
        pauseOverlay.render(world, resumeHovered, menuHovered, pauseResumeButton, pauseMenuButton);
    }

    private void renderShopOverlay() {
        mouseHud.set(Gdx.input.getX(), Gdx.input.getY());
        hudViewport.unproject(mouseHud);

        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(SHOP_WASH);
        game.shapes.rect(0f, 0f, Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        game.shapes.end();

        game.batch.begin();
        if (game.assets.uiVignette != null) {
            game.batch.setColor(1f, 1f, 1f, 0.18f);
            game.batch.draw(game.assets.uiVignette, 0f, 0f, Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
            game.batch.setColor(Color.WHITE);
        }
        game.batch.end();

        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        pauseRect(SHOP_PANEL_X, SHOP_PANEL_Y, SHOP_PANEL_W, SHOP_PANEL_H, SHOP_PANEL);

        for (int i = 0; i < SHOP_ITEMS.length && i < shopButtons.length; i++) {
            Rectangle btn = shopButtons[i];
            boolean hovered = btn.contains(mouseHud);
            ShopItem item = SHOP_ITEMS[i];
            boolean canAfford = world.player.gold >= item.price(world.floor);
            Color btnColor;
            if (!canAfford) {
                btnColor = SHOP_DISABLED;
            } else if (hovered) {
                btnColor = BaseMenuScreen.BUTTON_HOVER;
            } else {
                btnColor = BaseMenuScreen.BUTTON;
            }
            pauseRect(btn.x, btn.y, btn.width, btn.height, btnColor);
        }

        boolean closeHovered = shopCloseButton.contains(mouseHud);
        pauseRect(shopCloseButton.x, shopCloseButton.y, shopCloseButton.width, shopCloseButton.height,
                closeHovered ? BaseMenuScreen.BUTTON_HOVER : BaseMenuScreen.BUTTON);
        game.shapes.end();

        game.batch.begin();
        game.font.getData().setScale(1.32f);
        game.font.setColor(BaseMenuScreen.TEXT);
        game.font.draw(game.batch, "回响", SHOP_PANEL_X, SHOP_PANEL_Y + SHOP_PANEL_H - 42f,
                SHOP_PANEL_W, Align.center, false);

        game.font.getData().setScale(0.78f);
        game.font.setColor(PAUSE_TEAL);
        game.font.draw(game.batch, "持有 " + world.player.gold + " 响", SHOP_PANEL_X,
                SHOP_PANEL_Y + SHOP_PANEL_H - 88f, SHOP_PANEL_W, Align.center, false);

        for (int i = 0; i < SHOP_ITEMS.length && i < shopButtons.length; i++) {
            Rectangle btn = shopButtons[i];
            ShopItem item = SHOP_ITEMS[i];
            int price = item.price(world.floor);
            boolean canAfford = world.player.gold >= price;

            game.font.getData().setScale(0.78f);
            game.font.setColor(canAfford ? BaseMenuScreen.TEXT : SHOP_DISABLED_TEXT);
            game.font.draw(game.batch, item.label, btn.x + 16f, btn.y + 31f);

            game.font.setColor(canAfford ? BaseMenuScreen.DIM : SHOP_DISABLED_TEXT);
            game.font.draw(game.batch, item.description, btn.x + 138f, btn.y + 31f,
                    btn.width - 250f, Align.left, false);

            game.font.setColor(canAfford ? PAUSE_GOLD : new Color(0.55f, 0.30f, 0.25f, 1f));
            game.font.draw(game.batch, price + "响", btn.x + btn.width - 96f, btn.y + 31f,
                    80f, Align.right, false);
        }

        game.font.getData().setScale(1.0f);
        game.font.setColor(BaseMenuScreen.TEXT);
        game.font.draw(game.batch, "关闭", shopCloseButton.x, shopCloseButton.y + 30f,
                shopCloseButton.width, Align.center, false);
        game.font.getData().setScale(1.0f);
        game.batch.end();
    }

    private void pauseRect(float x, float y, float width, float height, Color color) {
        game.shapes.setColor(color);
        game.shapes.rect(x, y, width, height);
        game.shapes.setColor(BaseMenuScreen.BUTTON_LINE);
        game.shapes.rectLine(x, y, x + width, y, 2f);
        game.shapes.rectLine(x + width, y, x + width, y + height, 2f);
        game.shapes.rectLine(x + width, y + height, x, y + height, 2f);
        game.shapes.rectLine(x, y + height, x, y, 2f);
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, false);
        hudViewport.update(width, height, true);
        pauseOverlay.resize(width, height);
    }
}
