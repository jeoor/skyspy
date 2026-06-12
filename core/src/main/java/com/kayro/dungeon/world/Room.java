package com.kayro.dungeon.world;

import com.badlogic.gdx.math.Rectangle;
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

    public boolean overlaps(Room other) {
        Rectangle a = new Rectangle(x - 1, y - 1, width + 2, height + 2);
        Rectangle b = new Rectangle(other.x, other.y, other.width, other.height);
        return a.overlaps(b);
    }
}
