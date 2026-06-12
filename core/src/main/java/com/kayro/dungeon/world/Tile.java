package com.kayro.dungeon.world;

public class Tile {
    public TileType type;
    public boolean visible;
    public boolean explored;

    public Tile(TileType type) {
        this.type = type;
    }

    public boolean isWalkable() {
        return type != TileType.WALL;
    }
}
