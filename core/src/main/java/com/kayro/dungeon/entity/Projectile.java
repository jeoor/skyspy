package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectSet;
import com.kayro.dungeon.world.GameWorld;

public class Projectile extends Entity {
    public final Vector2 velocity = new Vector2();
    public final ObjectSet<Enemy> hitEnemies = new ObjectSet<>();
    public final boolean hostile;
    public float rotation;
    public float life = 1.2f;
    public int damage;
    public int pierceLeft;

    public Projectile(float centerX, float centerY, Vector2 direction, float speed, int damage) {
        this(centerX, centerY, direction, speed, damage, false);
    }

    public Projectile(float centerX, float centerY, Vector2 direction, float speed, int damage, boolean hostile) {
        super(centerX - 9f, centerY - 4f, 18f, 8f);
        velocity.set(direction).nor().scl(speed);
        rotation = direction.angleDeg();
        this.damage = damage;
        this.hostile = hostile;
    }

    @Override
    public void update(float delta, GameWorld world) {
        position.mulAdd(velocity, delta);
        life -= delta;
        if (life <= 0f || !world.map.isAreaWalkable(getBounds())) {
            removed = true;
        }
    }
}
