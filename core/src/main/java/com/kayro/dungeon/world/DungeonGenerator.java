package com.kayro.dungeon.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kayro.dungeon.util.Constants;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class DungeonGenerator {
    private static final int EMPTY = 0;
    private static final int VOID = 1;
    private static final int PLATE_LOWER_EDGE = 2;
    private static final int SAND = 3;
    private static final int SAND_GRASS = 4;
    private static final int PLATE = 5;
    private static final int PLATE_UPPER_EDGE = 6;
    private static final int UNUSED = 7;
    private static final int GRASS = 8;
    private static final int GRASS_LOWER_EDGE = 9;
    private static final int GRASS_UPPER_EDGE = 10;
    private static final int GRASS_FLOWERS = 11;
    private static final int GRATE = 13;
    private static final int MARGIN_X = 14;
    private static final int MARGIN_Y = 10;
    private static final int MIN_WALKABLE_TILES = 950;
    private static final int MIN_EXIT_DISTANCE_SQ = 34 * 34;
    private static final int MAX_GENERATION_ATTEMPTS = 18;
    private static final int MIN_ROOMS = 18;
    private static final int MAX_ROOMS = 24;
    private static final int ROOM_PLACEMENT_ATTEMPTS = 2600;
    private static final float LOOP_EDGE_CHANCE = 0.18f;
    private static final int[] PATH_DIR_X = {1, -1, 0, 0};
    private static final int[] PATH_DIR_Y = {0, 0, 1, -1};

    private final Array<Room> generatedRooms = new Array<>();
    private final RandomXS128 random;

    public DungeonGenerator() {
        this(new RandomXS128(MathUtils.random.nextLong()));
    }

    public DungeonGenerator(long seed) {
        this(new RandomXS128(seed));
    }

    private DungeonGenerator(RandomXS128 random) {
        this.random = random;
    }

    public DungeonMap generate() {
        DungeonMap map = new DungeonMap();
        int[][] visual = new int[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        Array<int[]> walkable = new Array<>();
        int[] spawn = null;
        int[] rift = null;
        int attempts = 0;
        do {
            attempts++;
            generateVisualMap(visual);
            walkable.clear();
            for (int x = 1; x < Constants.MAP_WIDTH - 1; x++) {
                for (int y = 1; y < Constants.MAP_HEIGHT - 1; y++) {
                    if (isWalkableVisual(visual[x][y])) {
                        walkable.add(new int[] {x, y});
                    }
                }
            }
            if (walkable.size >= MIN_WALKABLE_TILES) {
                spawn = randomCenterish(walkable);
                rift = farthestWalkable(spawn, walkable, visual);
            }
        } while (!validCandidate(walkable, spawn, rift, visual) && attempts < MAX_GENERATION_ATTEMPTS);

        if (spawn == null) {
            spawn = randomCenterish(walkable);
        }
        if (rift == null) {
            rift = farthestWalkable(spawn, walkable, visual);
        }

        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                Tile tile = map.getTile(x, y);
                if (isWalkableVisual(visual[x][y])) {
                    map.setType(x, y, TileType.FLOOR);
                    tile.skyKind = skyKindFor(visual[x][y]);
                    tile.variant = Math.abs(x * 73471 + y * 91577) % 9;
                    tile.grass = isGrassVisual(visual[x][y]);
                } else if (isEdgeVisual(visual[x][y])) {
                    tile.skyKind = skyKindFor(visual[x][y]);
                    tile.variant = Math.abs(x * 73471 + y * 91577) % 9;
                }
            }
        }

        assignEdgesAndGrassMasks(map);
        map.playerSpawn.set(map.tileCenter(spawn[0], spawn[1]));
        map.setType(rift[0], rift[1], TileType.STAIRS_DOWN);
        map.stairsPosition.set(map.tileCenter(rift[0], rift[1]));
        seedPseudoRooms(map, walkable, spawn, rift);
        return map;
    }

    public DungeonMap generate(long seed) {
        return new DungeonGenerator(seed).generate();
    }

    private boolean validCandidate(Array<int[]> walkable, int[] spawn, int[] rift, int[][] visual) {
        return walkable.size >= MIN_WALKABLE_TILES
                && spawn != null
                && rift != null
                && hasExitClearance(visual, rift[0], rift[1])
                && generatedRooms.size >= MIN_ROOMS
                && Vector2.dst2(spawn[0], spawn[1], rift[0], rift[1]) >= MIN_EXIT_DISTANCE_SQ;
    }

    private void generateVisualMap(int[][] map) {
        fill(map, VOID);
        generatedRooms.clear();
        placeGraphRooms();
        for (Room room : generatedRooms) {
            carveRect(map, room, PLATE);
        }
        for (GraphEdge edge : connectedRoomGraph()) {
            carvePathCorridor(map, generatedRooms.get(edge.a), generatedRooms.get(edge.b));
        }

        keepLargestComponent(map, PLATE, PLATE);
        widenThinWalkways(map);
        addEdges(map);
        addCenterTiles(map);
        int[][] overlay = new int[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        int overlayCount;
        int tries = 0;
        do {
            overlayCount = initOverlay(overlay);
            tries++;
        } while (overlayCount < 520 && tries < 8);
        combineGrass(map, overlay);
        cleanEdgesPostCombine(map);
        addGrates(map);
    }

    private void placeGraphRooms() {
        generatedRooms.add(centeredInitialRoom());
        int targetRooms = randomInt(MIN_ROOMS, MAX_ROOMS);
        int guard = 0;
        while (generatedRooms.size < targetRooms && guard < ROOM_PLACEMENT_ATTEMPTS) {
            guard++;
            Room room = randomGraphRoom();
            if (!overlapsExistingRoom(room)) {
                generatedRooms.add(room);
            }
        }
    }

    private Room centeredInitialRoom() {
        int width = randomInt(9, 13);
        int height = randomInt(7, 11);
        int x = Constants.MAP_WIDTH / 2 - width / 2 + randomInt(-5, 5);
        int y = Constants.MAP_HEIGHT / 2 - height / 2 + randomInt(-4, 4);
        x = MathUtils.clamp(x, MARGIN_X, Constants.MAP_WIDTH - MARGIN_X - width);
        y = MathUtils.clamp(y, MARGIN_Y, Constants.MAP_HEIGHT - MARGIN_Y - height);
        return new Room(x, y, width, height);
    }

    private Room randomGraphRoom() {
        int width = randomInt(6, 12);
        int height = randomInt(5, 9);
        int x = randomInt(MARGIN_X, Constants.MAP_WIDTH - MARGIN_X - width);
        int y = randomInt(MARGIN_Y, Constants.MAP_HEIGHT - MARGIN_Y - height);
        return new Room(x, y, width, height);
    }

    private boolean overlapsExistingRoom(Room room) {
        for (Room existing : generatedRooms) {
            if (room.x - 2 < existing.x + existing.width
                    && room.x + room.width + 2 > existing.x
                    && room.y - 2 < existing.y + existing.height
                    && room.y + room.height + 2 > existing.y) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<GraphEdge> connectedRoomGraph() {
        ArrayList<GraphEdge> delaunay = delaunayEdges();
        if (delaunay.size() < generatedRooms.size - 1) {
            delaunay = completeRoomGraph();
        }
        delaunay.sort(Comparator.comparingDouble(edge -> edge.weight));

        UnionFind unionFind = new UnionFind(generatedRooms.size);
        ArrayList<GraphEdge> selected = new ArrayList<>();
        ArrayList<GraphEdge> rejected = new ArrayList<>();
        for (GraphEdge edge : delaunay) {
            if (unionFind.union(edge.a, edge.b)) {
                selected.add(edge);
            } else {
                rejected.add(edge);
            }
        }

        if (selected.size() < generatedRooms.size - 1) {
            for (GraphEdge edge : completeRoomGraph()) {
                if (unionFind.union(edge.a, edge.b)) {
                    selected.add(edge);
                }
            }
        }

        int extraLoops = Math.max(2, generatedRooms.size / 5);
        for (GraphEdge edge : rejected) {
            if (extraLoops <= 0) {
                break;
            }
            if (randomChance(LOOP_EDGE_CHANCE)) {
                selected.add(edge);
                extraLoops--;
            }
        }
        return selected;
    }

    private ArrayList<GraphEdge> delaunayEdges() {
        int roomCount = generatedRooms.size;
        ArrayList<DelaunayPoint> points = new ArrayList<>();
        for (int i = 0; i < roomCount; i++) {
            Room room = generatedRooms.get(i);
            points.add(new DelaunayPoint(room.centerX(), room.centerY()));
        }

        points.add(new DelaunayPoint(-Constants.MAP_WIDTH * 4f, -Constants.MAP_HEIGHT * 2f));
        points.add(new DelaunayPoint(Constants.MAP_WIDTH * 5f, -Constants.MAP_HEIGHT * 2f));
        points.add(new DelaunayPoint(Constants.MAP_WIDTH * 0.5f, Constants.MAP_HEIGHT * 5f));

        ArrayList<Triangle> triangles = new ArrayList<>();
        Triangle superTriangle = Triangle.create(points, roomCount, roomCount + 1, roomCount + 2);
        if (superTriangle == null) {
            return completeRoomGraph();
        }
        triangles.add(superTriangle);

        for (int i = 0; i < roomCount; i++) {
            DelaunayPoint point = points.get(i);
            ArrayList<Triangle> badTriangles = new ArrayList<>();
            ArrayList<GraphEdge> polygon = new ArrayList<>();
            for (Triangle triangle : triangles) {
                if (triangle.contains(point)) {
                    badTriangles.add(triangle);
                    toggleBoundaryEdge(polygon, new GraphEdge(triangle.a, triangle.b, 0f));
                    toggleBoundaryEdge(polygon, new GraphEdge(triangle.b, triangle.c, 0f));
                    toggleBoundaryEdge(polygon, new GraphEdge(triangle.c, triangle.a, 0f));
                }
            }
            triangles.removeAll(badTriangles);
            for (GraphEdge edge : polygon) {
                Triangle triangle = Triangle.create(points, edge.a, edge.b, i);
                if (triangle != null) {
                    triangles.add(triangle);
                }
            }
        }

        ArrayList<GraphEdge> edges = new ArrayList<>();
        for (Triangle triangle : triangles) {
            if (triangle.a >= roomCount || triangle.b >= roomCount || triangle.c >= roomCount) {
                continue;
            }
            addUniqueEdge(edges, triangle.a, triangle.b);
            addUniqueEdge(edges, triangle.b, triangle.c);
            addUniqueEdge(edges, triangle.c, triangle.a);
        }
        return edges;
    }

    private ArrayList<GraphEdge> completeRoomGraph() {
        ArrayList<GraphEdge> edges = new ArrayList<>();
        for (int i = 0; i < generatedRooms.size; i++) {
            for (int j = i + 1; j < generatedRooms.size; j++) {
                edges.add(new GraphEdge(i, j, roomDistance(i, j)));
            }
        }
        edges.sort(Comparator.comparingDouble(edge -> edge.weight));
        return edges;
    }

    private void addUniqueEdge(ArrayList<GraphEdge> edges, int a, int b) {
        GraphEdge edge = new GraphEdge(a, b, roomDistance(a, b));
        for (GraphEdge existing : edges) {
            if (existing.sameUndirected(edge)) {
                return;
            }
        }
        edges.add(edge);
    }

    private void toggleBoundaryEdge(ArrayList<GraphEdge> polygon, GraphEdge edge) {
        for (int i = 0; i < polygon.size(); i++) {
            if (polygon.get(i).sameUndirected(edge)) {
                polygon.remove(i);
                return;
            }
        }
        polygon.add(edge);
    }

    private float roomDistance(int a, int b) {
        Room left = generatedRooms.get(a);
        Room right = generatedRooms.get(b);
        return Vector2.dst2(left.centerX(), left.centerY(), right.centerX(), right.centerY());
    }

    private void carvePathCorridor(int[][] map, Room from, Room to) {
        Array<int[]> path = findCorridorPath(map, from.centerX(), from.centerY(), to.centerX(), to.centerY());
        if (path.size == 0) {
            carveFallbackCorridor(map, from.centerX(), from.centerY(), to.centerX(), to.centerY());
            return;
        }
        int width = randomChance(0.25f) ? 3 : 2;
        for (int i = 0; i < path.size; i++) {
            int[] point = path.get(i);
            int prevX = i > 0 ? path.get(i - 1)[0] : point[0];
            int prevY = i > 0 ? path.get(i - 1)[1] : point[1];
            int nextX = i < path.size - 1 ? path.get(i + 1)[0] : point[0];
            int nextY = i < path.size - 1 ? path.get(i + 1)[1] : point[1];
            boolean horizontal = Math.abs(nextX - prevX) >= Math.abs(nextY - prevY);
            carveWideCell(map, point[0], point[1], width, horizontal);
        }
    }

    private Array<int[]> findCorridorPath(int[][] map, int startX, int startY, int goalX, int goalY) {
        int width = Constants.MAP_WIDTH;
        int height = Constants.MAP_HEIGHT;
        int size = width * height;
        float[] gScore = new float[size];
        int[] cameFrom = new int[size];
        boolean[] closed = new boolean[size];
        for (int i = 0; i < size; i++) {
            gScore[i] = Float.POSITIVE_INFINITY;
            cameFrom[i] = -1;
        }

        PriorityQueue<PathNode> open = new PriorityQueue<>();
        int start = pathIndex(startX, startY);
        int goal = pathIndex(goalX, goalY);
        gScore[start] = 0f;
        open.add(new PathNode(start, manhattan(startX, startY, goalX, goalY)));

        while (!open.isEmpty()) {
            PathNode current = open.poll();
            if (closed[current.index]) {
                continue;
            }
            if (current.index == goal) {
                return reconstructPath(cameFrom, start, goal);
            }
            closed[current.index] = true;
            int x = current.index % width;
            int y = current.index / width;
            for (int i = 0; i < PATH_DIR_X.length; i++) {
                int nextX = x + PATH_DIR_X[i];
                int nextY = y + PATH_DIR_Y[i];
                if (!isCorridorPathCell(nextX, nextY)) {
                    continue;
                }
                int next = pathIndex(nextX, nextY);
                if (closed[next]) {
                    continue;
                }
                float tentative = gScore[current.index] + corridorCost(map, nextX, nextY);
                if (tentative >= gScore[next]) {
                    continue;
                }
                gScore[next] = tentative;
                cameFrom[next] = current.index;
                open.add(new PathNode(next, tentative + manhattan(nextX, nextY, goalX, goalY) * 1.05f));
            }
        }
        return new Array<>();
    }

    private Array<int[]> reconstructPath(int[] cameFrom, int start, int goal) {
        Array<int[]> path = new Array<>();
        int current = goal;
        while (current != -1) {
            path.add(new int[] {current % Constants.MAP_WIDTH, current / Constants.MAP_WIDTH});
            if (current == start) {
                break;
            }
            current = cameFrom[current];
        }
        path.reverse();
        return path;
    }

    private boolean isCorridorPathCell(int x, int y) {
        return x >= MARGIN_X - 1 && x < Constants.MAP_WIDTH - MARGIN_X + 1
                && y >= MARGIN_Y - 1 && y < Constants.MAP_HEIGHT - MARGIN_Y + 1;
    }

    private float corridorCost(int[][] map, int x, int y) {
        if (map[x][y] == PLATE) {
            return 0.85f;
        }
        int neighbors = 0;
        for (int i = 0; i < PATH_DIR_X.length; i++) {
            if (map[x + PATH_DIR_X[i]][y + PATH_DIR_Y[i]] == PLATE) {
                neighbors++;
            }
        }
        return neighbors > 0 ? 2.2f : 3.8f;
    }

    private float manhattan(int x, int y, int goalX, int goalY) {
        return Math.abs(goalX - x) + Math.abs(goalY - y);
    }

    private int pathIndex(int x, int y) {
        return y * Constants.MAP_WIDTH + x;
    }

    private void carveFallbackCorridor(int[][] map, int startX, int startY, int goalX, int goalY) {
        int x = startX;
        int y = startY;
        int width = 2;
        while (x != goalX) {
            carveWideCell(map, x, y, width, true);
            x += goalX > x ? 1 : -1;
        }
        while (y != goalY) {
            carveWideCell(map, x, y, width, false);
            y += goalY > y ? 1 : -1;
        }
        carveWideCell(map, goalX, goalY, width, true);
    }

    private void carveWideCell(int[][] map, int x, int y, int width, boolean horizontal) {
        int start = -width / 2;
        for (int offset = start; offset < start + width; offset++) {
            int carveX = horizontal ? x : x + offset;
            int carveY = horizontal ? y + offset : y;
            if (carveX > 0 && carveX < Constants.MAP_WIDTH - 1
                    && carveY > 0 && carveY < Constants.MAP_HEIGHT - 1) {
                map[carveX][carveY] = PLATE;
            }
        }
    }

    private void carveRect(int[][] map, Room rect, int value) {
        for (int x = rect.x; x < rect.x + rect.width; x++) {
            for (int y = rect.y; y < rect.y + rect.height; y++) {
                map[x][y] = value;
            }
        }
    }

    private void fill(int[][] map, int value) {
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                map[x][y] = value;
            }
        }
    }

    private void clear(int[][] map) {
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                map[x][y] = EMPTY;
            }
        }
    }

    private void condense(int[][] map, int[][] temp, int reps) {
        for (int x = 2; x < Constants.MAP_WIDTH - 2; x++) {
            for (int y = 2; y < Constants.MAP_HEIGHT - 2; y++) {
                int count = 0;
                for (int ox = -1; ox <= 1; ox++) {
                    for (int oy = -1; oy <= 1; oy++) {
                        if (ox == 0 && oy == 0) {
                            continue;
                        }
                        if (map[x + ox][y + oy] == VOID) {
                            count++;
                        }
                    }
                }
                if (map[x][y] == VOID) {
                    temp[x][y] = count < 2 ? EMPTY : VOID;
                } else {
                    temp[x][y] = count > 5 ? VOID : EMPTY;
                }
            }
        }
        for (int x = 2; x < Constants.MAP_WIDTH - 2; x++) {
            for (int y = 2; y < Constants.MAP_HEIGHT - 2; y++) {
                map[x][y] = temp[x][y];
            }
        }
        if (reps > 0) {
            condense(map, temp, reps - 1);
        }
    }

    private void renumber(int[][] map) {
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                if (map[x][y] == VOID) {
                    map[x][y] = UNUSED;
                } else if (map[x][y] == EMPTY) {
                    map[x][y] = VOID;
                }
            }
        }
    }

    private void floodRandomUnused(int[][] map, int replacement) {
        int sx;
        int sy;
        int guard = 0;
        do {
            sx = randomInt(1, Constants.MAP_WIDTH - 2);
            sy = randomInt(1, Constants.MAP_HEIGHT - 2);
            guard++;
        } while (map[sx][sy] != UNUSED && guard < 1000);
        floodFill(map, sx, sy, UNUSED, replacement);
    }

    private void keepLargestComponent(int[][] map, int target, int replacement) {
        boolean[][] visited = new boolean[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        Array<int[]> largest = new Array<>();
        for (int x = 1; x < Constants.MAP_WIDTH - 1; x++) {
            for (int y = 1; y < Constants.MAP_HEIGHT - 1; y++) {
                if (visited[x][y] || map[x][y] != target) {
                    continue;
                }
                Array<int[]> component = collectComponent(map, visited, x, y, target);
                if (component.size > largest.size) {
                    largest = component;
                }
            }
        }
        for (int x = 1; x < Constants.MAP_WIDTH - 1; x++) {
            for (int y = 1; y < Constants.MAP_HEIGHT - 1; y++) {
                if (map[x][y] == target) {
                    map[x][y] = VOID;
                }
            }
        }
        for (int[] point : largest) {
            map[point[0]][point[1]] = replacement;
        }
    }

    private Array<int[]> collectComponent(int[][] map, boolean[][] visited, int sx, int sy, int target) {
        Array<int[]> component = new Array<>();
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {sx, sy});
        visited[sx][sy] = true;
        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            component.add(current);
            collectNeighbor(map, visited, queue, current[0] - 1, current[1], target);
            collectNeighbor(map, visited, queue, current[0] + 1, current[1], target);
            collectNeighbor(map, visited, queue, current[0], current[1] - 1, target);
            collectNeighbor(map, visited, queue, current[0], current[1] + 1, target);
        }
        return component;
    }

    private void collectNeighbor(int[][] map, boolean[][] visited, ArrayDeque<int[]> queue,
                                 int x, int y, int target) {
        if (x <= 0 || x >= Constants.MAP_WIDTH - 1 || y <= 0 || y >= Constants.MAP_HEIGHT - 1) {
            return;
        }
        if (!visited[x][y] && map[x][y] == target) {
            visited[x][y] = true;
            queue.add(new int[] {x, y});
        }
    }

    private void widenThinWalkways(int[][] map) {
        int[][] widened = new int[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            System.arraycopy(map[x], 0, widened[x], 0, Constants.MAP_HEIGHT);
        }
        for (int x = 2; x < Constants.MAP_WIDTH - 2; x++) {
            for (int y = 2; y < Constants.MAP_HEIGHT - 2; y++) {
                if (map[x][y] != PLATE) {
                    continue;
                }
                boolean horizontal = map[x - 1][y] == PLATE && map[x + 1][y] == PLATE;
                boolean vertical = map[x][y - 1] == PLATE && map[x][y + 1] == PLATE;
                boolean blockedNorthSouth = map[x][y - 1] != PLATE && map[x][y + 1] != PLATE;
                boolean blockedEastWest = map[x - 1][y] != PLATE && map[x + 1][y] != PLATE;
                if (horizontal && blockedNorthSouth) {
                    widened[x][map[x][y + 1] == VOID ? y + 1 : y - 1] = PLATE;
                } else if (vertical && blockedEastWest) {
                    widened[map[x + 1][y] == VOID ? x + 1 : x - 1][y] = PLATE;
                }
            }
        }
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            System.arraycopy(widened[x], 0, map[x], 0, Constants.MAP_HEIGHT);
        }
    }

    private void floodFill(int[][] map, int sx, int sy, int target, int replacement) {
        if (map[sx][sy] != target) {
            return;
        }
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {sx, sy});
        map[sx][sy] = replacement;
        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            visit(map, queue, current[0] - 1, current[1], target, replacement);
            visit(map, queue, current[0] + 1, current[1], target, replacement);
            visit(map, queue, current[0], current[1] - 1, target, replacement);
            visit(map, queue, current[0], current[1] + 1, target, replacement);
        }
    }

    private void visit(int[][] map, ArrayDeque<int[]> queue, int x, int y, int target, int replacement) {
        if (x <= 0 || x >= Constants.MAP_WIDTH - 1 || y <= 0 || y >= Constants.MAP_HEIGHT - 1) {
            return;
        }
        if (map[x][y] == target) {
            map[x][y] = replacement;
            queue.add(new int[] {x, y});
        }
    }

    private void addEdges(int[][] map) {
        for (int x = 1; x < Constants.MAP_WIDTH - 1; x++) {
            for (int y = 1; y < Constants.MAP_HEIGHT - 1; y++) {
                if (map[x][y] == VOID) {
                    if (map[x][y - 1] == PLATE) {
                        map[x][y] = map[x][y + 1] != PLATE ? PLATE_LOWER_EDGE : PLATE;
                    } else if (map[x][y + 1] == PLATE) {
                        map[x][y] = PLATE_UPPER_EDGE;
                    }
                }
            }
        }
    }

    private void addCenterTiles(int[][] map) {
        for (int x = 1; x < Constants.MAP_WIDTH - 1; x++) {
            for (int y = 1; y < Constants.MAP_HEIGHT - 1; y++) {
                if (isPlateFamily(map[x - 1][y]) && isPlateFamily(map[x + 1][y])
                        && isPlateFamily(map[x][y - 1]) && isPlateFamily(map[x][y + 1])) {
                    map[x][y] = randomInt(0, 12) > 2 ? SAND : SAND_GRASS;
                }
            }
        }
    }

    private int initOverlay(int[][] overlay) {
        int[][] temp = new int[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        clear(overlay);
        for (int x = MARGIN_X; x < Constants.MAP_WIDTH - MARGIN_X; x++) {
            for (int y = MARGIN_Y; y < Constants.MAP_HEIGHT - MARGIN_Y; y++) {
                overlay[x][y] = random.nextBoolean() ? VOID : EMPTY;
            }
        }
        condense(overlay, temp, 3);
        floodRandomVoid(overlay, PLATE);
        int count = 0;
        for (int x = 0; x < Constants.MAP_WIDTH - 2; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT - 2; y++) {
                if (overlay[x][y] == PLATE) {
                    count++;
                }
            }
        }
        return count;
    }

    private void floodRandomVoid(int[][] map, int replacement) {
        int sx;
        int sy;
        int guard = 0;
        do {
            sx = randomInt(1, Constants.MAP_WIDTH - 2);
            sy = randomInt(1, Constants.MAP_HEIGHT - 2);
            guard++;
        } while (map[sx][sy] != VOID && guard < 1000);
        floodFill(map, sx, sy, VOID, replacement);
    }

    private void combineGrass(int[][] map, int[][] overlay) {
        for (int x = 1; x < Constants.MAP_WIDTH - 1; x++) {
            for (int y = 1; y < Constants.MAP_HEIGHT - 1; y++) {
                if (overlay[x][y] != PLATE || overlayNeighborCount(overlay, x, y) < 2
                        || !isWalkableVisual(map[x][y])) {
                    continue;
                }
                if (map[x][y] == PLATE_LOWER_EDGE) {
                    map[x][y] = GRASS_LOWER_EDGE;
                } else if (map[x][y] == PLATE_UPPER_EDGE) {
                    map[x][y] = GRASS_UPPER_EDGE;
                } else if (map[x][y] == PLATE) {
                    map[x][y] = GRASS;
                } else {
                    map[x][y] = GRASS_FLOWERS;
                }
            }
        }
    }

    private int overlayNeighborCount(int[][] overlay, int x, int y) {
        int count = 0;
        if (overlay[x - 1][y] == PLATE) {
            count++;
        }
        if (overlay[x + 1][y] == PLATE) {
            count++;
        }
        if (overlay[x][y - 1] == PLATE) {
            count++;
        }
        if (overlay[x][y + 1] == PLATE) {
            count++;
        }
        return count;
    }

    private void cleanEdgesPostCombine(int[][] map) {
        for (int x = 1; x < Constants.MAP_WIDTH - 1; x++) {
            for (int y = 1; y < Constants.MAP_HEIGHT - 1; y++) {
                if (map[x][y] == GRASS_LOWER_EDGE && map[x][y - 1] != GRASS) {
                    map[x][y] = PLATE_LOWER_EDGE;
                } else if (map[x][y] == GRASS_UPPER_EDGE && map[x][y + 1] != GRASS) {
                    map[x][y] = PLATE_UPPER_EDGE;
                }
            }
        }
    }

    private void addGrates(int[][] map) {
        for (int x = 1; x < Constants.MAP_WIDTH - 1; x++) {
            for (int y = 1; y < Constants.MAP_HEIGHT - 1; y++) {
                if (map[x][y] == PLATE && randomInt(0, 10) == 0) {
                    map[x][y] = GRATE;
                }
            }
        }
    }

    private void assignEdgesAndGrassMasks(DungeonMap map) {
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                Tile tile = map.getTile(x, y);
                if (!tile.isWalkable()) {
                    continue;
                }
                tile.edgeWest = !map.isWalkableTile(x - 1, y);
                tile.edgeEast = !map.isWalkableTile(x + 1, y);
                tile.edgeSouth = !map.isWalkableTile(x, y - 1);
                tile.edgeNorth = !map.isWalkableTile(x, y + 1);
                if (tile.grass) {
                    int mask = 0;
                    if (map.getTile(x, y - 1).grass) {
                        mask += 1;
                    }
                    if (map.getTile(x + 1, y).grass) {
                        mask += 2;
                    }
                    if (map.getTile(x, y + 1).grass) {
                        mask += 4;
                    }
                    if (map.getTile(x - 1, y).grass) {
                        mask += 8;
                    }
                    tile.skyMask = mask;
                }
            }
        }
    }

    private void seedPseudoRooms(DungeonMap map, Array<int[]> walkable, int[] spawn, int[] rift) {
        map.rooms.clear();
        if (generatedRooms.size > 0) {
            map.rooms.addAll(generatedRooms);
            map.bossRoom = nearestRoomTo(rift[0], rift[1], map.rooms);
            return;
        }
        addPseudoRoom(map, spawn[0], spawn[1]);
        addPseudoRoom(map, rift[0], rift[1]);
        int count = Math.min(18, Math.max(6, walkable.size / 130));
        for (int i = 0; i < count; i++) {
            int[] point = randomPoint(walkable);
            addPseudoRoom(map, point[0], point[1]);
        }
        map.bossRoom = map.rooms.size > 1 ? map.rooms.get(1) : map.rooms.first();
    }

    private Room nearestRoomTo(int tileX, int tileY, Array<Room> rooms) {
        Room best = rooms.first();
        float bestDistance = Float.MAX_VALUE;
        for (Room room : rooms) {
            float distance = Vector2.dst2(tileX, tileY, room.centerX(), room.centerY());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = room;
            }
        }
        return best;
    }

    private void addPseudoRoom(DungeonMap map, int centerX, int centerY) {
        int x = MathUtils.clamp(centerX - 2, 1, Constants.MAP_WIDTH - 6);
        int y = MathUtils.clamp(centerY - 2, 1, Constants.MAP_HEIGHT - 6);
        map.rooms.add(new Room(x, y, 5, 5));
    }

    private int[] randomCenterish(Array<int[]> walkable) {
        int[] best = randomPoint(walkable);
        Vector2 center = new Vector2(Constants.MAP_WIDTH * 0.5f, Constants.MAP_HEIGHT * 0.5f);
        float bestScore = Float.MAX_VALUE;
        for (int i = 0; i < Math.min(160, walkable.size); i++) {
            int[] point = randomPoint(walkable);
            float score = Vector2.dst2(point[0], point[1], center.x, center.y);
            if (score < bestScore) {
                bestScore = score;
                best = point;
            }
        }
        return best;
    }

    private int[] farthestWalkable(int[] from, Array<int[]> walkable, int[][] visual) {
        int[] best = null;
        float bestDistance = -1f;
        for (int[] point : walkable) {
            if (!hasExitClearance(visual, point[0], point[1])) {
                continue;
            }
            float distance = Vector2.dst2(from[0], from[1], point[0], point[1]);
            if (distance > bestDistance) {
                bestDistance = distance;
                best = point;
            }
        }
        if (best != null) {
            return best;
        }
        best = walkable.first();
        bestDistance = -1f;
        for (int[] point : walkable) {
            float distance = Vector2.dst2(from[0], from[1], point[0], point[1]);
            if (distance > bestDistance) {
                bestDistance = distance;
                best = point;
            }
        }
        return best;
    }

    private boolean hasExitClearance(int[][] map, int tileX, int tileY) {
        if (tileX < 2 || tileX >= Constants.MAP_WIDTH - 2
                || tileY < 2 || tileY >= Constants.MAP_HEIGHT - 2) {
            return false;
        }
        for (int x = tileX - 1; x <= tileX + 1; x++) {
            for (int y = tileY - 1; y <= tileY + 1; y++) {
                if (!isWalkableVisual(map[x][y])) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPlateFamily(int value) {
        return value == PLATE || value == SAND || value == SAND_GRASS;
    }

    private boolean isGrassVisual(int value) {
        return value == GRASS || value == GRASS_FLOWERS
                || value == GRASS_LOWER_EDGE || value == GRASS_UPPER_EDGE;
    }

    private boolean isWalkableVisual(int value) {
        return value == PLATE || value == SAND || value == SAND_GRASS || value == GRATE
                || value == GRASS || value == GRASS_FLOWERS;
    }

    private boolean isEdgeVisual(int value) {
        return value == PLATE_LOWER_EDGE || value == PLATE_UPPER_EDGE
                || value == GRASS_LOWER_EDGE || value == GRASS_UPPER_EDGE;
    }

    private int skyKindFor(int value) {
        switch (value) {
            case SAND:
                return Tile.SKY_SAND;
            case SAND_GRASS:
                return Tile.SKY_SAND_GRASS;
            case PLATE_LOWER_EDGE:
                return Tile.SKY_PLATE_LOWER_EDGE;
            case PLATE_UPPER_EDGE:
                return Tile.SKY_PLATE_UPPER_EDGE;
            case GRASS:
                return Tile.SKY_GRASS;
            case GRASS_FLOWERS:
                return Tile.SKY_GRASS_FLOWERS;
            case GRASS_LOWER_EDGE:
                return Tile.SKY_GRASS_LOWER_EDGE;
            case GRASS_UPPER_EDGE:
                return Tile.SKY_GRASS_UPPER_EDGE;
            case GRATE:
                return Tile.SKY_GRATE;
            case PLATE:
            default:
                return Tile.SKY_PLATE;
        }
    }

    private int randomInt(int minInclusive, int maxInclusive) {
        return minInclusive + random.nextInt(maxInclusive - minInclusive + 1);
    }

    private boolean randomChance(float chance) {
        return random.nextFloat() < chance;
    }

    private int[] randomPoint(Array<int[]> points) {
        return points.get(random.nextInt(points.size));
    }

    private static class DelaunayPoint {
        final float x;
        final float y;

        DelaunayPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class GraphEdge {
        final int a;
        final int b;
        final float weight;

        GraphEdge(int a, int b, float weight) {
            this.a = Math.min(a, b);
            this.b = Math.max(a, b);
            this.weight = weight;
        }

        boolean sameUndirected(GraphEdge other) {
            return a == other.a && b == other.b;
        }
    }

    private static class Triangle {
        final int a;
        final int b;
        final int c;
        final float circumX;
        final float circumY;
        final float radiusSq;

        private Triangle(int a, int b, int c, float circumX, float circumY, float radiusSq) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.circumX = circumX;
            this.circumY = circumY;
            this.radiusSq = radiusSq;
        }

        static Triangle create(ArrayList<DelaunayPoint> points, int a, int b, int c) {
            DelaunayPoint pa = points.get(a);
            DelaunayPoint pb = points.get(b);
            DelaunayPoint pc = points.get(c);
            float ax = pa.x;
            float ay = pa.y;
            float bx = pb.x;
            float by = pb.y;
            float cx = pc.x;
            float cy = pc.y;
            float d = 2f * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));
            if (Math.abs(d) < 0.0001f) {
                return null;
            }
            float aa = ax * ax + ay * ay;
            float bb = bx * bx + by * by;
            float cc = cx * cx + cy * cy;
            float ux = (aa * (by - cy) + bb * (cy - ay) + cc * (ay - by)) / d;
            float uy = (aa * (cx - bx) + bb * (ax - cx) + cc * (bx - ax)) / d;
            return new Triangle(a, b, c, ux, uy, Vector2.dst2(ux, uy, ax, ay));
        }

        boolean contains(DelaunayPoint point) {
            return Vector2.dst2(point.x, point.y, circumX, circumY) <= radiusSq + 0.001f;
        }
    }

    private static class UnionFind {
        private final int[] parent;
        private final int[] rank;

        UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        boolean union(int a, int b) {
            int rootA = find(a);
            int rootB = find(b);
            if (rootA == rootB) {
                return false;
            }
            if (rank[rootA] < rank[rootB]) {
                parent[rootA] = rootB;
            } else if (rank[rootA] > rank[rootB]) {
                parent[rootB] = rootA;
            } else {
                parent[rootB] = rootA;
                rank[rootA]++;
            }
            return true;
        }

        private int find(int value) {
            if (parent[value] != value) {
                parent[value] = find(parent[value]);
            }
            return parent[value];
        }
    }

    private static class PathNode implements Comparable<PathNode> {
        final int index;
        final float score;

        PathNode(int index, float score) {
            this.index = index;
            this.score = score;
        }

        @Override
        public int compareTo(PathNode other) {
            return Float.compare(score, other.score);
        }
    }
}
