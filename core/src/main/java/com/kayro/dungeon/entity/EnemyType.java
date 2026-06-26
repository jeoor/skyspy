package com.kayro.dungeon.entity;

public enum EnemyType {
    SLIME(30, 6, 0, 55f, 8, 1.1f, 160f, 34f),
    CAT(24, 5, 0, 112f, 10, 1.0f, 190f, 28f),
    GOBLIN(45, 10, 1, 85f, 15, 0.85f, 220f, 34f),
    SKELETON(70, 14, 2, 60f, 25, 1.15f, 190f, 38f),
    MIRROR(58, 11, 1, 68f, 22, 1.0f, 230f, 34f),
    BOSS(165, 18, 4, 62f, 90, 0.9f, 300f, 48f);

    public final int maxHp;
    public final int attack;
    public final int defense;
    public final float speed;
    public final int expReward;
    public final float attackCooldown;
    public final float detectionRange;
    public final float attackRange;

    EnemyType(int maxHp, int attack, int defense, float speed, int expReward,
              float attackCooldown, float detectionRange, float attackRange) {
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.expReward = expReward;
        this.attackCooldown = attackCooldown;
        this.detectionRange = detectionRange;
        this.attackRange = attackRange;
    }
}
