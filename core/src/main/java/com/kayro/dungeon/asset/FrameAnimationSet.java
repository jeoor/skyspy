package com.kayro.dungeon.asset;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.kayro.dungeon.entity.AnimationState;

public class FrameAnimationSet {
    private final TextureRegion[] idle;
    private final TextureRegion[] walk;
    private final TextureRegion[] action;
    private final TextureRegion[] hurt;
    private final TextureRegion[] death;

    public FrameAnimationSet(TextureRegion[] idle, TextureRegion[] walk, TextureRegion[] action,
                             TextureRegion[] hurt, TextureRegion[] death) {
        this.idle = normalize(idle);
        this.walk = normalize(walk);
        this.action = normalize(action);
        this.hurt = normalize(hurt);
        this.death = normalize(death);
    }

    public TextureRegion frame(AnimationState state, float animationTime) {
        TextureRegion[] frames = framesFor(state);
        float duration = frameDuration(state);
        int index;
        if (state == AnimationState.DEATH) {
            index = Math.min(frames.length - 1, MathUtils.floor(animationTime / duration));
        } else {
            index = MathUtils.floor(animationTime / duration) % frames.length;
        }
        return frames[index];
    }

    private TextureRegion[] framesFor(AnimationState state) {
        switch (state) {
            case WALK:
                return walk != null ? walk : idle;
            case ATTACK:
            case SKILL:
                return action != null ? action : (walk != null ? walk : idle);
            case HURT:
                return hurt != null ? hurt : idle;
            case DEATH:
                return death != null ? death : (hurt != null ? hurt : idle);
            case IDLE:
            default:
                return idle;
        }
    }

    private float frameDuration(AnimationState state) {
        switch (state) {
            case WALK:
                return 0.10f;
            case ATTACK:
            case SKILL:
                return 0.08f;
            case HURT:
                return 0.12f;
            case DEATH:
                return 0.14f;
            case IDLE:
            default:
                return 0.16f;
        }
    }

    private TextureRegion[] normalize(TextureRegion[] frames) {
        if (frames == null || frames.length == 0 || frames[0] == null) {
            return null;
        }
        return frames;
    }
}
