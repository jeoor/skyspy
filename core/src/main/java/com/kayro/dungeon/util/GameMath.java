package com.kayro.dungeon.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public final class GameMath {
    private GameMath() {
    }

    public static int worldToTile(float worldValue) {
        return MathUtils.floor(worldValue / Constants.TILE_WIDTH);
    }

    public static float tileCenter(int tile) {
        return (tile + 0.5f) * Constants.TILE_WIDTH;
    }

    public static float distance2(Vector2 a, Vector2 b) {
        return Vector2.dst2(a.x, a.y, b.x, b.y);
    }
}
