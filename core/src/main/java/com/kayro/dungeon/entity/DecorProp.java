package com.kayro.dungeon.entity;

import com.kayro.dungeon.world.GameWorld;

public class DecorProp extends Entity {
    public final PropType type;

    public DecorProp(PropType type, float centerX, float centerY) {
        super(centerX - 10f, centerY - 10f, 20f, 20f);
        this.type = type;
    }

    @Override
    public void update(float delta, GameWorld world) {
    }
}
