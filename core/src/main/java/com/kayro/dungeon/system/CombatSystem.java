package com.kayro.dungeon.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.entity.AnimationState;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.EnemyActionState;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.entity.Item;
import com.kayro.dungeon.entity.ItemType;
import com.kayro.dungeon.entity.Player;
import com.kayro.dungeon.entity.Projectile;
import com.kayro.dungeon.entity.StoryBossKind;
import com.kayro.dungeon.world.GameWorld;

public class CombatSystem {
    private static final float PROJECTILE_HIT_STOP = 0.045f;

    private final Vector2 skillDirection = new Vector2();
    private final Vector2 tmpDirection = new Vector2();
    private final Vector2 tmpKnockback = new Vector2();
    private final Vector2 shotOrigin = new Vector2();
    private final Vector2 shotDirection = new Vector2();

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

        updatePlayerShoot(world);

        for (Enemy enemy : world.enemies) {
            enemy.update(delta, world);
            enemyAttack(world, enemy);
        }

        updateProjectiles(world, delta);
        removeFinishedDeadEnemies(world);
    }

    private void updatePlayerShoot(GameWorld world) {
        Player player = world.player;
        if (!world.input.shootHeld || player.isDead()) {
            return;
        }
        Vector2 direction = aimDirection(world);
        player.updateFacing(direction);
        if (player.shootPrepTimer <= 0f) {
            player.shootPrepTimer = Player.SHOOT_PREP_TIME;
            return;
        }
        if (player.shootPrepTimer >= Player.SHOOT_READY_TIME || player.shootBulletTimer > 0f) {
            return;
        }
        player.shootPrepTimer = Player.SHOOT_READY_TIME;
        player.shootBulletTimer = Player.SHOOT_BULLET_COOLDOWN;
        playerShoot(world, direction);
    }

    private Vector2 aimDirection(GameWorld world) {
        Player player = world.player;
        Vector2 playerCenter = player.getCenter();
        if (world.input.mouseShootHeld) {
            skillDirection.set(world.input.mouseWorld).sub(playerCenter);
            if (skillDirection.isZero(0.01f)) {
                skillDirection.set(player.facing.vector);
            } else {
                skillDirection.nor();
            }
        } else {
            Enemy nearest = findNearestEnemy(world, playerCenter);
            if (nearest != null) {
                skillDirection.set(nearest.getCenter()).sub(playerCenter).nor();
            } else {
                skillDirection.set(player.facing.vector);
            }
        }
        return skillDirection;
    }

    private void playerShoot(GameWorld world, Vector2 direction) {
        Player player = world.player;
        Vector2 origin = eyePosition(player, direction);
        shotDirection.set(direction);
        if (world.input.mouseShootHeld) {
            aimAtMouseFromOrigin(world, origin, shotDirection);
            origin = eyePosition(player, shotDirection);
            aimAtMouseFromOrigin(world, origin, shotDirection);
        }
        player.updateFacing(shotDirection);
        if (world.sfx != null) {
            world.sfx.shoot();
        }
        world.onPlayerShot(shotDirection);
        boolean boosted = player.consumeDashShotBoost();
        int damage = player.arrowDamage() + (boosted ? 6 : 0);
        Projectile projectile = new Projectile(origin.x, origin.y, shotDirection, player.projectileSpeed(), damage);
        projectile.life = player.projectileLife() + (boosted ? 0.08f : 0f);
        projectile.pierceLeft = player.arrowPierceCount();
        projectile.knockback = player.projectileKnockback() + (boosted ? 26f : 0f);
        world.projectiles.add(projectile);
        world.addParticleBurst(origin.x, origin.y, boosted ? Color.GOLD : Color.CYAN, boosted ? 12 : 5);
        if (boosted) {
            world.addDamageText("残影", player.getCenter().x - 12f, player.getCenter().y + 28f, Color.GOLD);
        }
    }

    private void aimAtMouseFromOrigin(GameWorld world, Vector2 origin, Vector2 out) {
        out.set(world.input.mouseWorld).sub(origin);
        if (out.isZero(0.01f)) {
            out.set(world.player.facing.vector);
        } else {
            out.nor();
        }
    }

    private Vector2 eyePosition(Player player, Vector2 direction) {
        float eyeX = player.getCenter().x;
        float eyeY = player.position.y + 68f;
        if (direction != null && !direction.isZero(0.01f)) {
            if (Math.abs(direction.x) > Math.abs(direction.y)) {
                eyeX += Math.signum(direction.x) * 10f;
            } else if (direction.y < -0.35f) {
                eyeY -= 8f;
            } else if (direction.y > 0.35f) {
                eyeY += 4f;
            }
        }
        return shotOrigin.set(eyeX, eyeY);
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
        world.addParticleBurst(player.getCenter().x, player.getCenter().y, Color.SCARLET, 12);
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
            if (world.blockDamageTo(enemy)) {
                projectile.hitEnemies.add(enemy);
                projectile.removed = true;
                world.hitStop(PROJECTILE_HIT_STOP);
                return;
            }
            int beforeHp = enemy.hp;
            int damage = playerDamage(world.player, enemy, projectile.damage);
            enemy.takeDamage(damage);
            world.recordStrongHit(damage);
            projectile.hitEnemies.add(enemy);
            healFromDamage(world, Math.min(beforeHp, damage));
            crackWhiteShell(world, enemy);
            tmpKnockback.set(projectile.velocity);
            if (!tmpKnockback.isZero(0.01f)) {
                tmpKnockback.nor();
                applyKnockback(world, enemy, tmpKnockback, projectile.knockback);
            }
            world.hitStop(PROJECTILE_HIT_STOP);
            if (world.sfx != null) {
                world.sfx.hit();
            }
            world.addDamageText(String.valueOf(damage), enemy.getCenter().x,
                    enemy.getCenter().y + 18f, enemy.isDead() ? Color.GOLD : Color.CYAN);
            world.addParticleBurst(enemy.getCenter().x, enemy.getCenter().y,
                    enemy.isDead() ? Color.GOLD : Color.CYAN, enemy.isDead() ? 20 : 10);
            world.shake(enemy.isDead() ? 4.5f : 2.8f, 0.11f);
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
        if (player.shellBreakDamageBonus > 0 && enemy.whiteHint >= 0.70f) {
            damage += player.shellBreakDamageBonus;
        }
        if (player.critChance() > 0 && MathUtils.random(100f) < player.critChance()) {
            damage = MathUtils.round(damage * 1.75f);
        }
        return damage;
    }

    private void crackWhiteShell(GameWorld world, Enemy enemy) {
        if (enemy.type == EnemyType.CAT || enemy.storyBossKind == StoryBossKind.CAT) {
            return;
        }
        boolean firstCrack = enemy.shellCrackTimer <= 0f;
        enemy.shellCrackTimer = 0.34f;
        if (firstCrack) {
            world.shellBreaks++;
            world.addDamageText("破壳", enemy.getCenter().x - 14f, enemy.getCenter().y + 36f, Color.WHITE);
        }
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
            world.addParticleBurst(player.getCenter().x, player.getCenter().y, Color.GREEN, 6);
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
        int levelsGained = world.levelSystem.gainExp(world.player, enemy.expReward);
        if (levelsGained > 0) {
            world.addDamageText("LEVEL UP", world.player.getCenter().x - 18f,
                    world.player.getCenter().y + 34f, Color.GOLD);
            world.addParticleBurst(world.player.getCenter().x, world.player.getCenter().y, Color.GOLD, 28);
            world.shake(3f, 0.12f);
        }
        if (enemy.storyBossKind != StoryBossKind.NONE) {
            world.items.add(new Item(ItemType.KEY, enemy.getCenter().x, enemy.getCenter().y, 1));
            world.addDamageText("Memory", enemy.getCenter().x - 18f, enemy.getCenter().y + 26f, Color.WHITE);
        } else {
            world.lootSystem.dropForEnemy(world, enemy);
        }
        if (world.sfx != null) {
            world.sfx.death();
        }
    }

    private void removeFinishedDeadEnemies(GameWorld world) {
        for (int i = world.enemies.size - 1; i >= 0; i--) {
            Enemy enemy = world.enemies.get(i);
            if (enemy.storyBossKind == StoryBossKind.CAT && enemy.isDeathAnimationDone()) {
                enemy.removed = false;
                continue;
            }
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
        world.addParticleBurst(player.getCenter().x, player.getCenter().y, Color.SCARLET, 12);
        world.shake(player.isDead() ? 8f : 5f, 0.16f);
    }

    private boolean canEnemyMelee(Enemy enemy) {
        if (enemy.type == EnemyType.GOBLIN) {
            return false;
        }
        if (enemy.type == EnemyType.MIRROR) {
            return false;
        }
        if (enemy.type == EnemyType.SLIME) {
            return enemy.actionState == EnemyActionState.SLIME_CHARGE;
        }
        if (enemy.type == EnemyType.CAT) {
            return enemy.actionState == EnemyActionState.SLIME_CHARGE;
        }
        if (enemy.type == EnemyType.BOSS && enemy.storyBossKind == StoryBossKind.CAT) {
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
