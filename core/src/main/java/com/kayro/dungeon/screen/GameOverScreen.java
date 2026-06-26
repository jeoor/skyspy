package com.kayro.dungeon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.kayro.dungeon.DungeonForgeGame;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;

public class GameOverScreen extends BaseMenuScreen {
    private static final String[] LEAKS = {
            "这里太白了。",
            "他们都像我。",
            "那东西不是第一次见。",
            "我记得一只猫。",
            "上面有人在等。",
            "他不想让我醒。",
            "我也不想。",
            "那天我没有出声。",
            "猫在我旁边。",
            "该记起来了。"
    };
    private static final int[] LEAK_DEATHS = {1, 2, 4, 6, 8, 10, 12, 15, 17, 20};
    private static final int[] LEAK_FLOORS = {1, 2, 2, 3, 3, 4, 4, 4, 5, 5};

    private final int floor;
    private final int kills;
    private final int highFloor;
    private final int earnedSparks;
    private final int forgeSparks;
    private final int deathCount;
    private final int voidKills;
    private final int relicsFound;
    private final int strongestHit;
    private final Rectangle retryButton = new Rectangle(426f, 250f, 182f, 46f);
    private final Rectangle menuButton = new Rectangle(672f, 250f, 182f, 46f);

    public GameOverScreen(DungeonForgeGame game, int floor, int kills, int gold, int highFloor,
                          int earnedSparks, int forgeSparks, int unlockedWeapons, int totalWeapons,
                          int deathCount) {
        this(game, floor, kills, gold, highFloor, earnedSparks, forgeSparks, unlockedWeapons,
                totalWeapons, deathCount, null);
    }

    public GameOverScreen(DungeonForgeGame game, int floor, int kills, int gold, int highFloor,
                          int earnedSparks, int forgeSparks, int unlockedWeapons, int totalWeapons,
                          int deathCount, GameWorld backgroundWorld) {
        super(game, backgroundWorld);
        this.floor = floor;
        this.kills = kills;
        this.highFloor = highFloor;
        this.earnedSparks = earnedSparks;
        this.forgeSparks = forgeSparks;
        this.deathCount = deathCount;
        this.voidKills = backgroundWorld == null ? 0 : backgroundWorld.voidKills;
        this.relicsFound = backgroundWorld == null ? 0 : backgroundWorld.relicsFound;
        this.strongestHit = backgroundWorld == null ? 0 : backgroundWorld.strongestHit;
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
        drawButtons(retryHovered, menuHovered);
        drawText(retryHovered, menuHovered);
    }

    private void drawButtons(boolean retryHovered, boolean menuHovered) {
        game.shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        drawButton(retryButton, retryHovered);
        drawButton(menuButton, menuHovered);
        game.shapes.end();
    }

    private void drawText(boolean retryHovered, boolean menuHovered) {
        game.batch.begin();
        game.font.getData().setScale(1.55f);
        game.font.setColor(TEXT);
        game.font.draw(game.batch, "记忆泄漏", 0f, Constants.HUD_HEIGHT - 58f,
                Constants.HUD_WIDTH, Align.center, false);

        game.font.getData().setScale(0.90f);
        game.font.setColor(DIM);
        game.font.draw(game.batch, leakLine(), 0f, Constants.HUD_HEIGHT - 126f,
                Constants.HUD_WIDTH, Align.center, false);
        game.font.getData().setScale(1.0f);
        game.font.setColor(TEXT);
        game.font.draw(game.batch, highlightLine(), 0f, 410f, Constants.HUD_WIDTH, Align.center, false);
        game.font.setColor(DIM);
        game.font.draw(game.batch, "层" + floor + "   击" + kills + "   最高 " + highFloor
                        + "   醒念 +" + earnedSparks + "/" + forgeSparks,
                0f, 356f, Constants.HUD_WIDTH, Align.center, false);
        game.font.getData().setScale(0.86f);
        game.font.draw(game.batch, "击落 " + voidKills + "   残留 " + relicsFound + "   最重 " + strongestHit,
                0f, 322f, Constants.HUD_WIDTH, Align.center, false);

        drawButtonLabel(retryButton, "重试", retryHovered);
        drawButtonLabel(menuButton, "菜单", menuHovered);
        game.font.setColor(DIM);
        game.font.draw(game.batch, "R重试     回车菜单",
                0f, 38f, Constants.HUD_WIDTH, Align.center, false);
        game.font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        game.font.getData().setScale(1.0f);
        game.batch.end();
    }

    private String leakLine() {
        int index = 0;
        for (int i = 0; i < LEAKS.length; i++) {
            if (deathCount >= LEAK_DEATHS[i] && highFloor >= LEAK_FLOORS[i]) {
                index = i;
            }
        }
        return LEAKS[index];
    }

    private String highlightLine() {
        if (voidKills >= 4) {
            return "高光：击落 " + voidKills + " 个白影";
        }
        if (strongestHit >= 30) {
            return "高光：最重一击 " + strongestHit;
        }
        if (relicsFound > 0) {
            return "高光：带回 " + relicsFound + " 个残留";
        }
        if (kills > 0) {
            return "高光：穿过 " + kills + " 个阻拦";
        }
        return "高光：又看清一点";
    }
}
