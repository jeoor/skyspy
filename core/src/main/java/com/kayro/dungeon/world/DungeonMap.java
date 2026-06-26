package com.kayro.dungeon.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kayro.dungeon.util.Constants;

public class DungeonMap {
    private static final Tile OUT_OF_BOUNDS = new Tile(TileType.VOID);

    private final Tile[][] tiles = new Tile[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
    public final Array<Room> rooms = new Array<>();
    public Vector2 playerSpawn = new Vector2();
    public Vector2 stairsPosition = new Vector2();
    public Room bossRoom;
    public boolean fallingVoid = true;

    public DungeonMap() {
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                tiles[x][y] = new Tile(TileType.VOID);
            }
        }
    }

    public Tile getTile(int x, int y) {
        if (!isInBounds(x, y)) {
            return OUT_OF_BOUNDS;
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
        return isWalkableTile(worldToTileX(worldX), worldToTileY(worldY));
    }

    public int worldToTile(float value) {
        return worldToTileX(value);
    }

    public int worldToTileX(float value) {
        return MathUtils.floor(value / Constants.TILE_WIDTH);
    }

    public int worldToTileY(float value) {
        return MathUtils.floor(value / Constants.TILE_HEIGHT);
    }

    public float tileCenterX(int tileX) {
        return (tileX + 0.5f) * Constants.TILE_WIDTH;
    }

    public float tileCenterY(int tileY) {
        return (tileY + 0.5f) * Constants.TILE_HEIGHT;
    }

    public Vector2 tileCenter(int tileX, int tileY) {
        return new Vector2(tileCenterX(tileX), tileCenterY(tileY));
    }

    public float worldWidth() {
        return Constants.MAP_WIDTH * Constants.TILE_WIDTH;
    }

    public float worldHeight() {
        return Constants.MAP_HEIGHT * Constants.TILE_HEIGHT;
    }
}
