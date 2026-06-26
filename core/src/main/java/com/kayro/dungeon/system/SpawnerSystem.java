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
        if (!world.reviewMode && world.floor >= 5) {
            return;
        }
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
        if (!world.reviewMode && world.floor >= 5) {
            reset();
            return;
        }
        if (!world.reviewMode && world.floor == 1) {
            int blankCount = MathUtils.random(4, 6);
            for (int i = 0; i < blankCount; i++) {
                trySpawn(world, true, EnemyType.SLIME);
            }
            reset();
            return;
        }
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
        return trySpawn(world, ignoreVisibility, null);
    }

    private boolean trySpawn(GameWorld world, boolean ignoreVisibility, EnemyType forcedType) {
        for (int attempt = 0; attempt < 80; attempt++) {
            int tileX = MathUtils.random(2, Constants.MAP_WIDTH - 3);
            int tileY = MathUtils.random(2, Constants.MAP_HEIGHT - 3);
            Tile tile = world.map.getTile(tileX, tileY);
            if (!tile.isWalkable()) {
                continue;
            }
            if (!world.map.fallingVoid && !ignoreVisibility && tile.visible) {
                continue;
            }

            float x = world.map.tileCenterX(tileX);
            float y = world.map.tileCenterY(tileY);
            if (world.player.getCenter().dst(x, y) < Constants.TILE_WIDTH * 12f) {
                continue;
            }
            if (new Vector2(x, y).dst(world.map.stairsPosition) < Constants.TILE_WIDTH * 2f) {
                continue;
            }

            world.addEnemy(new Enemy(forcedType == null ? randomType(world) : forcedType,
                    x, y, world.floor, world.biome, world.difficulty));
            return true;
        }
        return false;
    }

    private EnemyType randomType(GameWorld world) {
        if (world.reviewMode) {
            switch (world.floor) {
                case 1:
                    return EnemyType.SLIME;
                case 2:
                    return MathUtils.randomBoolean(0.68f) ? EnemyType.GOBLIN : EnemyType.SLIME;
                case 3:
                    return MathUtils.randomBoolean(0.68f) ? EnemyType.SKELETON : EnemyType.GOBLIN;
                default:
                    return MathUtils.randomBoolean(0.72f) ? EnemyType.MIRROR : EnemyType.SKELETON;
            }
        }
        switch (world.floor) {
            case 1:
                return EnemyType.SLIME;
            case 2:
                return MathUtils.randomBoolean(0.68f) ? EnemyType.GOBLIN : EnemyType.SLIME;
            case 3:
                return MathUtils.randomBoolean(0.68f) ? EnemyType.SKELETON : EnemyType.GOBLIN;
            case 4:
                return MathUtils.randomBoolean(0.72f) ? EnemyType.MIRROR : EnemyType.SKELETON;
            default:
                return EnemyType.MIRROR;
        }
    }
}
