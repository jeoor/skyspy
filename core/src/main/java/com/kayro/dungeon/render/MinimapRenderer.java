package com.kayro.dungeon.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;
import com.kayro.dungeon.world.Tile;
import com.kayro.dungeon.world.TileType;

public class MinimapRenderer {
    public void render(GameWorld world, ShapeRenderer shapes, float x, float y, float width, float height) {
        float scale = Math.min(width / Constants.MAP_WIDTH, height / Constants.MAP_HEIGHT);
        float drawWidth = Constants.MAP_WIDTH * scale;
        float drawHeight = Constants.MAP_HEIGHT * scale;
        float startX = x + (width - drawWidth) * 0.5f;
        float startY = y + (height - drawHeight) * 0.5f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.96f, 0.985f, 1f, 0.34f);
        shapes.rect(x - 8f, y - 8f, width + 16f, height + 16f);
        shapes.setColor(0.58f, 0.68f, 0.82f, 0.42f);
        shapes.rectLine(x - 8f, y - 8f, x + width + 8f, y - 8f, 2f);
        shapes.rectLine(x + width + 8f, y - 8f, x + width + 8f, y + height + 8f, 2f);
        shapes.rectLine(x + width + 8f, y + height + 8f, x - 8f, y + height + 8f, 2f);
        shapes.rectLine(x - 8f, y + height + 8f, x - 8f, y - 8f, 2f);
        for (int tx = 0; tx < Constants.MAP_WIDTH; tx++) {
            for (int ty = 0; ty < Constants.MAP_HEIGHT; ty++) {
                Tile tile = world.map.getTile(tx, ty);
                if (!tile.explored || tile.type == TileType.VOID) {
                    continue;
                }
                if (tile.visible) {
                    shapes.setColor(0.26f, 0.48f, 0.60f, 0.86f);
                } else {
                    shapes.setColor(0.40f, 0.54f, 0.66f, 0.46f);
                }
                shapes.rect(startX + tx * scale, startY + ty * scale, Math.max(1f, scale), Math.max(1f, scale));
            }
        }

        drawPoint(shapes, startX, startY, scale, world.map.worldToTileX(world.map.stairsPosition.x),
                world.map.worldToTileY(world.map.stairsPosition.y),
                world.exitReady() ? 0.08f : 1f, world.exitReady() ? 0.92f : 0.76f,
                world.exitReady() ? 0.96f : 0.20f, 8f);
        for (Chest chest : world.chests) {
            if (chest.opened) {
                continue;
            }
            int cx = world.map.worldToTileX(chest.getCenter().x);
            int cy = world.map.worldToTileY(chest.getCenter().y);
            if (world.map.getTile(cx, cy).explored) {
                drawPoint(shapes, startX, startY, scale, cx, cy, 1f, 0.72f, 0.18f, 5f);
            }
        }
        for (Enemy enemy : world.enemies) {
            int ex = world.map.worldToTileX(enemy.getCenter().x);
            int ey = world.map.worldToTileY(enemy.getCenter().y);
            if (world.map.getTile(ex, ey).visible) {
                drawPoint(shapes, startX, startY, scale, ex, ey, 0.92f, 0.18f, 0.16f, 5f);
            }
        }
        drawPoint(shapes, startX, startY, scale, world.map.worldToTileX(world.player.getCenter().x),
                world.map.worldToTileY(world.player.getCenter().y), 0.10f, 0.20f, 0.86f, 6f);
        shapes.end();
    }

    private void drawPoint(ShapeRenderer shapes, float startX, float startY, float scale, int tileX, int tileY,
                           float r, float g, float b, float size) {
        shapes.setColor(r, g, b, 1f);
        shapes.rect(startX + tileX * scale - size * 0.5f, startY + tileY * scale - size * 0.5f, size, size);
    }
}
