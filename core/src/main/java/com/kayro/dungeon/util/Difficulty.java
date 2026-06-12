package com.kayro.dungeon.util;

import com.badlogic.gdx.graphics.Color;

public enum Difficulty {
    EASY("Easy", "More HP, weaker foes",
            new Color(0.32f, 0.82f, 0.40f, 1f),
            1.3f, 0.85f, 0.80f, 3, 0.6f, 1.2f, 0, 0.12f, 0.08f),
    NORMAL("Normal", "Standard challenge",
            new Color(0.96f, 0.70f, 0.28f, 1f),
            1.0f, 1.1f, 1.1f, 2, 1.0f, 0.85f, 2, 0.18f, 0.14f),
    HARD("Hard", "Less HP, brutal enemies",
            new Color(0.92f, 0.28f, 0.22f, 1f),
            0.8f, 1.5f, 1.5f, 1, 1.5f, 0.55f, 4, 0.25f, 0.20f);

    private static final Difficulty[] VALUES = values();

    public final String label;
    public final String description;
    public final Color color;
    public final float playerHpMultiplier;
    public final float enemyHpMultiplier;
    public final float enemyDamageMultiplier;
    public final int startingPotions;
    public final float trapDamageMultiplier;
    public final float spawnIntervalMultiplier;
    public final int extraMaxEnemies;
    public final float enemyHpGrowth;
    public final float enemyDamageGrowth;

    Difficulty(String label, String description, Color color,
              float playerHpMultiplier, float enemyHpMultiplier, float enemyDamageMultiplier,
              int startingPotions, float trapDamageMultiplier, float spawnIntervalMultiplier,
              int extraMaxEnemies, float enemyHpGrowth, float enemyDamageGrowth) {
        this.label = label;
        this.description = description;
        this.color = color;
        this.playerHpMultiplier = playerHpMultiplier;
        this.enemyHpMultiplier = enemyHpMultiplier;
        this.enemyDamageMultiplier = enemyDamageMultiplier;
        this.startingPotions = startingPotions;
        this.trapDamageMultiplier = trapDamageMultiplier;
        this.spawnIntervalMultiplier = spawnIntervalMultiplier;
        this.extraMaxEnemies = extraMaxEnemies;
        this.enemyHpGrowth = enemyHpGrowth;
        this.enemyDamageGrowth = enemyDamageGrowth;
    }

    public Difficulty next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public Difficulty prev() {
        return VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
    }
}
