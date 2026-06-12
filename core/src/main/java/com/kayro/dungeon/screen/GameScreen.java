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
import com.kayro.dungeon.entity.ShopItem;
import com.kayro.dungeon.entity.WeaponType;

public class GameScreen extends ScreenAdapter {
    private static final Color PAUSE_PANEL = new Color(0.055f, 0.065f, 0.075f, 0.94f);
    private static final Color PAUSE_CARD = new Color(0.025f, 0.030f, 0.038f, 0.96f);
    private static final Color PAUSE_LINE = new Color(0.26f, 0.31f, 0.34f, 1f);
    private static final Color PAUSE_GOLD = new Color(0.96f, 0.70f, 0.28f, 1f);
    private static final Color PAUSE_TEAL = new Color(0.32f, 0.76f, 0.78f, 1f);
    private static final Color SHOP_BG = new Color(0.04f, 0.06f, 0.08f, 0.95f);
    private static final Color SHOP_PANEL = new Color(0.065f, 0.075f, 0.085f, 0.96f);
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
    private final Vector2 mouseWorld = new Vector2();
    private final Vector2 mouseHud = new Vector2();
    private final Rectangle pauseResumeButton = new Rectangle(426f, 202f, 182f, 52f);
    private final Rectangle pauseMenuButton = new Rectangle(672f, 202f, 182f, 52f);
    private final Rectangle[] shopButtons = new Rectangle[5];
    private final Rectangle shopCloseButton = new Rectangle(530f, 140f, 220f, 44f);
    private boolean paused;

    public GameScreen(DungeonForgeGame game) {
        this(game, WeaponType.SWORD);
    }

    public GameScreen(DungeonForgeGame game, WeaponType startingWeapon) {
        this.game = game;
        world = new GameWorld(game.sfx, startingWeapon, game.difficulty);
        cameraController = new CameraController(worldCamera, worldViewport);
        cameraController.setBounds(world.map.worldWidth(), world.map.worldHeight());
        cameraController.snapTo(world.player.getCenter());
        for (int i = 0; i < shopButtons.length; i++) {
            shopButtons[i] = new Rectangle(420f, 440f - i * 56f, 440f, 48f);
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
        if (world.shopOpen) {
            renderShopOverlay();
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
        game.font.getData().setScale(1.0f);
        game.font.setColor(game.difficulty.color);
        game.font.draw(game.batch, game.difficulty.label, panelX, panelY + panelH - 74f, panelW, Align.center, false);

        game.font.setColor(0.70f, 0.78f, 0.82f, 1f);
        game.font.draw(game.batch, "Click a button, or use ESC / Enter.",
                panelX, panelY + panelH - 94f, panelW, Align.center, false);

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

    private void renderShopOverlay() {
        mouseHud.set(Gdx.input.getX(), Gdx.input.getY());
        hudViewport.unproject(mouseHud);

        float panelX = 380f;
        float panelY = 100f;
        float panelW = 520f;
        float panelH = 440f;

        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(0f, 0f, 0f, 0.60f);
        game.shapes.rect(0f, 0f, Constants.HUD_WIDTH, Constants.HUD_HEIGHT);
        pauseRect(panelX, panelY, panelW, panelH, SHOP_PANEL);

        for (int i = 0; i < SHOP_ITEMS.length && i < shopButtons.length; i++) {
            Rectangle btn = shopButtons[i];
            boolean hovered = btn.contains(mouseHud);
            ShopItem item = SHOP_ITEMS[i];
            boolean canAfford = world.player.gold >= item.price(world.floor);
            Color btnColor;
            if (!canAfford) {
                btnColor = new Color(0.12f, 0.10f, 0.10f, 0.80f);
            } else if (hovered) {
                btnColor = new Color(0.14f, 0.28f, 0.22f, 0.95f);
            } else {
                btnColor = new Color(0.06f, 0.10f, 0.10f, 0.90f);
            }
            pauseRect(btn.x, btn.y, btn.width, btn.height, btnColor);
        }

        boolean closeHovered = shopCloseButton.contains(mouseHud);
        pauseRect(shopCloseButton.x, shopCloseButton.y, shopCloseButton.width, shopCloseButton.height,
                closeHovered ? new Color(0.34f, 0.38f, 0.42f, 1f) : new Color(0.20f, 0.24f, 0.28f, 1f));
        game.shapes.end();

        game.batch.begin();
        game.font.getData().setScale(1.5f);
        game.font.setColor(PAUSE_TEAL);
        game.font.draw(game.batch, "SHOP", panelX, panelY + panelH - 30f, panelW, Align.center, false);

        game.font.getData().setScale(1.0f);
        game.font.setColor(PAUSE_GOLD);
        game.font.draw(game.batch, "Gold: " + world.player.gold, panelX, panelY + panelH - 58f, panelW, Align.center, false);

        for (int i = 0; i < SHOP_ITEMS.length && i < shopButtons.length; i++) {
            Rectangle btn = shopButtons[i];
            ShopItem item = SHOP_ITEMS[i];
            int price = item.price(world.floor);
            boolean canAfford = world.player.gold >= price;

            game.font.setColor(canAfford ? Color.WHITE : new Color(0.45f, 0.40f, 0.40f, 1f));
            game.font.draw(game.batch, item.label, btn.x + 14f, btn.y + 32f);

            game.font.setColor(canAfford ? new Color(0.62f, 0.70f, 0.76f, 1f) : new Color(0.38f, 0.36f, 0.36f, 1f));
            game.font.draw(game.batch, item.description, btn.x + 160f, btn.y + 32f);

            game.font.setColor(canAfford ? PAUSE_GOLD : new Color(0.55f, 0.30f, 0.25f, 1f));
            game.font.draw(game.batch, price + "g", btn.x + btn.width - 60f, btn.y + 32f);
        }

        game.font.getData().setScale(1.1f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Close [ESC]", shopCloseButton.x, shopCloseButton.y + 30f,
                shopCloseButton.width, Align.center, false);
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
