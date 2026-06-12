package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.Vector2;

public class AttackEffect {
    private static final float DURATION = 0.12f;

    public final Vector2 start = new Vector2();
    public final Vector2 end = new Vector2();
    public float timer = DURATION;

    public AttackEffect(Vector2 origin, Vector2 direction) {
        float nx = -direction.y;
        float ny = direction.x;
        float len = (float) Math.sqrt(nx * nx + ny * ny);
        if (len > 0.001f) {
            nx /= len;
            ny /= len;
        }
        start.set(origin.x + direction.x * 18f + nx * -14f,
                  origin.y + direction.y * 18f + ny * -14f);
        end.set(origin.x + direction.x * 48f + nx * 14f,
                origin.y + direction.y * 48f + ny * 14f);
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
