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
    private final MinimapRenderer minimapRenderer = new MinimapRenderer();

    public void render(GameWorld world, SpriteBatch batch, ShapeRenderer shapes, BitmapFont font, GameAssets assets) {
        float expRatio = world.player.exp / (float)world.levelSystem.expToNext(world.player);
        float barX = 66f;
        float barWidth = 170f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.rect(18f, Constants.HUD_HEIGHT - 108f, 420f, 88f);
        shapes.setColor(0.22f, 0.04f, 0.05f, 1f);
        shapes.rect(barX, Constants.HUD_HEIGHT - 52f, barWidth, 16f);
        shapes.setColor(0.75f, 0.08f, 0.10f, 1f);
        shapes.rect(barX, Constants.HUD_HEIGHT - 52f, barWidth * world.player.hp / (float)world.player.maxHp, 16f);
        shapes.setColor(0.05f, 0.08f, 0.18f, 1f);
        shapes.rect(barX, Constants.HUD_HEIGHT - 80f, barWidth, 12f);
        shapes.setColor(0.20f, 0.45f, 0.95f, 1f);
        shapes.rect(barX, Constants.HUD_HEIGHT - 80f, barWidth * expRatio, 12f);
        minimapRenderer.render(world, shapes, Constants.HUD_WIDTH - 248f, 54f, 220f, 160f);
        shapes.end();

        batch.begin();
        drawIcon(batch, assets.heartIcon, 32f, Constants.HUD_HEIGHT - 57f, 22f);
        drawIcon(batch, assets.expIcon, 32f, Constants.HUD_HEIGHT - 86f, 22f);
        drawIcon(batch, assets.sword, 262f, Constants.HUD_HEIGHT - 62f, 20f);
        drawIcon(batch, assets.shieldIcon, 330f, Constants.HUD_HEIGHT - 62f, 20f);
        drawIcon(batch, assets.coin, 944f, Constants.HUD_HEIGHT - 42f, 20f);
        drawIcon(batch, assets.potion, 1018f, Constants.HUD_HEIGHT - 42f, 20f);
        drawIcon(batch, assets.key, 1110f, Constants.HUD_HEIGHT - 42f, 20f);
        drawIcon(batch, assets.arrow, 944f, Constants.HUD_HEIGHT - 68f, 18f);

        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);
        font.draw(batch, world.player.hp + "/" + world.player.maxHp, barX + 8f, Constants.HUD_HEIGHT - 58f);
        font.draw(batch, world.player.exp + "/" + world.levelSystem.expToNext(world.player), barX + 8f,
                Constants.HUD_HEIGHT - 84f);
        font.draw(batch, String.valueOf(world.player.attackDamage()), 286f, Constants.HUD_HEIGHT - 47f);
        font.draw(batch, String.valueOf(world.player.defense), 354f, Constants.HUD_HEIGHT - 47f);
        font.draw(batch, "Lv " + world.player.level + "  " + world.player.weapon.label + "  R" + world.player.relics.size,
                262f, Constants.HUD_HEIGHT - 76f);
        font.draw(batch, "F" + world.floor + " " + world.biome.label + "  E" + world.enemies.size
                        + "  K" + world.kills,
                32f, Constants.HUD_HEIGHT - 24f);
        font.draw(batch, String.valueOf(world.player.gold), 968f, Constants.HUD_HEIGHT - 24f);
        font.draw(batch, String.valueOf(world.player.potions), 1042f, Constants.HUD_HEIGHT - 24f);
        font.draw(batch, String.valueOf(world.player.keys), 1134f, Constants.HUD_HEIGHT - 24f);
        String skillText = world.player.skillCooldownTimer > 0f
                ? String.format("%.1fs", world.player.skillCooldownTimer)
                : "Ready";
        font.draw(batch, skillText, 968f, Constants.HUD_HEIGHT - 50f);
        String dashText = world.player.dashCooldownTimer > 0f
                ? "Dash " + String.format("%.1fs", world.player.dashCooldownTimer)
                : "Dash Ready";
        font.draw(batch, dashText, 944f, Constants.HUD_HEIGHT - 76f);
        font.setColor(world.exitReady() ? Color.GOLD : Color.LIGHT_GRAY);
        font.draw(batch, objectiveText(world), 944f, Constants.HUD_HEIGHT - 102f);
        font.setColor(0.72f, 0.78f, 0.86f, 1f);
        font.draw(batch, "WASD Move   Shift Dash   J/LMB Attack   K/RMB Arrow   Q Potion   E Pickup/Stairs   ESC Pause",
                0f, 34f, Constants.HUD_WIDTH, Align.center, false);
        batch.end();
    }

    private void drawIcon(SpriteBatch batch, TextureRegion region, float x, float y, float size) {
        if (region != null) {
            batch.draw(region, x, y, size, size);
        }
    }

    private String objectiveText(GameWorld world) {
        if (world.hasLiveBoss()) {
            return "Goal: defeat boss";
        }
        if (world.player.keys <= 0) {
            return "Goal: find a key";
        }
        return "Goal: use stairs";
    }
}
