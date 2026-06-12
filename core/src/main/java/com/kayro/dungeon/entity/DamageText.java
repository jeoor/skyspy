package com.kayro.dungeon.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class DamageText {
    public final String text;
    public final Vector2 position = new Vector2();
    public final Color color = new Color();
    public float life = 0.75f;

    public DamageText(String text, float x, float y, Color color) {
        this.text = text;
        position.set(x, y);
        this.color.set(color);
    }

    public void update(float delta) {
        life -= delta;
        position.y += 28f * delta;
        color.a = Math.max(0f, life / 0.75f);
    }

    public boolean isDone() {
        return life <= 0f;
    }
}
