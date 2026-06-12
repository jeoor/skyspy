package com.kayro.dungeon.system;

import com.kayro.dungeon.entity.Player;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.DungeonMap;
import com.kayro.dungeon.world.Tile;

public class FogOfWarSystem {
    private static final int VISION_RADIUS = 8;

    public void update(DungeonMap map, Player player) {
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                map.getTile(x, y).visible = false;
            }
        }

        int playerX = map.worldToTile(player.getCenter().x);
        int playerY = map.worldToTile(player.getCenter().y);
        int radiusSq = VISION_RADIUS * VISION_RADIUS;

        for (int x = playerX - VISION_RADIUS; x <= playerX + VISION_RADIUS; x++) {
            for (int y = playerY - VISION_RADIUS; y <= playerY + VISION_RADIUS; y++) {
                if (!map.isInBounds(x, y)) {
                    continue;
                }
                int dx = x - playerX;
                int dy = y - playerY;
                if (dx * dx + dy * dy <= radiusSq) {
                    Tile tile = map.getTile(x, y);
                    tile.visible = true;
                    tile.explored = true;
                }
            }
        }
    }
}
