package com.kayro.dungeon.asset;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

public final class ProceduralTextures {
    private static final int TILE = 16;

    private ProceduralTextures() {
    }

    public static Pixmap dungeonTileset() {
        Pixmap pixmap = new Pixmap(16 * TILE, 8 * TILE, Pixmap.Format.RGBA8888);
        for (int row = 2; row <= 3; row++) {
            for (int col = 10; col <= 12; col++) {
                drawFloor(pixmap, col, row, 0.18f + (col - 10) * 0.02f);
            }
        }
        drawWall(pixmap, 8, 5);
        drawStairs(pixmap, 13, 1);
        return pixmap;
    }

    public static Pixmap actorSheet(Color primary, Color accent) {
        Pixmap pixmap = new Pixmap(17 * TILE, 6 * TILE, Pixmap.Format.RGBA8888);
        int[] startCols = {3, 8, 13};
        for (int startCol : startCols) {
            for (int row = 1; row <= 5; row++) {
                for (int frame = 0; frame < 4; frame++) {
                    drawActorFrame(pixmap, startCol + frame, row, frame, primary, accent);
                }
            }
        }
        return pixmap;
    }

    public static Pixmap itemSheet() {
        Pixmap pixmap = new Pixmap(8 * TILE, 4 * TILE, Pixmap.Format.RGBA8888);
        drawPotion(pixmap, 1, 2);
        drawArmor(pixmap, 3, 2);
        drawKey(pixmap, 4, 2);
        drawCoin(pixmap, 5, 2);
        drawArrow(pixmap, 6, 2);
        return pixmap;
    }

    public static Pixmap uiSheet() {
        Pixmap pixmap = new Pixmap(11 * TILE, 7 * TILE, Pixmap.Format.RGBA8888);
        drawEmptyHeartGauge(pixmap, 5, 0);
        drawHeartIcon(pixmap, 5, 1);
        drawDiamondIcon(pixmap, 5, 2);
        drawShieldIcon(pixmap, 6, 2);
        return pixmap;
    }

    public static Pixmap propSheet() {
        Pixmap pixmap = new Pixmap(7 * TILE, 8 * TILE, Pixmap.Format.RGBA8888);
        drawBarrel(pixmap, 1, 1);
        drawCrate(pixmap, 2, 1);
        drawTorch(pixmap, 1, 3);
        drawRubble(pixmap, 4, 5);
        drawChest(pixmap, 0, 6, 0);
        drawChest(pixmap, 1, 6, 1);
        drawChest(pixmap, 2, 6, 2);
        drawChest(pixmap, 3, 6, 3);
        drawTrap(pixmap, 3, 3);
        return pixmap;
    }

    private static void drawFloor(Pixmap pixmap, int col, int row, float base) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(base, base, base, 1f);
        pixmap.fillRectangle(x, y, TILE, TILE);
        pixmap.setColor(base + 0.07f, base + 0.07f, base + 0.07f, 1f);
        pixmap.drawLine(x, y + 5, x + TILE - 1, y + 5);
        pixmap.drawLine(x + 3, y + 11, x + TILE - 4, y + 11);
    }

    private static void drawWall(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.36f, 0.36f, 0.36f, 1f);
        pixmap.fillRectangle(x, y, TILE, TILE);
        pixmap.setColor(0.16f, 0.16f, 0.16f, 1f);
        for (int line = 3; line < TILE; line += 4) {
            pixmap.drawLine(x, y + line, x + TILE - 1, y + line);
        }
        pixmap.drawRectangle(x, y, TILE, TILE);
    }

    private static void drawStairs(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.10f, 0.12f, 0.18f, 1f);
        pixmap.fillRectangle(x, y, TILE, TILE);
        pixmap.setColor(0.78f, 0.72f, 0.36f, 1f);
        for (int step = 0; step < 4; step++) {
            pixmap.drawLine(x + 3 + step, y + 4 + step * 3, x + 12 - step, y + 4 + step * 3);
        }
    }

    private static void drawActorFrame(Pixmap pixmap, int col, int row, int frame, Color primary, Color accent) {
        int x = col * TILE;
        int y = row * TILE;
        int bob = row == 2 ? frame % 2 : 0;
        pixmap.setColor(0.04f, 0.04f, 0.05f, 1f);
        pixmap.fillRectangle(x + 5, y + 3 + bob, 6, 9);
        pixmap.setColor(primary);
        pixmap.fillRectangle(x + 6, y + 4 + bob, 4, 6);
        pixmap.setColor(accent);
        pixmap.fillRectangle(x + 6, y + 2 + bob, 4, 3);
        if (row == 3) {
            pixmap.setColor(0.82f, 0.84f, 0.88f, 1f);
            pixmap.drawLine(x + 10, y + 7, x + 14, y + 5);
        } else if (row == 4) {
            pixmap.setColor(0.80f, 0.18f, 0.16f, 1f);
            pixmap.drawLine(x + 5, y + 5, x + 11, y + 10);
        } else if (row == 5) {
            pixmap.setColor(primary);
            pixmap.fillRectangle(x + 4, y + 10, 8, 3);
        }
    }

    private static void drawPotion(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.88f, 0.10f, 0.16f, 1f);
        pixmap.fillRectangle(x + 5, y + 6, 6, 7);
        pixmap.setColor(0.85f, 0.88f, 0.92f, 1f);
        pixmap.fillRectangle(x + 6, y + 3, 4, 3);
    }

    private static void drawArmor(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.55f, 0.60f, 0.65f, 1f);
        pixmap.fillRectangle(x + 4, y + 4, 8, 10);
        pixmap.setColor(0.22f, 0.25f, 0.30f, 1f);
        pixmap.drawRectangle(x + 4, y + 4, 8, 10);
    }

    private static void drawSword(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.82f, 0.84f, 0.88f, 1f);
        pixmap.drawLine(x + 4, y + 12, x + 12, y + 4);
        pixmap.setColor(0.48f, 0.24f, 0.10f, 1f);
        pixmap.drawLine(x + 4, y + 10, x + 6, y + 12);
    }

    private static void drawCoin(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.95f, 0.76f, 0.18f, 1f);
        pixmap.fillCircle(x + 8, y + 8, 5);
        pixmap.setColor(0.48f, 0.30f, 0.05f, 1f);
        pixmap.drawCircle(x + 8, y + 8, 5);
    }

    private static void drawArrow(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.72f, 0.56f, 0.42f, 1f);
        pixmap.drawLine(x + 3, y + 8, x + 11, y + 8);
        pixmap.setColor(0.78f, 0.84f, 0.86f, 1f);
        pixmap.drawLine(x + 11, y + 8, x + 8, y + 5);
        pixmap.drawLine(x + 11, y + 8, x + 8, y + 11);
        pixmap.setColor(0.30f, 0.42f, 0.50f, 1f);
        pixmap.drawLine(x + 3, y + 8, x + 1, y + 6);
        pixmap.drawLine(x + 3, y + 8, x + 1, y + 10);
    }

    private static void drawKey(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.96f, 0.72f, 0.20f, 1f);
        pixmap.fillCircle(x + 5, y + 8, 3);
        pixmap.drawLine(x + 8, y + 8, x + 13, y + 8);
        pixmap.drawLine(x + 11, y + 8, x + 11, y + 11);
        pixmap.drawLine(x + 13, y + 8, x + 13, y + 10);
    }

    private static void drawHeartIcon(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.92f, 0.10f, 0.16f, 1f);
        pixmap.fillCircle(x + 5, y + 6, 3);
        pixmap.fillCircle(x + 10, y + 6, 3);
        pixmap.fillTriangle(x + 2, y + 7, x + 13, y + 7, x + 8, y + 14);
        pixmap.setColor(0.42f, 0.04f, 0.06f, 1f);
        pixmap.drawLine(x + 2, y + 7, x + 8, y + 14);
        pixmap.drawLine(x + 13, y + 7, x + 8, y + 14);
    }

    private static void drawEmptyHeartGauge(Pixmap pixmap, int col, int row) {
        for (int i = 0; i < 5; i++) {
            int x = (col + i) * TILE;
            int y = row * TILE;
            pixmap.setColor(0.56f, 0.56f, 0.52f, 1f);
            pixmap.fillCircle(x + 5, y + 6, 3);
            pixmap.fillCircle(x + 10, y + 6, 3);
            pixmap.fillTriangle(x + 2, y + 7, x + 13, y + 7, x + 8, y + 14);
            pixmap.setColor(0.12f, 0.12f, 0.12f, 1f);
            pixmap.drawLine(x + 2, y + 7, x + 8, y + 14);
            pixmap.drawLine(x + 13, y + 7, x + 8, y + 14);
            pixmap.setColor(0.72f, 0.72f, 0.66f, 1f);
            pixmap.drawLine(x + 4, y + 4, x + 11, y + 4);
        }
    }

    private static void drawDiamondIcon(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.20f, 0.56f, 0.95f, 1f);
        pixmap.fillTriangle(x + 8, y + 2, x + 13, y + 8, x + 8, y + 14);
        pixmap.fillTriangle(x + 8, y + 2, x + 3, y + 8, x + 8, y + 14);
        pixmap.setColor(0.68f, 0.88f, 1.0f, 1f);
        pixmap.drawLine(x + 8, y + 2, x + 13, y + 8);
        pixmap.drawLine(x + 8, y + 2, x + 3, y + 8);
    }

    private static void drawShieldIcon(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.68f, 0.74f, 0.80f, 1f);
        pixmap.fillRectangle(x + 4, y + 3, 8, 7);
        pixmap.fillTriangle(x + 4, y + 9, x + 12, y + 9, x + 8, y + 14);
        pixmap.setColor(0.26f, 0.30f, 0.36f, 1f);
        pixmap.drawRectangle(x + 4, y + 3, 8, 7);
        pixmap.drawLine(x + 4, y + 9, x + 8, y + 14);
        pixmap.drawLine(x + 12, y + 9, x + 8, y + 14);
    }

    private static void drawBarrel(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.34f, 0.16f, 0.06f, 1f);
        pixmap.fillRectangle(x + 4, y + 4, 8, 11);
        pixmap.setColor(0.68f, 0.42f, 0.16f, 1f);
        pixmap.drawLine(x + 4, y + 6, x + 11, y + 6);
        pixmap.drawLine(x + 4, y + 12, x + 11, y + 12);
        pixmap.setColor(0.08f, 0.05f, 0.03f, 1f);
        pixmap.drawRectangle(x + 4, y + 4, 8, 11);
    }

    private static void drawCrate(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.42f, 0.23f, 0.10f, 1f);
        pixmap.fillRectangle(x + 3, y + 4, 10, 10);
        pixmap.setColor(0.72f, 0.45f, 0.18f, 1f);
        pixmap.drawLine(x + 3, y + 4, x + 13, y + 14);
        pixmap.drawLine(x + 13, y + 4, x + 3, y + 14);
    }

    private static void drawTorch(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.38f, 0.18f, 0.08f, 1f);
        pixmap.drawLine(x + 8, y + 7, x + 8, y + 15);
        pixmap.setColor(1f, 0.72f, 0.18f, 1f);
        pixmap.fillCircle(x + 8, y + 5, 3);
        pixmap.setColor(0.90f, 0.18f, 0.08f, 1f);
        pixmap.drawLine(x + 8, y + 2, x + 8, y + 7);
    }

    private static void drawRubble(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.24f, 0.24f, 0.25f, 1f);
        pixmap.fillRectangle(x + 3, y + 11, 5, 4);
        pixmap.fillRectangle(x + 8, y + 8, 4, 6);
        pixmap.setColor(0.40f, 0.40f, 0.42f, 1f);
        pixmap.fillRectangle(x + 5, y + 7, 4, 3);
    }

    private static void drawChest(Pixmap pixmap, int col, int row, int frame) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.14f, 0.07f, 0.03f, 1f);
        pixmap.fillRectangle(x + 2, y + 6, 12, 8);
        pixmap.setColor(0.56f, 0.28f, 0.08f, 1f);
        pixmap.fillRectangle(x + 3, y + 7, 10, 6);
        pixmap.setColor(0.90f, 0.68f, 0.20f, 1f);
        pixmap.drawLine(x + 3, y + 9, x + 13, y + 9);
        pixmap.fillRectangle(x + 7, y + 8, 2, 3);
        if (frame > 0) {
            pixmap.setColor(0.08f, 0.04f, 0.02f, 1f);
            pixmap.fillRectangle(x + 3, y + 5, 10, 2);
            pixmap.setColor(0.70f, 0.34f, 0.10f, 1f);
            pixmap.fillRectangle(x + 3, y + Math.max(2, 6 - frame), 10, 2);
        }
    }

    private static void drawTrap(Pixmap pixmap, int col, int row) {
        int x = col * TILE;
        int y = row * TILE;
        pixmap.setColor(0.12f, 0.12f, 0.14f, 1f);
        pixmap.fillRectangle(x + 3, y + 9, 10, 3);
        pixmap.setColor(0.70f, 0.74f, 0.78f, 1f);
        for (int spike = 0; spike < 4; spike++) {
            int sx = x + 4 + spike * 3;
            pixmap.drawLine(sx, y + 9, sx + 1, y + 4);
            pixmap.drawLine(sx + 1, y + 4, sx + 2, y + 9);
        }
    }
}
