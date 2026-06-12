package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.util.Direction;
import com.kayro.dungeon.world.GameWorld;

public abstract class LivingEntity extends Entity {
    public static final float DEATH_ANIMATION_DURATION = 0.56f;

    public int hp;
    public int maxHp;
    public int attack;
    public int defense;
    public float speed;
    public float attackCooldown;
    public float attackTimer;
    public Direction facing = Direction.DOWN;
    public boolean facingLeft;
    public AnimationState animationState = AnimationState.IDLE;
    public float animationTime;

    protected LivingEntity(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public void takeDamage(int amount) {
        if (isDead()) {
            return;
        }
        hp -= Math.max(0, amount);
        if (hp <= 0) {
            hp = 0;
            setAnimationState(AnimationState.DEATH);
        } else {
            setAnimationState(AnimationState.HURT);
        }
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public boolean isDeathAnimationDone() {
        return isDead() && animationTime >= DEATH_ANIMATION_DURATION;
    }

    public void setAnimationState(AnimationState state) {
        if (animationState != state) {
            animationState = state;
            animationTime = 0f;
        }
    }

    public void updateAnimation(float delta) {
        animationTime += delta;
    }

    public void updateFacing(Vector2 direction) {
        if (direction.isZero(0.01f)) {
            return;
        }
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            facing = direction.x > 0f ? Direction.RIGHT : Direction.LEFT;
            facingLeft = direction.x < 0f;
        } else {
            facing = direction.y > 0f ? Direction.UP : Direction.DOWN;
        }
    }

    public void setLocomotionState(boolean moving) {
        if (isActionAnimationLocked()) {
            return;
        }
        setAnimationState(moving ? AnimationState.WALK : AnimationState.IDLE);
    }

    private boolean isActionAnimationLocked() {
        switch (animationState) {
            case ATTACK:
                return animationTime < 0.28f;
            case SKILL:
                return animationTime < 0.36f;
            case HURT:
                return animationTime < 0.22f;
            case DEATH:
                return true;
            default:
                return false;
        }
    }

    @Override
    public abstract void update(float delta, GameWorld world);
}
