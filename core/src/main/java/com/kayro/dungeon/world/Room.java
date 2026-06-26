package com.kayro.dungeon.world;

import com.badlogic.gdx.math.Vector2;

public class Room {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int centerX() {
        return x + width / 2;
    }

    public int centerY() {
        return y + height / 2;
    }

    public Vector2 centerWorld(int tileSize) {
        return new Vector2((centerX() + 0.5f) * tileSize, (centerY() + 0.5f) * tileSize);
    }

    public Vector2 centerWorld(int tileWidth, int tileHeight) {
        return new Vector2((centerX() + 0.5f) * tileWidth, (centerY() + 0.5f) * tileHeight);
    }

    public boolean overlaps(Room other) {
        int ax = x - 1, ay = y - 1, aw = width + 2, ah = height + 2;
        return ax < other.x + other.width && ax + aw > other.x
                && ay < other.y + other.height && ay + ah > other.y;
    }
}
