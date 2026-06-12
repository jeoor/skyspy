package com.kayro.dungeon.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kayro.dungeon.util.Constants;

public class DungeonGenerator {
    private static final int MIN_ROOMS = 18;
    private static final int MAX_ROOMS = 28;
    private static final int MIN_ROOM_WIDTH = 6;
    private static final int MAX_ROOM_WIDTH = 14;
    private static final int MIN_ROOM_HEIGHT = 5;
    private static final int MAX_ROOM_HEIGHT = 12;

    public DungeonMap generate() {
        DungeonMap map = new DungeonMap();
        int targetRooms = MathUtils.random(MIN_ROOMS, MAX_ROOMS);
        int attempts = targetRooms * 12;

        for (int i = 0; i < attempts && map.rooms.size < targetRooms; i++) {
            Room room = randomRoom();
            if (overlapsAny(room, map.rooms)) {
                continue;
            }

            carveRoom(map, room);
            if (map.rooms.size > 0) {
                Room previous = map.rooms.peek();
                carveCorridor(map, previous.centerX(), previous.centerY(), room.centerX(), room.centerY());
            }
            map.rooms.add(room);
        }

        if (map.rooms.size == 0) {
            Room fallback = new Room(8, 8, 10, 8);
            carveRoom(map, fallback);
            map.rooms.add(fallback);
        }

        Room first = map.rooms.first();
        map.playerSpawn.set(first.centerWorld(Constants.TILE_SIZE));
        Room stairsRoom = farthestRoomFrom(first, map.rooms);
        map.bossRoom = stairsRoom;
        map.setType(stairsRoom.centerX(), stairsRoom.centerY(), TileType.STAIRS_DOWN);
        map.stairsPosition.set(stairsRoom.centerWorld(Constants.TILE_SIZE));
        return map;
    }

    private Room randomRoom() {
        int width = MathUtils.random(MIN_ROOM_WIDTH, MAX_ROOM_WIDTH);
        int height = MathUtils.random(MIN_ROOM_HEIGHT, MAX_ROOM_HEIGHT);
        int x = MathUtils.random(2, Constants.MAP_WIDTH - width - 3);
        int y = MathUtils.random(2, Constants.MAP_HEIGHT - height - 3);
        return new Room(x, y, width, height);
    }

    private boolean overlapsAny(Room room, Array<Room> rooms) {
        for (Room other : rooms) {
            if (room.overlaps(other)) {
                return true;
            }
        }
        return false;
    }

    private void carveRoom(DungeonMap map, Room room) {
        for (int x = room.x; x < room.x + room.width; x++) {
            for (int y = room.y; y < room.y + room.height; y++) {
                map.setType(x, y, TileType.FLOOR);
            }
        }
    }

    private void carveCorridor(DungeonMap map, int x1, int y1, int x2, int y2) {
        if (MathUtils.randomBoolean()) {
            carveHorizontal(map, x1, x2, y1);
            carveVertical(map, y1, y2, x2);
        } else {
            carveVertical(map, y1, y2, x1);
            carveHorizontal(map, x1, x2, y2);
        }
    }

    private void carveHorizontal(DungeonMap map, int x1, int x2, int y) {
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);
        for (int x = start; x <= end; x++) {
            map.setType(x, y, TileType.FLOOR);
        }
    }

    private void carveVertical(DungeonMap map, int y1, int y2, int x) {
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2);
        for (int y = start; y <= end; y++) {
            map.setType(x, y, TileType.FLOOR);
        }
    }

    private Room farthestRoomFrom(Room origin, Array<Room> rooms) {
        Room farthest = origin;
        float bestDistance = -1f;
        for (Room room : rooms) {
            float distance = Vector2.dst2(origin.centerX(), origin.centerY(), room.centerX(), room.centerY());
            if (distance > bestDistance) {
                bestDistance = distance;
                farthest = room;
            }
        }
        return farthest;
    }
}
