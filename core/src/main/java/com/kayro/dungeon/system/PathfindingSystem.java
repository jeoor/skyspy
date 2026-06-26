package com.kayro.dungeon.system;

import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;

import java.util.PriorityQueue;

public class PathfindingSystem {
    private static final int WIDTH = Constants.MAP_WIDTH;
    private static final int HEIGHT = Constants.MAP_HEIGHT;
    private static final int SIZE = WIDTH * HEIGHT;
    private static final int[] DIR_X = {1, -1, 0, 0};
    private static final int[] DIR_Y = {0, 0, 1, -1};

    private final float[] gScore = new float[SIZE];
    private final int[] cameFrom = new int[SIZE];
    private final int[] openedGen = new int[SIZE];
    private final int[] closedGen = new int[SIZE];
    private final PriorityQueue<Node> open = new PriorityQueue<>();
    private int generation;

    public boolean findNextStep(GameWorld world, Vector2 from, Vector2 to, Vector2 out) {
        int startX = world.map.worldToTileX(from.x);
        int startY = world.map.worldToTileY(from.y);
        int goalX = world.map.worldToTileX(to.x);
        int goalY = world.map.worldToTileY(to.y);
        if (!isWalkable(world, startX, startY) || !isWalkable(world, goalX, goalY)) {
            return false;
        }
        int start = index(startX, startY);
        int goal = index(goalX, goalY);
        if (start == goal) {
            return false;
        }

        generation++;
        open.clear();

        openNode(start, 0f, -1);
        open.add(new Node(start, heuristic(startX, startY, goalX, goalY)));

        boolean found = false;
        while (!open.isEmpty()) {
            Node current = open.poll();
            if (isClosedNode(current.index)) {
                continue;
            }
            if (current.index == goal) {
                found = true;
                break;
            }
            closedGen[current.index] = generation;
            int x = current.index % WIDTH;
            int y = current.index / WIDTH;
            for (int i = 0; i < DIR_X.length; i++) {
                int nextX = x + DIR_X[i];
                int nextY = y + DIR_Y[i];
                if (!isWalkable(world, nextX, nextY)) {
                    continue;
                }
                int next = index(nextX, nextY);
                if (isClosedNode(next)) {
                    continue;
                }
                float tentative = getGScore(current.index) + 1f + edgeExposureCost(world, nextX, nextY);
                if (tentative >= getGScore(next)) {
                    continue;
                }
                openNode(next, tentative, current.index);
                open.add(new Node(next, tentative + heuristic(nextX, nextY, goalX, goalY)));
            }
        }
        if (!found) {
            return false;
        }

        int step = goal;
        int previous = getCameFrom(step);
        if (previous == -1) {
            return false;
        }
        while (previous != start && previous != -1) {
            step = previous;
            previous = getCameFrom(step);
        }
        int stepX = step % WIDTH;
        int stepY = step / WIDTH;
        out.set(world.map.tileCenterX(stepX), world.map.tileCenterY(stepY));
        return true;
    }

    private void openNode(int idx, float g, int from) {
        openedGen[idx] = generation;
        gScore[idx] = g;
        cameFrom[idx] = from;
    }

    private float getGScore(int idx) {
        return openedGen[idx] == generation ? gScore[idx] : Float.POSITIVE_INFINITY;
    }

    private int getCameFrom(int idx) {
        return openedGen[idx] == generation ? cameFrom[idx] : -1;
    }

    private boolean isClosedNode(int idx) {
        return closedGen[idx] == generation;
    }

    private boolean isWalkable(GameWorld world, int x, int y) {
        if (!world.map.isWalkableTile(x, y)) {
            return false;
        }
        for (Chest chest : world.chests) {
            if (!chest.blocksMovement()) {
                continue;
            }
            int chestX = world.map.worldToTileX(chest.getCenter().x);
            int chestY = world.map.worldToTileY(chest.getCenter().y);
            if (chestX == x && chestY == y) {
                return false;
            }
        }
        return true;
    }

    private float edgeExposureCost(GameWorld world, int x, int y) {
        int exposedSides = 0;
        for (int i = 0; i < DIR_X.length; i++) {
            if (!world.map.isWalkableTile(x + DIR_X[i], y + DIR_Y[i])) {
                exposedSides++;
            }
        }
        return exposedSides * 0.75f;
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
