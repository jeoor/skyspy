package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.util.Difficulty;
import com.kayro.dungeon.world.GameWorld;
import com.kayro.dungeon.world.BiomeType;

public class Enemy extends LivingEntity {
    private static final float BODY_BOUNDS_WIDTH = 36f;
    private static final float BODY_BOUNDS_HEIGHT = 72f;
    private static final float BOSS_BOUNDS_WIDTH = 44f;
    private static final float BOSS_BOUNDS_HEIGHT = 82f;
    private static final float CAT_BOUNDS_WIDTH = 32f;
    private static final float CAT_BOUNDS_HEIGHT = 24f;

    public final EnemyType type;
    public StoryBossKind storyBossKind = StoryBossKind.NONE;
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
    public int storyPhase;
    public final int whiteHintCount;
    public final float whiteHint;
    public float shellCrackTimer;
    public float pathRefreshTimer;
    public final Vector2 pathTarget = new Vector2();
    private final Rectangle collisionBounds = new Rectangle();

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
        } else if (type == EnemyType.CAT) {
            position.set(centerX - 12f, centerY - 10f);
            size.set(24f, 20f);
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
        whiteHintCount = MathUtils.clamp(floor, 1, 10);
        float layerWhite = MathUtils.clamp(0.96f - (whiteHintCount - 1) * 0.14f, 0.40f, 0.96f);
        whiteHint = type == EnemyType.BOSS
                ? Math.min(1f, layerWhite + 0.08f)
                : type == EnemyType.MIRROR ? Math.min(layerWhite, 0.56f) : layerWhite;
        randomizeWander();
    }

    @Override
    public Rectangle getBounds() {
        float width = type == EnemyType.BOSS ? BOSS_BOUNDS_WIDTH
                : type == EnemyType.CAT ? CAT_BOUNDS_WIDTH : BODY_BOUNDS_WIDTH;
        float height = type == EnemyType.BOSS ? BOSS_BOUNDS_HEIGHT
                : type == EnemyType.CAT ? CAT_BOUNDS_HEIGHT : BODY_BOUNDS_HEIGHT;
        float centerX = position.x + size.x * 0.5f;
        return collisionBounds.set(centerX - width * 0.5f, position.y - 2f, width, height);
    }

    @Override
    public void update(float delta, GameWorld world) {
        attackTimer = Math.max(0f, attackTimer - delta);
        specialTimer = Math.max(0f, specialTimer - delta);
        actionTimer = Math.max(0f, actionTimer - delta);
        pathRefreshTimer = Math.max(0f, pathRefreshTimer - delta);
        shellCrackTimer = Math.max(0f, shellCrackTimer - delta);
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
        setAnimationState(state == EnemyActionState.SLIME_CHARGE
                ? AnimationState.WALK
                : AnimationState.ATTACK);
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
