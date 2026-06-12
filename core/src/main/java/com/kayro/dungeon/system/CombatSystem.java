package com.kayro.dungeon.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.entity.AnimationState;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.EnemyActionState;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.entity.Player;
import com.kayro.dungeon.entity.Projectile;
import com.kayro.dungeon.world.GameWorld;

public class CombatSystem {
    private static final float MELEE_HIT_STOP = 0.045f;
    private static final float MELEE_KILL_HIT_STOP = 0.06f;
    private static final float MELEE_KNOCKBACK = 22f;
    private static final float MELEE_KILL_KNOCKBACK = 34f;
    private static final float PROJECTILE_HIT_STOP = 0.03f;
    private static final float PROJECTILE_KNOCKBACK = 18f;

    private final Vector2 skillDirection = new Vector2();
    private final Vector2 tmpFacing = new Vector2();
    private final Vector2 tmpDirection = new Vector2();
    private final Vector2 tmpKnockback = new Vector2();

    public void update(GameWorld world, float delta) {
        if (world.input.usePotionPressed) {
            int before = world.player.hp;
            world.player.usePotion();
            if (world.player.hp > before) {
                world.addDamageText("+" + (world.player.hp - before), world.player.getCenter().x,
                        world.player.getCenter().y + 24f, Color.GREEN);
                if (world.sfx != null) {
                    world.sfx.potion();
                }
            }
        }

        if (world.input.attackPressed && world.player.attackTimer <= 0f) {
            playerAttack(world);
        }

        if (world.input.skillPressed && world.player.skillCooldownTimer <= 0f) {
            playerArrowSkill(world);
        }

        for (Enemy enemy : world.enemies) {
            enemy.update(delta, world);
            enemyAttack(world, enemy);
        }

        updateProjectiles(world, delta);
        removeFinishedDeadEnemies(world);
    }

    private void playerAttack(GameWorld world) {
        Player player = world.player;
        player.attackTimer = player.attackCooldown;
        player.setAnimationState(AnimationState.ATTACK);
        if (world.sfx != null) {
            world.sfx.attack();
        }
        Vector2 playerCenter = player.getCenter();
        float pcx = playerCenter.x;
        float pcy = playerCenter.y;
        tmpFacing.set(player.facing.vector);
        if (world.input.mouseAttackPressed) {
            tmpFacing.set(world.input.mouseWorld).sub(pcx, pcy);
            if (tmpFacing.isZero(0.01f)) {
                tmpFacing.set(player.facing.vector);
            } else {
                tmpFacing.nor();
            }
        }
        world.addAttackEffect(playerCenter, tmpFacing);
        for (int i = world.enemies.size - 1; i >= 0; i--) {
            Enemy enemy = world.enemies.get(i);
            if (enemy.isDead()) {
                continue;
            }
            Vector2 enemyCenter = enemy.getCenter();
            float ecx = enemyCenter.x;
            float ecy = enemyCenter.y;
            float distance = Vector2.dst(pcx, pcy, ecx, ecy);
            if (distance > player.meleeRange()) {
                continue;
            }

            tmpDirection.set(ecx - pcx, ecy - pcy);
            if (tmpDirection.isZero(0.01f)) {
                tmpDirection.set(tmpFacing);
            } else {
                tmpDirection.nor();
            }
            if (tmpDirection.dot(tmpFacing) < 0.15f && distance > 30f) {
                continue;
            }

            int beforeHp = enemy.hp;
            int damage = playerDamage(player, enemy, player.attackDamage());
            enemy.takeDamage(damage);
            healFromDamage(world, Math.min(beforeHp, damage));
            float knockback = (enemy.isDead() ? MELEE_KILL_KNOCKBACK : MELEE_KNOCKBACK)
                    + player.knockbackBonus + player.weaponKnockbackBonus();
            applyKnockback(world, enemy, tmpDirection, knockback);
            world.hitStop(enemy.isDead() ? MELEE_KILL_HIT_STOP : MELEE_HIT_STOP);
            if (world.sfx != null) {
                world.sfx.hit();
            }
            world.addDamageText(String.valueOf(damage), enemy.getCenter().x, enemy.getCenter().y + 18f, Color.WHITE);
            world.shake(enemy.isDead() ? 4f : 2.2f, 0.10f);

            if (enemy.isDead()) {
                finishEnemyKill(world, enemy);
            }
        }
    }

    private void playerArrowSkill(GameWorld world) {
        Player player = world.player;
        Vector2 playerCenter = player.getCenter();
        Enemy nearest = findNearestEnemy(world, playerCenter);
        if (nearest != null) {
            skillDirection.set(nearest.getCenter()).sub(playerCenter).nor();
        } else if (world.input.mouseSkillPressed) {
            skillDirection.set(world.input.mouseWorld).sub(playerCenter);
            if (skillDirection.isZero(0.01f)) {
                skillDirection.set(player.facing.vector);
            } else {
                skillDirection.nor();
            }
        } else {
            skillDirection.set(player.facing.vector);
        }
        player.updateFacing(skillDirection);
        player.skillCooldownTimer = player.skillCooldown;
        player.setAnimationState(AnimationState.SKILL);
        if (world.sfx != null) {
            world.sfx.attack();
        }
        float spawnX = playerCenter.x + skillDirection.x * 24f;
        float spawnY = playerCenter.y + skillDirection.y * 24f;
        Projectile projectile = new Projectile(spawnX, spawnY, skillDirection, 420f, player.arrowDamage());
        projectile.pierceLeft = player.arrowPierceCount();
        world.projectiles.add(projectile);
    }

    private void updateProjectiles(GameWorld world, float delta) {
        for (int i = world.projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = world.projectiles.get(i);
            projectile.update(delta, world);
            if (!projectile.removed) {
                if (projectile.hostile) {
                    hitPlayerWithProjectile(world, projectile);
                } else {
                    hitEnemiesWithProjectile(world, projectile);
                }
            }
            if (projectile.removed) {
                world.projectiles.removeIndex(i);
            }
        }
    }

    private void hitPlayerWithProjectile(GameWorld world, Projectile projectile) {
        Player player = world.player;
        if (player.isDead() || !projectile.getBounds().overlaps(player.getBounds())) {
            return;
        }
        projectile.removed = true;
        if (player.invincibleTimer > 0f) {
            return;
        }

        int damage = Math.max(1, projectile.damage - player.defense);
        player.takeDamage(damage);
        player.invincibleTimer = 0.5f;
        if (world.sfx != null) {
            world.sfx.hit();
            if (player.isDead()) {
                world.sfx.death();
            }
        }
        world.addDamageText("-" + damage, player.getCenter().x, player.getCenter().y + 22f, Color.SCARLET);
        world.shake(player.isDead() ? 8f : 4.5f, 0.14f);
    }

    private void hitEnemiesWithProjectile(GameWorld world, Projectile projectile) {
        Rectangle bounds = projectile.getBounds();
        for (int i = world.enemies.size - 1; i >= 0; i--) {
            Enemy enemy = world.enemies.get(i);
            if (enemy.isDead()) {
                continue;
            }
            if (projectile.hitEnemies.contains(enemy)) {
                continue;
            }
            if (!bounds.overlaps(enemy.getBounds())) {
                continue;
            }
            int beforeHp = enemy.hp;
            int damage = playerDamage(world.player, enemy, projectile.damage);
            enemy.takeDamage(damage);
            projectile.hitEnemies.add(enemy);
            healFromDamage(world, Math.min(beforeHp, damage));
            tmpKnockback.set(projectile.velocity);
            if (!tmpKnockback.isZero(0.01f)) {
                tmpKnockback.nor();
                applyKnockback(world, enemy, tmpKnockback,
                        PROJECTILE_KNOCKBACK + world.player.knockbackBonus * 0.5f);
            }
            world.hitStop(PROJECTILE_HIT_STOP);
            if (world.sfx != null) {
                world.sfx.hit();
            }
            world.addDamageText(String.valueOf(damage), enemy.getCenter().x,
                    enemy.getCenter().y + 18f, Color.CYAN);
            world.shake(enemy.isDead() ? 4f : 2.4f, 0.10f);
            if (projectile.pierceLeft > 0) {
                projectile.pierceLeft--;
            } else {
                projectile.removed = true;
            }
            if (enemy.isDead()) {
                finishEnemyKill(world, enemy);
            }
            return;
        }
    }

    private int playerDamage(Player player, Enemy enemy, int baseDamage) {
        int damage = Math.max(1, baseDamage - enemy.defense);
        if (player.critChance() > 0 && MathUtils.random(100f) < player.critChance()) {
            damage = MathUtils.round(damage * 1.75f);
        }
        return damage;
    }

    private void healFromDamage(GameWorld world, int damage) {
        Player player = world.player;
        if (damage <= 0 || player.lifeStealPercent <= 0 || player.hp >= player.maxHp) {
            return;
        }
        int heal = Math.max(1, damage * player.lifeStealPercent / 100);
        int before = player.hp;
        player.hp = Math.min(player.maxHp, player.hp + heal);
        if (player.hp > before) {
            world.addDamageText("+" + (player.hp - before), player.getCenter().x,
                    player.getCenter().y + 28f, Color.GREEN);
        }
    }

    private void applyKnockback(GameWorld world, Enemy enemy, Vector2 direction, float distance) {
        if (direction.isZero(0.01f) || distance <= 0f) {
            return;
        }
        float appliedDistance = Math.max(4f, distance);
        world.moveEntity(enemy, direction.x * appliedDistance, direction.y * appliedDistance);
    }

    private void finishEnemyKill(GameWorld world, Enemy enemy) {
        if (enemy.rewardGranted) {
            return;
        }
        enemy.rewardGranted = true;
        world.kills++;
        world.levelSystem.gainExp(world.player, enemy.expReward);
        world.lootSystem.dropForEnemy(world, enemy);
        if (world.sfx != null) {
            world.sfx.death();
        }
    }

    private void removeFinishedDeadEnemies(GameWorld world) {
        for (int i = world.enemies.size - 1; i >= 0; i--) {
            Enemy enemy = world.enemies.get(i);
            if (enemy.isDeathAnimationDone()) {
                enemy.removed = true;
                world.enemies.removeIndex(i);
            }
        }
    }

    private void enemyAttack(GameWorld world, Enemy enemy) {
        Player player = world.player;
        if (enemy.attackTimer > 0f || enemy.isDead() || player.isDead()) {
            return;
        }
        if (!canEnemyMelee(enemy)) {
            return;
        }
        if (enemy.getCenter().dst(player.getCenter()) > enemy.attackRange) {
            consumePreparedMelee(enemy);
            return;
        }
        enemy.attackTimer = enemy.attackCooldown;
        consumePreparedMelee(enemy);
        tmpDirection.set(player.getCenter()).sub(enemy.getCenter());
        enemy.updateFacing(tmpDirection);
        enemy.setAnimationState(AnimationState.ATTACK);
        if (player.invincibleTimer > 0f) {
            return;
        }

        int damage = Math.max(1, enemy.attack - player.defense);
        player.takeDamage(damage);
        if (world.sfx != null) {
            world.sfx.hit();
            if (player.isDead()) {
                world.sfx.death();
            }
        }
        player.invincibleTimer = 0.5f;
        world.addDamageText("-" + damage, player.getCenter().x, player.getCenter().y + 22f, Color.SCARLET);
        world.shake(player.isDead() ? 8f : 5f, 0.16f);
    }

    private boolean canEnemyMelee(Enemy enemy) {
        if (enemy.type == EnemyType.GOBLIN) {
            return false;
        }
        if (enemy.type == EnemyType.SLIME) {
            return enemy.actionState == EnemyActionState.SLIME_CHARGE;
        }
        if (enemy.type == EnemyType.SKELETON || enemy.type == EnemyType.BOSS) {
            return enemy.meleeStrikeReady;
        }
        return true;
    }

    private void consumePreparedMelee(Enemy enemy) {
        if (enemy.meleeStrikeReady) {
            enemy.meleeStrikeReady = false;
            enemy.attackTimer = enemy.attackCooldown;
        }
    }

    private Enemy findNearestEnemy(GameWorld world, Vector2 from) {
        Enemy nearest = null;
        float bestDist = Float.MAX_VALUE;
        for (Enemy enemy : world.enemies) {
            if (enemy.isDead()) {
                continue;
            }
            float dist = from.dst2(enemy.getCenter());
            if (dist < bestDist) {
                bestDist = dist;
                nearest = enemy;
            }
        }
        return nearest;
    }
}
