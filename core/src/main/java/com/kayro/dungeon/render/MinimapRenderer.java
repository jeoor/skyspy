package com.kayro.dungeon.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.Trap;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;
import com.kayro.dungeon.world.Tile;
import com.kayro.dungeon.world.TileType;

public class MinimapRenderer {
    public void render(GameWorld world, ShapeRenderer shapes, float x, float y, float width, float height) {
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(x - 8f, y - 8f, width + 16f, height + 16f);

        float scale = Math.min(width / Constants.MAP_WIDTH, height / Constants.MAP_HEIGHT);
        float drawWidth = Constants.MAP_WIDTH * scale;
        float drawHeight = Constants.MAP_HEIGHT * scale;
        float startX = x + (width - drawWidth) * 0.5f;
        float startY = y + (height - drawHeight) * 0.5f;

        for (int tx = 0; tx < Constants.MAP_WIDTH; tx++) {
            for (int ty = 0; ty < Constants.MAP_HEIGHT; ty++) {
                Tile tile = world.map.getTile(tx, ty);
                if (!tile.explored || tile.type == TileType.WALL) {
                    continue;
                }
                if (tile.visible) {
                    shapes.setColor(0.42f, 0.50f, 0.62f, 1f);
                } else {
                    shapes.setColor(0.16f, 0.20f, 0.28f, 1f);
                }
                shapes.rect(startX + tx * scale, startY + ty * scale, Math.max(1f, scale), Math.max(1f, scale));
            }
        }

        drawPoint(shapes, startX, startY, scale, world.map.worldToTile(world.map.stairsPosition.x),
                world.map.worldToTile(world.map.stairsPosition.y), 1f, 0.86f, 0.20f);
        for (Chest chest : world.chests) {
            if (chest.opened) {
                continue;
            }
            int cx = world.map.worldToTile(chest.getCenter().x);
            int cy = world.map.worldToTile(chest.getCenter().y);
            if (world.map.getTile(cx, cy).explored) {
                drawPoint(shapes, startX, startY, scale, cx, cy, 1f, 0.72f, 0.18f);
            }
        }
        for (Trap trap : world.traps) {
            int tx = world.map.worldToTile(trap.getCenter().x);
            int ty = world.map.worldToTile(trap.getCenter().y);
            if (world.map.getTile(tx, ty).visible) {
                drawPoint(shapes, startX, startY, scale, tx, ty, 1f, 0.42f, 0.16f);
            }
        }
        for (Enemy enemy : world.enemies) {
            int ex = world.map.worldToTile(enemy.getCenter().x);
            int ey = world.map.worldToTile(enemy.getCenter().y);
            if (world.map.getTile(ex, ey).visible) {
                drawPoint(shapes, startX, startY, scale, ex, ey, 0.92f, 0.18f, 0.16f);
            }
        }
        drawPoint(shapes, startX, startY, scale, world.map.worldToTile(world.player.getCenter().x),
                world.map.worldToTile(world.player.getCenter().y), 0.18f, 0.62f, 1f);
    }

    private void drawPoint(ShapeRenderer shapes, float startX, float startY, float scale, int tileX, int tileY,
                           float r, float g, float b) {
        shapes.setColor(r, g, b, 1f);
        shapes.rect(startX + tileX * scale - 2f, startY + tileY * scale - 2f, 5f, 5f);
    }
}
