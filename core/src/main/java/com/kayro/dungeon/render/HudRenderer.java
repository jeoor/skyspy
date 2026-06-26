package com.kayro.dungeon.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.kayro.dungeon.asset.GameAssets;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;

public class HudRenderer {
    private static final Color TEXT = new Color(0.06f, 0.10f, 0.18f, 1f);
    private static final Color DIM = new Color(0.30f, 0.38f, 0.52f, 1f);
    private static final Color TRACK = new Color(0.78f, 0.86f, 0.95f, 0.76f);
    private static final Color ACCENT = new Color(0.02f, 0.62f, 0.70f, 1f);
    private static final Color RED = new Color(0.90f, 0.06f, 0.08f, 1f);
    private static final Color BLUE = new Color(0.20f, 0.45f, 0.95f, 1f);
    private static final Color SHADOW = new Color(0.94f, 0.97f, 1f, 0.70f);
    private static final float MARGIN = 44f;
    private static final float BAR_X = MARGIN + 32f;
    private static final float BAR_WIDTH = 142f;

    public void render(GameWorld world, SpriteBatch batch, ShapeRenderer shapes, BitmapFont font, GameAssets assets,
                       boolean showControlHint) {
        float hpRatio = world.player.hp / (float)world.player.maxHp;
        float expRatio = world.player.exp / (float)world.levelSystem.expToNext(world.player);
        float top = Constants.HUD_HEIGHT - MARGIN;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(TRACK);
        shapes.rect(BAR_X, top - 25f, BAR_WIDTH, 8f);
        shapes.setColor(RED);
        shapes.rect(BAR_X, top - 25f, BAR_WIDTH * hpRatio, 8f);
        shapes.setColor(TRACK);
        shapes.rect(BAR_X, top - 48f, BAR_WIDTH, 7f);
        shapes.setColor(BLUE);
        shapes.rect(BAR_X, top - 48f, BAR_WIDTH * expRatio, 7f);
        shapes.end();

        batch.begin();
        font.getData().setScale(0.82f);
        drawText(font, batch, world.reviewMode ? "回顾" : "阶 " + world.floor, MARGIN, top + 10f, TEXT);
        drawIcon(batch, assets.heartIcon, MARGIN, top - 30f, 14f);
        drawIcon(batch, assets.expIcon, MARGIN, top - 53f, 14f);
        drawText(font, batch, world.player.hp + "/" + world.player.maxHp,
                BAR_X + BAR_WIDTH + 12f, top - 18f, TEXT);
        drawText(font, batch, world.player.exp + "/" + world.levelSystem.expToNext(world.player),
                BAR_X + BAR_WIDTH + 12f, top - 41f, DIM);

        float rightX = Constants.HUD_WIDTH - MARGIN - 246f;
        drawIcon(batch, assets.key, rightX, top - 6f, 18f);
        drawIcon(batch, assets.potion, rightX + 86f, top - 5f, 16f);
        drawIcon(batch, assets.coin, rightX + 168f, top - 5f, 16f);
        font.getData().setScale(0.86f);
        drawText(font, batch, String.valueOf(world.player.keys), rightX + 26f, top + 9f, TEXT);
        drawText(font, batch, String.valueOf(world.player.potions), rightX + 110f, top + 9f, TEXT);
        drawText(font, batch, String.valueOf(world.player.gold), rightX + 192f, top + 9f, TEXT);
        font.getData().setScale(0.70f);
        drawText(font, batch, "碎片", rightX, top - 16f, DIM);
        drawText(font, batch, "修补", rightX + 84f, top - 16f, DIM);
        drawText(font, batch, "回响", rightX + 166f, top - 16f, DIM);

        font.getData().setScale(0.78f);
        Color riftColor = world.reviewMode || world.exitReady() ? ACCENT : DIM;
        drawText(font, batch, world.reviewMode ? world.reviewFloorComplete() ? "裂隙可用" : "安抚中"
                        : world.exitReady() ? "裂隙可用" : "找碎片",
                rightX, top - 43f, riftColor);
        drawText(font, batch, world.player.dashCooldownTimer <= 0f ? "冲刺就绪" : "冲刺冷却",
                rightX + 118f, top - 43f, world.player.dashCooldownTimer <= 0f ? ACCENT : DIM);

        if (showControlHint) {
            font.getData().setScale(0.76f);
            drawCenteredText(font, batch, world.reviewMode ? "WASD 移动   E安抚   ESC"
                            : "WASD 移动   Shift 冲刺   鼠标射击   Q药   E交互   ESC",
                    24f, DIM);
        }
        font.setColor(Color.WHITE);
        font.getData().setScale(1.0f);
        batch.end();
    }

    private void drawIcon(SpriteBatch batch, TextureRegion region, float x, float y, float size) {
        if (region != null) {
            batch.draw(region, x, y, size, size);
        }
    }

    private void drawText(BitmapFont font, SpriteBatch batch, String text, float x, float y, Color color) {
        font.setColor(SHADOW);
        font.draw(batch, text, x + 1f, y - 1f);
        font.setColor(color);
        font.draw(batch, text, x, y);
    }

    private void drawCenteredText(BitmapFont font, SpriteBatch batch, String text, float y, Color color) {
        font.setColor(SHADOW);
        font.draw(batch, text, 1f, y - 1f, Constants.HUD_WIDTH, Align.center, false);
        font.setColor(color);
        font.draw(batch, text, 0f, y, Constants.HUD_WIDTH, Align.center, false);
    }
}
