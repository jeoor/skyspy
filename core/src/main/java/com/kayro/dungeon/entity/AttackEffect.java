package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.Vector2;

public class AttackEffect {
    private static final float DURATION = 0.12f;

    public final Vector2 start = new Vector2();
    public final Vector2 end = new Vector2();
    public float timer = DURATION;

    public AttackEffect(Vector2 origin, Vector2 direction) {
        Vector2 normal = new Vector2(-direction.y, direction.x).nor();
        start.set(origin).mulAdd(direction, 18f).mulAdd(normal, -14f);
        end.set(origin).mulAdd(direction, 48f).mulAdd(normal, 14f);
    }

    public void update(float delta) {
        timer = Math.max(0f, timer - delta);
    }

    public boolean isDone() {
        return timer <= 0f;
    }

    public float alpha() {
        return timer / DURATION;
    }
}
