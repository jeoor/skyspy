package com.kayro.dungeon.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Particle {
    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    public final Color color = new Color();
    public final float size;
    public final float maxLife;
    public float life;

    public Particle(float x, float y, float vx, float vy, float size, float life, Color color) {
        this.position.set(x, y);
        this.velocity.set(vx, vy);
        this.size = size;
        this.maxLife = life;
        this.life = life;
        this.color.set(color);
    }

    public void update(float delta) {
        life -= delta;
        position.mulAdd(velocity, delta);
        velocity.scl(Math.max(0f, 1f - delta * 3.8f));
    }

    public float alpha() {
        return Math.max(0f, life / maxLife);
    }

    public boolean isDone() {
        return life <= 0f;
    }
}
