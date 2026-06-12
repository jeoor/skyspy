package com.kayro.dungeon.util;

import com.badlogic.gdx.math.Vector2;

public enum Direction {
    UP(0f, 1f),
    DOWN(0f, -1f),
    LEFT(-1f, 0f),
    RIGHT(1f, 0f);

    public final Vector2 vector;

    Direction(float x, float y) {
        vector = new Vector2(x, y);
    }
}
