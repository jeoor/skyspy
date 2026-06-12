package com.kayro.dungeon.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kayro.dungeon.util.Constants;

public class DungeonMap {
    private final Tile[][] tiles = new Tile[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
    public final Array<Room> rooms = new Array<>();
    public Vector2 playerSpawn = new Vector2();
    public Vector2 stairsPosition = new Vector2();
    public Room bossRoom;

    public DungeonMap() {
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                tiles[x][y] = new Tile(TileType.WALL);
            }
        }
    }

    public Tile getTile(int x, int y) {
        if (!isInBounds(x, y)) {
            return new Tile(TileType.WALL);
        }
        return tiles[x][y];
    }

    public void setType(int x, int y, TileType type) {
        if (isInBounds(x, y)) {
            tiles[x][y].type = type;
        }
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < Constants.MAP_WIDTH && y < Constants.MAP_HEIGHT;
    }

    public boolean isWalkableTile(int x, int y) {
        return isInBounds(x, y) && tiles[x][y].isWalkable();
    }

    public boolean isWalkableWorld(float worldX, float worldY) {
        return isWalkableTile(worldToTile(worldX), worldToTile(worldY));
    }

    public boolean isAreaWalkable(Rectangle bounds) {
        int startX = worldToTile(bounds.x);
        int endX = worldToTile(bounds.x + bounds.width - 0.1f);
        int startY = worldToTile(bounds.y);
        int endY = worldToTile(bounds.y + bounds.height - 0.1f);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (!isWalkableTile(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int worldToTile(float value) {
        return MathUtils.floor(value / Constants.TILE_SIZE);
    }

    public float worldWidth() {
        return Constants.MAP_WIDTH * Constants.TILE_SIZE;
    }

    public float worldHeight() {
        return Constants.MAP_HEIGHT * Constants.TILE_SIZE;
    }
}
