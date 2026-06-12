package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.MathUtils;

public enum WeaponType {
    SWORD("Sword", 0, 0.35f, 58f, 0f, 0.80f, 8, 0, 0, 0),
    GREATSWORD("Greatsword", 8, 0.56f, 68f, 16f, 1.00f, 8, 0, 0, 8),
    DAGGER("Dagger", -3, 0.22f, 48f, -6f, 0.70f, 5, 0, 10, 20),
    RUNESTAFF("Runestaff", 1, 0.44f, 56f, 0f, 0.58f, 12, 1, 0, 36);

    private static final WeaponType[] VALUES = values();

    public final String label;
    public final int attackBonus;
    public final float attackCooldown;
    public final float meleeRange;
    public final float knockbackBonus;
    public final float skillCooldown;
    public final int arrowDamageBonus;
    public final int arrowPierceBonus;
    public final int critChanceBonus;
    public final int unlockSparks;

    WeaponType(String label, int attackBonus, float attackCooldown, float meleeRange, float knockbackBonus,
               float skillCooldown, int arrowDamageBonus, int arrowPierceBonus, int critChanceBonus,
               int unlockSparks) {
        this.label = label;
        this.attackBonus = attackBonus;
        this.attackCooldown = attackCooldown;
        this.meleeRange = meleeRange;
        this.knockbackBonus = knockbackBonus;
        this.skillCooldown = skillCooldown;
        this.arrowDamageBonus = arrowDamageBonus;
        this.arrowPierceBonus = arrowPierceBonus;
        this.critChanceBonus = critChanceBonus;
        this.unlockSparks = unlockSparks;
    }

    public static int unlockedCount(int sparks) {
        int count = 0;
        for (WeaponType weapon : VALUES) {
            if (sparks >= weapon.unlockSparks) {
                count++;
            }
        }
        return Math.max(1, count);
    }

    public static WeaponType randomUnlocked(int sparks) {
        return VALUES[MathUtils.random(unlockedCount(sparks) - 1)];
    }
}
