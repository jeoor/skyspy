package com.kayro.dungeon.system;

import com.kayro.dungeon.entity.Player;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.DungeonMap;
import com.kayro.dungeon.world.Tile;

public class FogOfWarSystem {
    private static final int VISION_RADIUS = 8;

    private static final int[][] OCTANTS = {
            {1, 0, 0, 1},
            {0, 1, 1, 0},
            {0, -1, 1, 0},
            {-1, 0, 0, 1},
            {-1, 0, 0, -1},
            {0, -1, -1, 0},
            {0, 1, -1, 0},
            {1, 0, 0, -1}
    };

    public void update(DungeonMap map, Player player) {
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                map.getTile(x, y).visible = false;
            }
        }

        int playerX = map.worldToTile(player.getCenter().x);
        int playerY = map.worldToTile(player.getCenter().y);

        if (map.isInBounds(playerX, playerY)) {
            Tile center = map.getTile(playerX, playerY);
            center.visible = true;
            center.explored = true;
        }

        for (int[] oct : OCTANTS) {
            castLight(map, playerX, playerY, VISION_RADIUS, 1, 1.0f, 0.0f,
                    oct[0], oct[1], oct[2], oct[3]);
        }
    }

    private void castLight(DungeonMap map, int cx, int cy, int radius, int row,
                           float startSlope, float endSlope,
                           int xx, int xy, int yx, int yy) {
        if (startSlope < endSlope) {
            return;
        }

        int radiusSq = radius * radius;
        float nextStartSlope = startSlope;
        boolean blocked = false;

        for (int dist = row; dist <= radius && !blocked; dist++) {
            int deltaY = -dist;
            for (int deltaX = -dist; deltaX <= 0; deltaX++) {
                int mapX = cx + deltaX * xx + deltaY * xy;
                int mapY = cy + deltaX * yx + deltaY * yy;
                float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (startSlope < rightSlope) {
                    continue;
                }
                if (endSlope > leftSlope) {
                    break;
                }

                if (map.isInBounds(mapX, mapY) && deltaX * deltaX + deltaY * deltaY <= radiusSq) {
                    Tile tile = map.getTile(mapX, mapY);
                    tile.visible = true;
                    tile.explored = true;
                }

                boolean isWall = !map.isWalkableTile(mapX, mapY);

                if (blocked) {
                    if (isWall) {
                        nextStartSlope = rightSlope;
                    } else {
                        blocked = false;
                        startSlope = nextStartSlope;
                    }
                } else if (isWall && dist < radius) {
                    blocked = true;
                    castLight(map, cx, cy, radius, dist + 1, startSlope, leftSlope,
                            xx, xy, yx, yy);
                    nextStartSlope = rightSlope;
                }
            }
        }
    }
}
