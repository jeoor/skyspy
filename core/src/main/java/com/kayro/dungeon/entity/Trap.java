package com.kayro.dungeon.entity;

import com.kayro.dungeon.world.GameWorld;

public class Trap extends Entity {
    public float cooldownTimer;

    public Trap(float centerX, float centerY) {
        super(centerX - 12f, centerY - 12f, 24f, 24f);
    }

    @Override
    public void update(float delta, GameWorld world) {
        cooldownTimer = Math.max(0f, cooldownTimer - delta);
    }

    public boolean isReady() {
        return cooldownTimer <= 0f;
    }

    public void trigger() {
        cooldownTimer = 1.2f;
    }
}
