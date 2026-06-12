package com.kayro.dungeon.system;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;
import com.kayro.dungeon.world.Tile;

public class SpawnerSystem {
    private float timer;

    public void reset() {
        timer = 1f;
    }

    public void update(GameWorld world, float delta) {
        timer -= delta;
        if (timer > 0f) {
            return;
        }
        timer = spawnInterval(world.floor) * world.difficulty.spawnIntervalMultiplier;
        if (world.enemies.size < maxEnemies(world)) {
            trySpawn(world, false);
        }
    }

    public void spawnInitial(GameWorld world) {
        int count = MathUtils.random(4, 6 + Math.min(4, world.floor));
        for (int i = 0; i < count; i++) {
            trySpawn(world, true);
        }
        reset();
    }

    public int maxEnemies(GameWorld world) {
        return 8 + world.floor * 2 + (world.biome == null ? 0 : world.biome.extraEnemies)
                + world.difficulty.extraMaxEnemies;
    }

    public float spawnInterval(int floor) {
        return Math.max(1.2f, 3.0f - floor * 0.1f);
    }

    public boolean trySpawn(GameWorld world, boolean ignoreVisibility) {
        for (int attempt = 0; attempt < 80; attempt++) {
            int tileX = MathUtils.random(2, Constants.MAP_WIDTH - 3);
            int tileY = MathUtils.random(2, Constants.MAP_HEIGHT - 3);
            Tile tile = world.map.getTile(tileX, tileY);
            if (!tile.isWalkable()) {
                continue;
            }
            if (!ignoreVisibility && tile.visible) {
                continue;
            }

            float x = (tileX + 0.5f) * Constants.TILE_SIZE;
            float y = (tileY + 0.5f) * Constants.TILE_SIZE;
            if (world.player.getCenter().dst(x, y) < Constants.TILE_SIZE * 12f) {
                continue;
            }
            if (new Vector2(x, y).dst(world.map.stairsPosition) < Constants.TILE_SIZE * 2f) {
                continue;
            }

            world.addEnemy(new Enemy(randomType(world), x, y, world.floor, world.biome, world.difficulty));
            return true;
        }
        return false;
    }

    private EnemyType randomType(GameWorld world) {
        return world.biome == null ? EnemyType.SLIME : world.biome.randomEnemy(world.floor);
    }
}
