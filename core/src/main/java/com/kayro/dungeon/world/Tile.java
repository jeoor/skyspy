package com.kayro.dungeon.world;

public class Tile {
    public static final int SKY_EMPTY = 0;
    public static final int SKY_PLATE = 1;
    public static final int SKY_SAND = 2;
    public static final int SKY_SAND_GRASS = 3;
    public static final int SKY_PLATE_LOWER_EDGE = 4;
    public static final int SKY_PLATE_UPPER_EDGE = 5;
    public static final int SKY_GRASS = 6;
    public static final int SKY_GRASS_FLOWERS = 7;
    public static final int SKY_GRASS_LOWER_EDGE = 8;
    public static final int SKY_GRASS_UPPER_EDGE = 9;
    public static final int SKY_GRATE = 10;

    public TileType type;
    public boolean visible;
    public boolean explored;
    public boolean edgeNorth;
    public boolean edgeSouth;
    public boolean edgeWest;
    public boolean edgeEast;
    public boolean grass;
    public int variant;
    public int skyKind;
    public int skyMask;

    public Tile(TileType type) {
        this.type = type;
    }

    public boolean isWalkable() {
        return type != TileType.VOID;
    }
}
