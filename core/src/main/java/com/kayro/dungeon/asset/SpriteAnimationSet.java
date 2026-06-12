package com.kayro.dungeon.asset;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.kayro.dungeon.entity.AnimationState;

public class SpriteAnimationSet {
    private static final int DEFAULT_CELL_SIZE = 100;
    private static final int CROP_PADDING = 4;

    private final TextureRegion[][] frames;
    private final int[] frameCounts;
    private final int rowCount;

    public SpriteAnimationSet(Texture texture, Pixmap pixmap) {
        int cellSize = DEFAULT_CELL_SIZE;
        int cols = Math.max(1, pixmap.getWidth() / cellSize);
        rowCount = Math.max(1, pixmap.getHeight() / cellSize);
        frames = new TextureRegion[rowCount][cols];
        frameCounts = new int[rowCount];

        for (int row = 0; row < rowCount; row++) {
            int lastVisibleFrame = -1;
            for (int col = 0; col < cols; col++) {
                TextureRegion frame = cropVisibleFrame(texture, pixmap, col, row, cellSize);
                frames[row][col] = frame;
                if (frame != null) {
                    lastVisibleFrame = col;
                }
            }
            frameCounts[row] = Math.max(1, lastVisibleFrame + 1);
        }
    }

    public TextureRegion frame(AnimationState state, float animationTime) {
        int row = rowFor(state);
        int count = frameCounts[row];
        float duration = frameDuration(state);
        int index;
        if (state == AnimationState.DEATH) {
            index = Math.min(count - 1, MathUtils.floor(animationTime / duration));
        } else {
            index = MathUtils.floor(animationTime / duration) % count;
        }
        return frames[row][index] == null ? firstFrame() : frames[row][index];
    }

    private TextureRegion firstFrame() {
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < frameCounts[row]; col++) {
                if (frames[row][col] != null) {
                    return frames[row][col];
                }
            }
        }
        return null;
    }

    private int rowFor(AnimationState state) {
        switch (state) {
            case WALK:
                return clampRow(1);
            case ATTACK:
                return clampRow(2);
            case SKILL:
                return clampRow(rowCount >= 7 ? 4 : 3);
            case HURT:
                return clampRow(rowCount >= 7 ? 5 : Math.max(0, rowCount - 2));
            case DEATH:
                return clampRow(rowCount - 1);
            case IDLE:
            default:
                return 0;
        }
    }

    private int clampRow(int row) {
        return MathUtils.clamp(row, 0, rowCount - 1);
    }

    private float frameDuration(AnimationState state) {
        switch (state) {
            case WALK:
                return 0.10f;
            case ATTACK:
            case SKILL:
                return 0.075f;
            case HURT:
                return 0.12f;
            case DEATH:
                return 0.14f;
            case IDLE:
            default:
                return 0.16f;
        }
    }

    private TextureRegion cropVisibleFrame(Texture texture, Pixmap pixmap, int col, int row, int cellSize) {
        int startX = col * cellSize;
        int startY = row * cellSize;
        int endX = Math.min(startX + cellSize, pixmap.getWidth());
        int endY = Math.min(startY + cellSize, pixmap.getHeight());
        int minX = endX;
        int minY = endY;
        int maxX = startX;
        int maxY = startY;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int alpha = pixmap.getPixel(x, y) & 0x000000ff;
                if (alpha > 12) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (minX > maxX || minY > maxY) {
            return null;
        }

        minX = Math.max(startX, minX - CROP_PADDING);
        minY = Math.max(startY, minY - CROP_PADDING);
        maxX = Math.min(endX - 1, maxX + CROP_PADDING);
        maxY = Math.min(endY - 1, maxY + CROP_PADDING);
        return new TextureRegion(texture, minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
