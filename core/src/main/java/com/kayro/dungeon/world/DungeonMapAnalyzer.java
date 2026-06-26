package com.kayro.dungeon.world;

import com.kayro.dungeon.util.Constants;

import java.util.ArrayDeque;

public final class DungeonMapAnalyzer {
    private DungeonMapAnalyzer() {
    }

    public static String signature(DungeonMap map) {
        StringBuilder builder = new StringBuilder(Constants.MAP_WIDTH * Constants.MAP_HEIGHT * 4);
        builder.append(map.worldToTileX(map.playerSpawn.x)).append(',')
                .append(map.worldToTileY(map.playerSpawn.y)).append('|')
                .append(map.worldToTileX(map.stairsPosition.x)).append(',')
                .append(map.worldToTileY(map.stairsPosition.y)).append('|')
                .append(map.rooms.size).append('|');
        for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
            for (int x = 0; x < Constants.MAP_WIDTH; x++) {
                Tile tile = map.getTile(x, y);
                builder.append((char) ('A' + tile.type.ordinal()));
                builder.append((char) ('a' + tile.skyKind));
                builder.append((char) ('0' + Math.min(9, tile.variant)));
                builder.append(tile.grass ? 'g' : '-');
            }
        }
        return builder.toString();
    }

    public static int walkableCount(DungeonMap map) {
        int count = 0;
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                if (map.isWalkableTile(x, y)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static int reachableWalkableCount(DungeonMap map) {
        int startX = map.worldToTileX(map.playerSpawn.x);
        int startY = map.worldToTileY(map.playerSpawn.y);
        boolean[][] visited = floodWalkable(map, startX, startY);
        int count = 0;
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                if (visited[x][y]) {
                    count++;
                }
            }
        }
        return count;
    }

    public static boolean exitReachable(DungeonMap map) {
        int startX = map.worldToTileX(map.playerSpawn.x);
        int startY = map.worldToTileY(map.playerSpawn.y);
        int exitX = map.worldToTileX(map.stairsPosition.x);
        int exitY = map.worldToTileY(map.stairsPosition.y);
        return floodWalkable(map, startX, startY)[exitX][exitY];
    }

    private static boolean[][] floodWalkable(DungeonMap map, int startX, int startY) {
        boolean[][] visited = new boolean[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        if (!map.isWalkableTile(startX, startY)) {
            return visited;
        }
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {startX, startY});
        visited[startX][startY] = true;
        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            visit(map, visited, queue, point[0] - 1, point[1]);
            visit(map, visited, queue, point[0] + 1, point[1]);
            visit(map, visited, queue, point[0], point[1] - 1);
            visit(map, visited, queue, point[0], point[1] + 1);
        }
        return visited;
    }

    private static void visit(DungeonMap map, boolean[][] visited, ArrayDeque<int[]> queue, int x, int y) {
        if (!map.isInBounds(x, y) || visited[x][y] || !map.isWalkableTile(x, y)) {
            return;
        }
        visited[x][y] = true;
        queue.add(new int[] {x, y});
    }
}
