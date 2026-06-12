package com.kayro.dungeon.asset;

import com.badlogic.gdx.graphics.Texture;

public final class SpriteAnimations {
    private SpriteAnimations() {
    }

    public static DirectionalFrameAnimationSet directional16(Texture texture) {
        return new DirectionalFrameAnimationSet(
                animationSet(texture, 3),
                animationSet(texture, 8),
                animationSet(texture, 13)
        );
    }

    private static FrameAnimationSet animationSet(Texture texture, int startCol) {
        return new FrameAnimationSet(
                row(texture, startCol, 1),
                row(texture, startCol, 2),
                row(texture, startCol, 3),
                row(texture, startCol, 5),
                row(texture, startCol, 4)
        );
    }

    private static com.badlogic.gdx.graphics.g2d.TextureRegion[] row(Texture texture, int startCol, int row) {
        com.badlogic.gdx.graphics.g2d.TextureRegion[] frames = new com.badlogic.gdx.graphics.g2d.TextureRegion[4];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = new com.badlogic.gdx.graphics.g2d.TextureRegion(texture, (startCol + i) * 16, row * 16, 16, 16);
        }
        return frames;
    }
}
