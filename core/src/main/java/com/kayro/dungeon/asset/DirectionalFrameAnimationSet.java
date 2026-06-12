package com.kayro.dungeon.asset;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kayro.dungeon.entity.AnimationState;
import com.kayro.dungeon.util.Direction;

public class DirectionalFrameAnimationSet {
    private final FrameAnimationSet side;
    private final FrameAnimationSet south;
    private final FrameAnimationSet north;

    public DirectionalFrameAnimationSet(FrameAnimationSet side, FrameAnimationSet south, FrameAnimationSet north) {
        this.side = side;
        this.south = south;
        this.north = north;
    }

    public TextureRegion frame(AnimationState state, Direction direction, float animationTime) {
        switch (direction) {
            case UP:
                return north.frame(state, animationTime);
            case DOWN:
                return south.frame(state, animationTime);
            case LEFT:
            case RIGHT:
            default:
                return side.frame(state, animationTime);
        }
    }
}
