package com.kayro.dungeon.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kayro.dungeon.world.GameWorld;

public class Player extends LivingEntity {
    private static final float BODY_BOUNDS_WIDTH = 36f;
    private static final float BODY_BOUNDS_HEIGHT = 72f;
    private static final float BODY_BOUNDS_BOTTOM_OFFSET = -2f;
    public static final float SHOOT_PREP_TIME = 1.760f;
    public static final float SHOOT_READY_TIME = 1.671f;
    public static final float SHOOT_BULLET_COOLDOWN = 0.440f;

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
    private final Rectangle collisionBounds = new Rectangle();

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
    public int projectileDamageBonus;
    public float projectileSpeedBonus;
    public float projectileLifeBonus;
    public float projectileKnockbackBonus;
    public int shellBreakDamageBonus;
    public float invincibleTimer;
    public float dashTimer;
    public float dashCooldown = 0.75f;
    public float dashCooldownTimer;
    public float dashShotBoostTimer;
    public float footstepTimer;
    public float skillCooldown = 0.8f;
    public float skillCooldownTimer;
    public float shootPrepTimer;
    public float shootBulletTimer;

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
    public Rectangle getBounds() {
        float centerX = position.x + size.x * 0.5f;
        return collisionBounds.set(centerX - BODY_BOUNDS_WIDTH * 0.5f,
                position.y + BODY_BOUNDS_BOTTOM_OFFSET, BODY_BOUNDS_WIDTH, BODY_BOUNDS_HEIGHT);
    }

    @Override
    public void update(float delta, GameWorld world) {
        attackTimer = Math.max(0f, attackTimer - delta);
        shootPrepTimer = Math.max(0f, shootPrepTimer - delta);
        shootBulletTimer = Math.max(0f, shootBulletTimer - delta);
        invincibleTimer = Math.max(0f, invincibleTimer - delta);
        dashTimer = Math.max(0f, dashTimer - delta);
        dashCooldownTimer = Math.max(0f, dashCooldownTimer - delta);
        dashShotBoostTimer = Math.max(0f, dashShotBoostTimer - delta);
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
        if (hasRelic(RelicType.AFTERIMAGE_STEP)) {
            dashShotBoostTimer = 1.45f;
        }
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
                knockbackBonus += 18f;
                break;
            case LAST_STAND:
                lowHealthAttackBonus += 8;
                dashCooldown = Math.max(0.52f, dashCooldown - 0.04f);
                break;
            case ECHO_LENS:
                projectileDamageBonus += 3;
                projectileSpeedBonus += 70f;
                projectileLifeBonus += 0.10f;
                break;
            case WHITE_SPLINTER:
                shellBreakDamageBonus += 4;
                projectileKnockbackBonus += 12f;
                break;
            case AFTERIMAGE_STEP:
                dashCooldown = Math.max(0.48f, dashCooldown - 0.10f);
                projectileKnockbackBonus += 6f;
                break;
            default:
                break;
        }
    }

    public boolean hasRelic(RelicType relic) {
        return relics.contains(relic, true);
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
        return Math.max(1, attackDamage() + weapon.arrowDamageBonus + projectileDamageBonus);
    }

    public int arrowPierceCount() {
        return arrowPierce + weapon.arrowPierceBonus;
    }

    public float projectileSpeed() {
        return 560f + projectileSpeedBonus;
    }

    public float projectileLife() {
        return 0.95f + projectileLifeBonus;
    }

    public float projectileKnockback() {
        return 34f + knockbackBonus + projectileKnockbackBonus + weapon.knockbackBonus * 0.75f;
    }

    public boolean consumeDashShotBoost() {
        if (dashShotBoostTimer <= 0f) {
            return false;
        }
        dashShotBoostTimer = 0f;
        return true;
    }
}
