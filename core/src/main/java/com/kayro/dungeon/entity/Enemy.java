package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.util.Difficulty;
import com.kayro.dungeon.world.GameWorld;
import com.kayro.dungeon.world.BiomeType;

public class Enemy extends LivingEntity {
    public final EnemyType type;
    public final Vector2 wanderDirection = new Vector2();
    public final Vector2 actionDirection = new Vector2();
    public EnemyActionState actionState = EnemyActionState.NONE;
    public float wanderTimer;
    public float actionTimer;
    public float specialTimer;
    public int expReward;
    public float detectionRange;
    public float attackRange;
    public boolean rewardGranted;
    public boolean meleeStrikeReady;
    public boolean hasPathTarget;
    public float pathRefreshTimer;
    public final Vector2 pathTarget = new Vector2();

    public Enemy(EnemyType type, float centerX, float centerY, int floor) {
        this(type, centerX, centerY, floor, BiomeType.CATACOMBS, Difficulty.NORMAL);
    }

    public Enemy(EnemyType type, float centerX, float centerY, int floor, BiomeType biome) {
        this(type, centerX, centerY, floor, biome, Difficulty.NORMAL);
    }

    public Enemy(EnemyType type, float centerX, float centerY, int floor, BiomeType biome, Difficulty difficulty) {
        super(centerX - 14f, centerY - 14f, 28f, 28f);
        this.type = type;
        if (type == EnemyType.BOSS) {
            position.set(centerX - 18f, centerY - 18f);
            size.set(36f, 36f);
        }
        BiomeType activeBiome = biome == null ? BiomeType.CATACOMBS : biome;
        Difficulty diff = difficulty == null ? Difficulty.NORMAL : difficulty;
        float hpMultiplier = 1f + floor * diff.enemyHpGrowth;
        float damageMultiplier = 1f + floor * diff.enemyDamageGrowth;
        float speedMultiplier = 1f + floor * 0.02f;
        maxHp = MathUtils.round(type.maxHp * hpMultiplier * activeBiome.enemyHpMultiplier);
        hp = maxHp;
        attack = MathUtils.round(type.attack * damageMultiplier * activeBiome.enemyDamageMultiplier);
        defense = type.defense;
        speed = type.speed * activeBiome.enemySpeedMultiplier * speedMultiplier;
        attackCooldown = type.attackCooldown;
        attackTimer = MathUtils.random(0f, attackCooldown);
        expReward = type.expReward;
        detectionRange = type.detectionRange;
        attackRange = type.attackRange;
        randomizeWander();
    }

    @Override
    public void update(float delta, GameWorld world) {
        attackTimer = Math.max(0f, attackTimer - delta);
        specialTimer = Math.max(0f, specialTimer - delta);
        actionTimer = Math.max(0f, actionTimer - delta);
        pathRefreshTimer = Math.max(0f, pathRefreshTimer - delta);
        updateAnimation(delta);
    }

    public void beginAction(EnemyActionState state, Vector2 direction, float duration) {
        actionState = state;
        actionTimer = duration;
        if (direction.isZero(0.01f)) {
            actionDirection.set(facing.vector);
        } else {
            actionDirection.set(direction).nor();
        }
        updateFacing(actionDirection);
        setAnimationState(AnimationState.ATTACK);
    }

    public void clearAction() {
        actionState = EnemyActionState.NONE;
        actionTimer = 0f;
        actionDirection.setZero();
        hasPathTarget = false;
    }

    public void randomizeWander() {
        float angle = MathUtils.random(0f, 360f);
        wanderDirection.set(MathUtils.cosDeg(angle), MathUtils.sinDeg(angle));
        wanderTimer = MathUtils.random(0.8f, 2.2f);
    }
}
