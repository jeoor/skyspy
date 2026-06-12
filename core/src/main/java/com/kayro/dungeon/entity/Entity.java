package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.world.GameWorld;

public abstract class Entity {
    public final Vector2 position = new Vector2();
    public final Vector2 size = new Vector2();
    public boolean removed;

    private final Rectangle cachedBounds = new Rectangle();
    private final Vector2 cachedCenter = new Vector2();

    protected Entity(float x, float y, float width, float height) {
        position.set(x, y);
        size.set(width, height);
    }

    public Rectangle getBounds() {
        return cachedBounds.set(position.x, position.y, size.x, size.y);
    }

    public Vector2 getCenter() {
        return cachedCenter.set(position.x + size.x * 0.5f, position.y + size.y * 0.5f);
    }

    public abstract void update(float delta, GameWorld world);
}
