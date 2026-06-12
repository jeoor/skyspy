package com.kayro.dungeon.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.kayro.dungeon.entity.EnemyType;

public enum BiomeType {
    CATACOMBS("Catacombs",
            new Color(0.56f, 0.56f, 0.56f, 1f),
            new Color(1f, 1f, 1f, 1f),
            new Color(0.78f, 0.82f, 0.84f, 1f),
            0, 0, 0, 1.00f, 1.00f, 1.00f),
    FUNGAL_HOLLOW("Fungal",
            new Color(0.44f, 0.58f, 0.42f, 1f),
            new Color(0.76f, 0.88f, 0.72f, 1f),
            new Color(0.52f, 0.90f, 0.42f, 1f),
            1, 1, 0, 1.04f, 0.96f, 1.06f),
    ASHEN_KEEP("Ashen",
            new Color(0.62f, 0.48f, 0.40f, 1f),
            new Color(1.0f, 0.80f, 0.64f, 1f),
            new Color(1.0f, 0.36f, 0.18f, 1f),
            2, 1, 4, 1.02f, 1.12f, 1.00f),
    FROST_CRYPT("Frost",
            new Color(0.46f, 0.56f, 0.66f, 1f),
            new Color(0.80f, 0.90f, 1.0f, 1f),
            new Color(0.48f, 0.82f, 1.0f, 1f),
            1, 0, 1, 1.10f, 0.98f, 0.92f);

    private static final BiomeType[] VALUES = values();

    public final String label;
    public final Color floorTint;
    public final Color wallTint;
    public final Color accent;
    public final int extraTraps;
    public final int extraEnemies;
    public final int trapDamageBonus;
    public final float enemyHpMultiplier;
    public final float enemyDamageMultiplier;
    public final float enemySpeedMultiplier;

    BiomeType(String label, Color floorTint, Color wallTint, Color accent, int extraTraps,
              int extraEnemies, int trapDamageBonus, float enemyHpMultiplier, float enemyDamageMultiplier,
              float enemySpeedMultiplier) {
        this.label = label;
        this.floorTint = floorTint;
        this.wallTint = wallTint;
        this.accent = accent;
        this.extraTraps = extraTraps;
        this.extraEnemies = extraEnemies;
        this.trapDamageBonus = trapDamageBonus;
        this.enemyHpMultiplier = enemyHpMultiplier;
        this.enemyDamageMultiplier = enemyDamageMultiplier;
        this.enemySpeedMultiplier = enemySpeedMultiplier;
    }

    public static BiomeType forFloor(int floor) {
        return VALUES[Math.max(0, (floor - 1) / 3) % VALUES.length];
    }

    public EnemyType randomEnemy(int floor) {
        float roll = MathUtils.random();
        float slimeWeight = Math.max(0.05f, 0.50f - floor * 0.07f);
        float skeletonWeight = Math.min(0.75f, 0.12f + floor * 0.08f);
        float goblinWeight = 1f - slimeWeight - skeletonWeight;

        if (roll < slimeWeight) {
            return EnemyType.SLIME;
        }
        return roll < slimeWeight + goblinWeight ? EnemyType.GOBLIN : EnemyType.SKELETON;
    }
}
