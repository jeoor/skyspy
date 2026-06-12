package com.kayro.dungeon.system;

import com.badlogic.gdx.math.Rectangle;
import com.kayro.dungeon.entity.Entity;
import com.kayro.dungeon.world.DungeonMap;

public class CollisionSystem {
    private final DungeonMap map;

    public CollisionSystem(DungeonMap map) {
        this.map = map;
    }

    public void move(Entity entity, float dx, float dy) {
        if (dx != 0f) {
            if (canMove(entity, dx, 0f)) {
                entity.position.x += dx;
            }
        }
        if (dy != 0f) {
            if (canMove(entity, 0f, dy)) {
                entity.position.y += dy;
            }
        }
    }

    public boolean canMove(Entity entity, float dx, float dy) {
        Rectangle next = entity.getBounds();
        next.x += dx;
        next.y += dy;
        return map.isAreaWalkable(next);
    }
}
