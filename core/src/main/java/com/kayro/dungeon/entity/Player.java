package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kayro.dungeon.world.GameWorld;

public class Player extends LivingEntity {

    public static class State {
        public int hp, maxHp, attack, defense, level, exp, gold, keys, potions;
        public WeaponType weapon;
        public final Array<RelicType> relics = new Array<>();
    }

    public State saveState() {
        State s = new State();
        s.hp = hp;
        s.maxHp = maxHp;
        s.attack = attack;
        s.defense = defense;
        s.level = level;
        s.exp = exp;
        s.gold = gold;
        s.keys = keys;
        s.potions = potions;
        s.weapon = weapon;
        s.relics.addAll(relics);
        return s;
    }

    public void restoreState(State s) {
        hp = s.hp;
        maxHp = s.maxHp;
        attack = s.attack;
        defense = s.defense;
        level = s.level;
        exp = s.exp;
        gold = s.gold;
        keys = s.keys;
        potions = s.potions;
        equipWeapon(s.weapon);
        for (RelicType relic : s.relics) {
            addRelic(relic);
        }
    }

    private final Vector2 dashDirection = new Vector2();

    public final Array<RelicType> relics = new Array<>();
    public WeaponType weapon = WeaponType.SWORD;
    public int level = 1;
    public int exp = 0;
    public int gold = 0;
    public int keys = 0;
    public int potions = 2;
    public int critChanceBonus;
    public int lifeStealPercent;
    public int arrowPierce;
    public float knockbackBonus;
    public int lowHealthAttackBonus;
    public float invincibleTimer;
    public float dashTimer;
    public float dashCooldown = 0.75f;
    public float dashCooldownTimer;
    public float footstepTimer;
    public float skillCooldown = 0.8f;
    public float skillCooldownTimer;

    public Player(float centerX, float centerY) {
        super(centerX - 14f, centerY - 14f, 28f, 28f);
        maxHp = 100;
        hp = maxHp;
        attack = 15;
        defense = 2;
        speed = 150f;
        equipWeapon(WeaponType.SWORD);
    }

    @Override
    public void update(float delta, GameWorld world) {
        attackTimer = Math.max(0f, attackTimer - delta);
        invincibleTimer = Math.max(0f, invincibleTimer - delta);
        dashTimer = Math.max(0f, dashTimer - delta);
        dashCooldownTimer = Math.max(0f, dashCooldownTimer - delta);
        skillCooldownTimer = Math.max(0f, skillCooldownTimer - delta);
        footstepTimer = Math.max(0f, footstepTimer - delta);
        if (isDead()) {
            updateAnimation(delta);
            return;
        }

        if (world.input.dashPressed && dashCooldownTimer <= 0f) {
            startDash(world);
        }

        boolean moving = false;
        if (dashTimer > 0f) {
            world.moveEntity(this, dashDirection.x * speed * 3.2f * delta,
                    dashDirection.y * speed * 3.2f * delta);
            moving = true;
        } else if (!world.input.moveDirection.isZero(0.01f)) {
            Vector2 direction = world.input.moveDirection;
            world.moveEntity(this, direction.x * speed * delta, direction.y * speed * delta);
            updateFacing(direction);
            moving = true;
            if (footstepTimer <= 0f && world.sfx != null) {
                world.sfx.footstep();
                footstepTimer = 0.32f;
            }
        }
        setLocomotionState(moving);
        updateAnimation(delta);
    }

    private void startDash(GameWorld world) {
        if (!world.input.moveDirection.isZero(0.01f)) {
            dashDirection.set(world.input.moveDirection);
            updateFacing(dashDirection);
        } else {
            dashDirection.set(facing.vector);
        }
        dashTimer = 0.16f;
        dashCooldownTimer = dashCooldown;
        invincibleTimer = Math.max(invincibleTimer, 0.18f);
        if (world.sfx != null) {
            world.sfx.dash();
        }
    }

    public void usePotion() {
        if (potions <= 0 || hp >= maxHp) {
            return;
        }
        potions--;
        hp = Math.min(maxHp, hp + 35);
    }

    public void addRelic(RelicType relic) {
        relics.add(relic);
        switch (relic) {
            case CRIT_CHARM:
                critChanceBonus += 12;
                break;
            case VAMPIRE_FANG:
                lifeStealPercent += 8;
                break;
            case PIERCING_QUIVER:
                arrowPierce += 1;
                break;
            case IRON_GRIP:
                knockbackBonus += 8f;
                break;
            case LAST_STAND:
                lowHealthAttackBonus += 5;
                break;
            default:
                break;
        }
    }

    public void equipWeapon(WeaponType weapon) {
        this.weapon = weapon == null ? WeaponType.SWORD : weapon;
        attackCooldown = this.weapon.attackCooldown;
        skillCooldown = this.weapon.skillCooldown;
        attackTimer = Math.min(attackTimer, attackCooldown);
        skillCooldownTimer = Math.min(skillCooldownTimer, skillCooldown);
    }

    public int attackDamage() {
        int total = attack + weapon.attackBonus;
        if (hp <= maxHp * 0.35f) {
            total += lowHealthAttackBonus;
        }
        return Math.max(1, total);
    }

    public int critChance() {
        return critChanceBonus + weapon.critChanceBonus;
    }

    public float meleeRange() {
        return weapon.meleeRange;
    }

    public float weaponKnockbackBonus() {
        return weapon.knockbackBonus;
    }

    public int arrowDamage() {
        return attackDamage() + weapon.arrowDamageBonus;
    }

    public int arrowPierceCount() {
        return arrowPierce + weapon.arrowPierceBonus;
    }

}
