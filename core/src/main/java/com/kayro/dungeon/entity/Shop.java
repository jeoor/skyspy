package com.kayro.dungeon.entity;

import com.kayro.dungeon.world.GameWorld;

public class Shop extends Entity {
    public static final float INTERACT_RANGE = 60f;

    public Shop(float centerX, float centerY) {
        super(centerX - 14f, centerY - 14f, 28f, 28f);
    }

    @Override
    public void update(float delta, GameWorld world) {
    }
}
