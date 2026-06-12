package com.kayro.dungeon.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.EnemyActionState;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.entity.Projectile;
import com.kayro.dungeon.world.GameWorld;

public class AISystem {
    private static final float PATH_REFRESH_TIME = 0.35f;
    private static final float PATH_TARGET_REACHED = 10f;
    private static final float SLIME_WINDUP_TIME = 0.36f;
    private static final float SLIME_CHARGE_TIME = 0.30f;
    private static final float SLIME_CHARGE_RANGE = 150f;
    private static final float SLIME_CHARGE_SPEED = 330f;
    private static final float SLIME_CHARGE_COOLDOWN = 1.35f;
    private static final float GOBLIN_MIN_RANGE = 110f;
    private static final float GOBLIN_MAX_RANGE = 180f;
    private static final float GOBLIN_SHOOT_RANGE = 250f;
    private static final float GOBLIN_WINDUP_TIME = 0.42f;
    private static final float GOBLIN_SHOOT_COOLDOWN = 1.25f;
    private static final float GOBLIN_PROJECTILE_SPEED = 260f;
    private static final float HEAVY_WINDUP_TIME = 0.48f;
    private static final float BOSS_SHOOT_RANGE = 280f;
    private static final float BOSS_SHOOT_COOLDOWN = 1.6f;

    private final Vector2 desired = new Vector2();
    private final Vector2 tmp = new Vector2();
    private final PathfindingSystem pathfinder = new PathfindingSystem();

    public void update(GameWorld world, float delta) {
        Vector2 playerCenter = world.player.getCenter();
        for (Enemy enemy : world.enemies) {
            if (enemy.isDead()) {
                continue;
            }
            Vector2 enemyCenter = enemy.getCenter();
            float distance = enemyCenter.dst(playerCenter);

            if (enemy.actionState != EnemyActionState.NONE) {
                updateAction(world, enemy, delta);
                continue;
            }

            switch (enemy.type) {
                case SLIME:
                    updateSlime(world, enemy, enemyCenter, playerCenter, distance, delta);
                    break;
                case GOBLIN:
                    updateGoblin(world, enemy, enemyCenter, playerCenter, distance, delta);
                    break;
                case SKELETON:
                    updateSkeleton(world, enemy, enemyCenter, playerCenter, distance, delta);
                    break;
                case BOSS:
                    updateBoss(world, enemy, enemyCenter, playerCenter, distance, delta);
                    break;
                default:
                    chaseOrWander(world, enemy, enemyCenter, playerCenter, distance, delta);
                    break;
            }
        }
    }

    private void updateAction(GameWorld world, Enemy enemy, float delta) {
        switch (enemy.actionState) {
            case SLIME_WINDUP:
                enemy.setLocomotionState(false);
                if (enemy.actionTimer <= 0f) {
                    enemy.beginAction(EnemyActionState.SLIME_CHARGE, enemy.actionDirection, SLIME_CHARGE_TIME);
                }
                break;
            case SLIME_CHARGE:
                move(world, enemy, enemy.actionDirection, SLIME_CHARGE_SPEED, delta);
                if (enemy.actionTimer <= 0f) {
                    enemy.clearAction();
                    enemy.specialTimer = SLIME_CHARGE_COOLDOWN;
                    enemy.setLocomotionState(false);
                }
                break;
            case RANGED_WINDUP:
                enemy.setLocomotionState(false);
                if (enemy.actionTimer <= 0f) {
                    fireEnemyProjectile(world, enemy);
                    enemy.clearAction();
                    enemy.specialTimer = enemy.type == EnemyType.BOSS ? BOSS_SHOOT_COOLDOWN : GOBLIN_SHOOT_COOLDOWN;
                }
                break;
            case HEAVY_WINDUP:
                enemy.setLocomotionState(false);
                if (enemy.actionTimer <= 0f) {
                    enemy.meleeStrikeReady = true;
                    enemy.clearAction();
                }
                break;
            default:
                enemy.clearAction();
                break;
        }
    }

    private void updateSlime(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                             Vector2 playerCenter, float distance, float delta) {
        if (distance <= SLIME_CHARGE_RANGE && enemy.specialTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.SLIME_WINDUP,
                    tmp.set(playerCenter).sub(enemyCenter), SLIME_WINDUP_TIME, Color.ORANGE);
            return;
        }
        chaseOrWander(world, enemy, enemyCenter, playerCenter, distance, delta);
    }

    private void updateGoblin(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                              Vector2 playerCenter, float distance, float delta) {
        if (distance > enemy.detectionRange) {
            wander(world, enemy, delta);
            return;
        }
        if (distance <= GOBLIN_SHOOT_RANGE && enemy.specialTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.RANGED_WINDUP,
                    tmp.set(playerCenter).sub(enemyCenter), GOBLIN_WINDUP_TIME, Color.CYAN);
            return;
        }
        if (distance < GOBLIN_MIN_RANGE) {
            move(world, enemy, desired.set(enemyCenter).sub(playerCenter), enemy.speed, delta);
            return;
        }
        if (distance > GOBLIN_MAX_RANGE) {
            chaseWithPath(world, enemy, enemyCenter, playerCenter, delta);
            return;
        }
        enemy.updateFacing(tmp.set(playerCenter).sub(enemyCenter));
        enemy.setLocomotionState(false);
    }

    private void updateSkeleton(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                                Vector2 playerCenter, float distance, float delta) {
        if (distance <= enemy.attackRange * 1.1f && enemy.attackTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.HEAVY_WINDUP,
                    tmp.set(playerCenter).sub(enemyCenter), HEAVY_WINDUP_TIME, Color.SCARLET);
            return;
        }
        chaseOrWander(world, enemy, enemyCenter, playerCenter, distance, delta);
    }

    private void updateBoss(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                            Vector2 playerCenter, float distance, float delta) {
        if (distance <= enemy.attackRange * 1.15f && enemy.attackTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.HEAVY_WINDUP,
                    tmp.set(playerCenter).sub(enemyCenter), HEAVY_WINDUP_TIME, Color.SCARLET);
            return;
        }
        if (distance <= BOSS_SHOOT_RANGE && enemy.specialTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.RANGED_WINDUP,
                    tmp.set(playerCenter).sub(enemyCenter), GOBLIN_WINDUP_TIME, Color.CYAN);
            return;
        }
        chaseOrWander(world, enemy, enemyCenter, playerCenter, distance, delta);
    }

    private void chaseOrWander(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                               Vector2 playerCenter, float distance, float delta) {
        if (distance <= enemy.detectionRange) {
            chaseWithPath(world, enemy, enemyCenter, playerCenter, delta);
        } else {
            wander(world, enemy, delta);
        }
    }

    private void chaseWithPath(GameWorld world, Enemy enemy, Vector2 enemyCenter, Vector2 playerCenter, float delta) {
        if (pathfinder.hasWalkableLine(world, enemyCenter, playerCenter)) {
            enemy.hasPathTarget = false;
            move(world, enemy, desired.set(playerCenter).sub(enemyCenter), enemy.speed, delta);
            return;
        }
        if (enemy.pathRefreshTimer <= 0f || !enemy.hasPathTarget
                || enemy.pathTarget.dst2(enemyCenter) <= PATH_TARGET_REACHED * PATH_TARGET_REACHED) {
            enemy.pathRefreshTimer = PATH_REFRESH_TIME;
            enemy.hasPathTarget = pathfinder.findNextStep(world, enemyCenter, playerCenter, enemy.pathTarget);
        }
        if (enemy.hasPathTarget) {
            move(world, enemy, desired.set(enemy.pathTarget).sub(enemyCenter), enemy.speed, delta);
            return;
        }
        move(world, enemy, desired.set(playerCenter).sub(enemyCenter), enemy.speed, delta);
    }

    private void wander(GameWorld world, Enemy enemy, float delta) {
        enemy.hasPathTarget = false;
        enemy.wanderTimer -= delta;
        if (enemy.wanderTimer <= 0f) {
            enemy.randomizeWander();
        }
        move(world, enemy, desired.set(enemy.wanderDirection), enemy.speed, delta);
    }

    private void move(GameWorld world, Enemy enemy, Vector2 direction, float speed, float delta) {
        if (direction.isZero(0.01f)) {
            enemy.setLocomotionState(false);
            return;
        }
        desired.set(direction).nor();
        enemy.updateFacing(desired);
        enemy.setLocomotionState(true);
        world.moveEntity(enemy, desired.x * speed * delta, desired.y * speed * delta);
    }

    private void beginTelegraphedAction(GameWorld world, Enemy enemy, EnemyActionState state,
                                        Vector2 direction, float duration, Color color) {
        enemy.beginAction(state, direction, duration);
        enemy.setLocomotionState(false);
        world.addDamageText("!", enemy.getCenter().x - 4f, enemy.getCenter().y + 24f, color);
    }

    private void fireEnemyProjectile(GameWorld world, Enemy enemy) {
        Vector2 center = enemy.getCenter();
        Vector2 direction = tmp.set(enemy.actionDirection);
        if (direction.isZero(0.01f)) {
            direction.set(enemy.facing.vector);
        }
        direction.nor();
        float speed = enemy.type == EnemyType.BOSS ? GOBLIN_PROJECTILE_SPEED * 1.18f : GOBLIN_PROJECTILE_SPEED;
        world.projectiles.add(new Projectile(center.x + direction.x * 20f,
                center.y + direction.y * 20f, direction, speed, enemy.attack, true));
        if (world.sfx != null) {
            world.sfx.attack();
        }
    }

}
