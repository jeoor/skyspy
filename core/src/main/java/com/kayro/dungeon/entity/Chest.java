package com.kayro.dungeon.entity;

import com.kayro.dungeon.world.GameWorld;

public class Chest extends Entity {
    public static final float OPEN_ANIMATION_DURATION = 0.55f;

    public final boolean bossChest;
    public boolean opened;
    public float openTimer;

    public Chest(float centerX, float centerY, boolean bossChest) {
        super(centerX - 12f, centerY - 12f, 24f, 24f);
        this.bossChest = bossChest;
    }

    @Override
    public void update(float delta, GameWorld world) {
        openTimer = Math.max(0f, openTimer - delta);
    }

    public void open() {
        if (opened) {
            return;
        }
        opened = true;
        openTimer = OPEN_ANIMATION_DURATION;
    }

    public boolean blocksMovement() {
        return !opened;
    }

    public float openAnimationTime() {
        return OPEN_ANIMATION_DURATION - openTimer;
    }

    public boolean isOpenAnimationDone() {
        return opened && openTimer <= 0f;
    }
}
