package com.kayro.dungeon.system;

import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;

import java.util.Arrays;
import java.util.PriorityQueue;

public class PathfindingSystem {
    private static final int WIDTH = Constants.MAP_WIDTH;
    private static final int HEIGHT = Constants.MAP_HEIGHT;
    private static final int SIZE = WIDTH * HEIGHT;
    private static final int[] DIR_X = {1, -1, 0, 0};
    private static final int[] DIR_Y = {0, 0, 1, -1};

    private final float[] gScore = new float[SIZE];
    private final int[] cameFrom = new int[SIZE];
    private final boolean[] closed = new boolean[SIZE];
    private final PriorityQueue<Node> open = new PriorityQueue<>();

    public boolean findNextStep(GameWorld world, Vector2 from, Vector2 to, Vector2 out) {
        int startX = world.map.worldToTile(from.x);
        int startY = world.map.worldToTile(from.y);
        int goalX = world.map.worldToTile(to.x);
        int goalY = world.map.worldToTile(to.y);
        if (!isWalkable(world, startX, startY) || !isWalkable(world, goalX, goalY)) {
            return false;
        }
        int start = index(startX, startY);
        int goal = index(goalX, goalY);
        if (start == goal) {
            return false;
        }

        Arrays.fill(gScore, Float.POSITIVE_INFINITY);
        Arrays.fill(cameFrom, -1);
        Arrays.fill(closed, false);
        open.clear();

        gScore[start] = 0f;
        open.add(new Node(start, heuristic(startX, startY, goalX, goalY)));

        boolean found = false;
        while (!open.isEmpty()) {
            Node current = open.poll();
            if (closed[current.index]) {
                continue;
            }
            if (current.index == goal) {
                found = true;
                break;
            }
            closed[current.index] = true;
            int x = current.index % WIDTH;
            int y = current.index / WIDTH;
            for (int i = 0; i < DIR_X.length; i++) {
                int nextX = x + DIR_X[i];
                int nextY = y + DIR_Y[i];
                if (!isWalkable(world, nextX, nextY)) {
                    continue;
                }
                int next = index(nextX, nextY);
                if (closed[next]) {
                    continue;
                }
                float tentative = gScore[current.index] + 1f;
                if (tentative >= gScore[next]) {
                    continue;
                }
                cameFrom[next] = current.index;
                gScore[next] = tentative;
                open.add(new Node(next, tentative + heuristic(nextX, nextY, goalX, goalY)));
            }
        }
        if (!found) {
            return false;
        }

        int step = goal;
        int previous = cameFrom[step];
        if (previous == -1) {
            return false;
        }
        while (previous != start && previous != -1) {
            step = previous;
            previous = cameFrom[step];
        }
        int stepX = step % WIDTH;
        int stepY = step / WIDTH;
        out.set((stepX + 0.5f) * Constants.TILE_SIZE, (stepY + 0.5f) * Constants.TILE_SIZE);
        return true;
    }

    public boolean hasWalkableLine(GameWorld world, Vector2 from, Vector2 to) {
        int x0 = world.map.worldToTile(from.x);
        int y0 = world.map.worldToTile(from.y);
        int x1 = world.map.worldToTile(to.x);
        int y1 = world.map.worldToTile(to.y);
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (!isWalkable(world, x0, y0)) {
                return false;
            }
            if (x0 == x1 && y0 == y1) {
                return true;
            }
            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    private boolean isWalkable(GameWorld world, int x, int y) {
        if (!world.map.isWalkableTile(x, y)) {
            return false;
        }
        for (Chest chest : world.chests) {
            if (!chest.blocksMovement()) {
                continue;
            }
            int chestX = world.map.worldToTile(chest.getCenter().x);
            int chestY = world.map.worldToTile(chest.getCenter().y);
            if (chestX == x && chestY == y) {
                return false;
            }
        }
        return true;
    }

    private int index(int x, int y) {
        return y * WIDTH + x;
    }

    private float heuristic(int x, int y, int goalX, int goalY) {
        return Math.abs(goalX - x) + Math.abs(goalY - y);
    }

    private static class Node implements Comparable<Node> {
        final int index;
        final float score;

        Node(int index, float score) {
            this.index = index;
            this.score = score;
        }

        @Override
        public int compareTo(Node other) {
            return Float.compare(score, other.score);
        }
    }
}
