package com.kayro.dungeon.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.EnemyActionState;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.entity.Projectile;
import com.kayro.dungeon.entity.StoryBossKind;
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
    private static final float PLEASER_MIN_RANGE = 96f;
    private static final float PLEASER_SHOOT_RANGE = 210f;
    private static final float PLEASER_SHOOT_COOLDOWN = 1.55f;
    private static final float CAT_BOSS_POUNCE_RANGE = 190f;
    private static final float CAT_BOSS_POUNCE_COOLDOWN = 1.05f;
    private static final float CAT_CRAWLER_POUNCE_RANGE = 145f;
    private static final float CAT_CRAWLER_POUNCE_COOLDOWN = 1.45f;
    private static final float MIRROR_BOSS_MIN_RANGE = 118f;
    private static final float MIRROR_BOSS_SHOOT_RANGE = 260f;
    private static final float MIRROR_BOSS_SHOOT_COOLDOWN = 1.35f;
    private static final float MIRROR_MIN_RANGE = 92f;
    private static final float MIRROR_SHOOT_RANGE = 210f;
    private static final float MIRROR_SHOOT_COOLDOWN = 1.45f;

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
                case CAT:
                    updateCatCrawler(world, enemy, enemyCenter, playerCenter, distance, delta);
                    break;
                case GOBLIN:
                    updateGoblin(world, enemy, enemyCenter, playerCenter, distance, delta);
                    break;
                case SKELETON:
                    updateSkeleton(world, enemy, enemyCenter, playerCenter, distance, delta);
                    break;
                case MIRROR:
                    updateMirror(world, enemy, enemyCenter, playerCenter, distance, delta);
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
                if (!move(world, enemy, enemy.actionDirection, SLIME_CHARGE_SPEED, delta)) {
                    enemy.clearAction();
                    enemy.specialTimer = chargeCooldownFor(enemy);
                    enemy.setLocomotionState(false);
                    break;
                }
                if (enemy.actionTimer <= 0f) {
                    enemy.clearAction();
                    enemy.specialTimer = chargeCooldownFor(enemy);
                    enemy.setLocomotionState(false);
                }
                break;
            case RANGED_WINDUP:
                enemy.setLocomotionState(false);
                if (enemy.actionTimer <= 0f) {
                    fireEnemyProjectile(world, enemy);
                    enemy.clearAction();
                    enemy.specialTimer = rangedCooldownFor(enemy);
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

    private void updateCatCrawler(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                                  Vector2 playerCenter, float distance, float delta) {
        if (distance <= CAT_CRAWLER_POUNCE_RANGE && enemy.specialTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.SLIME_WINDUP,
                    tmp.set(playerCenter).sub(enemyCenter), SLIME_WINDUP_TIME, Color.WHITE);
            enemy.specialTimer = CAT_CRAWLER_POUNCE_COOLDOWN;
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
            retreat(world, enemy, desired.set(enemyCenter).sub(playerCenter), enemy.speed, delta);
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

    private void updateMirror(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                              Vector2 playerCenter, float distance, float delta) {
        if (distance > enemy.detectionRange) {
            wander(world, enemy, delta);
            return;
        }
        if (distance < MIRROR_MIN_RANGE) {
            retreat(world, enemy, desired.set(enemyCenter).sub(playerCenter), enemy.speed * 0.72f, delta);
            return;
        }
        if (distance <= MIRROR_SHOOT_RANGE && enemy.specialTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.RANGED_WINDUP,
                    tmp.set(world.mirrorDelayedTarget()).sub(enemyCenter), GOBLIN_WINDUP_TIME, Color.WHITE);
            enemy.specialTimer = MIRROR_SHOOT_COOLDOWN;
            return;
        }
        enemy.updateFacing(tmp.set(world.mirrorDelayedTarget()).sub(enemyCenter));
        enemy.setLocomotionState(false);
    }

    private void updateBoss(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                            Vector2 playerCenter, float distance, float delta) {
        if (world.isFinalBossDebating(enemy)) {
            enemy.updateFacing(tmp.set(playerCenter).sub(enemyCenter));
            enemy.setLocomotionState(false);
            return;
        }
        if (world.isFinalBossDefending(enemy)) {
            enemy.updateFacing(tmp.set(playerCenter).sub(enemyCenter));
            enemy.setLocomotionState(false);
            enemy.clearAction();
            return;
        }
        if (enemy.storyBossKind == StoryBossKind.PLEASER) {
            updatePleaserBoss(world, enemy, enemyCenter, playerCenter, distance, delta);
            return;
        }
        if (enemy.storyBossKind == StoryBossKind.CAT) {
            updateCatBoss(world, enemy, enemyCenter, playerCenter, distance, delta);
            return;
        }
        if (enemy.storyBossKind == StoryBossKind.MIRROR) {
            updateMirrorBoss(world, enemy, enemyCenter, playerCenter, distance, delta);
            return;
        }
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

    private void updatePleaserBoss(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                                   Vector2 playerCenter, float distance, float delta) {
        float health = healthRatio(enemy);
        if (health > 0.60f) {
            announcePhase(world, enemy, 1, "Please");
            if (enemy.specialTimer <= 0f) {
                summonGuard(world, enemy, EnemyType.SLIME, -1);
                summonGuard(world, enemy, EnemyType.SLIME, 1);
                enemy.specialTimer = 3.2f;
                return;
            }
            if (distance < PLEASER_MIN_RANGE) {
                retreat(world, enemy, desired.set(enemyCenter).sub(playerCenter), enemy.speed * 0.52f, delta);
                return;
            }
            enemy.updateFacing(tmp.set(playerCenter).sub(enemyCenter));
            enemy.setLocomotionState(false);
            return;
        }
        if (health <= 0.30f) {
            announcePhase(world, enemy, 3, "No");
            if (distance <= enemy.attackRange * 1.75f && enemy.attackTimer <= 0f) {
                beginTelegraphedAction(world, enemy, EnemyActionState.HEAVY_WINDUP,
                        tmp.set(playerCenter).sub(enemyCenter), HEAVY_WINDUP_TIME * 1.15f, Color.WHITE);
                return;
            }
            if (distance < PLEASER_MIN_RANGE * 1.35f) {
                retreat(world, enemy, desired.set(enemyCenter).sub(playerCenter), enemy.speed * 0.68f, delta);
                return;
            }
            enemy.updateFacing(tmp.set(playerCenter).sub(enemyCenter));
            enemy.setLocomotionState(false);
            return;
        }
        announcePhase(world, enemy, 2, "Quiet");
        if (distance < PLEASER_MIN_RANGE) {
            retreat(world, enemy, desired.set(enemyCenter).sub(playerCenter), enemy.speed * 0.62f, delta);
            return;
        }
        if (distance <= PLEASER_SHOOT_RANGE && enemy.specialTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.RANGED_WINDUP,
                    tmp.set(playerCenter).sub(enemyCenter), GOBLIN_WINDUP_TIME, Color.WHITE);
            enemy.specialTimer = PLEASER_SHOOT_COOLDOWN;
            return;
        }
        enemy.updateFacing(tmp.set(playerCenter).sub(enemyCenter));
        enemy.setLocomotionState(false);
    }

    private void updateCatBoss(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                               Vector2 playerCenter, float distance, float delta) {
        float health = healthRatio(enemy);
        float windup = SLIME_WINDUP_TIME;
        if (health <= 0.30f) {
            announcePhase(world, enemy, 3, "It slows");
            windup = SLIME_WINDUP_TIME * 1.85f;
            if (distance > CAT_BOSS_POUNCE_RANGE * 0.62f) {
                chaseWithPath(world, enemy, enemyCenter, playerCenter, delta);
                return;
            }
            enemy.updateFacing(tmp.set(playerCenter).sub(enemyCenter));
            enemy.setLocomotionState(false);
            if (distance <= CAT_BOSS_POUNCE_RANGE * 0.62f && enemy.specialTimer <= 0f) {
                beginTelegraphedAction(world, enemy, EnemyActionState.SLIME_WINDUP,
                        tmp.set(playerCenter).sub(enemyCenter), windup, Color.WHITE);
                enemy.specialTimer = CAT_BOSS_POUNCE_COOLDOWN * 1.9f;
            }
            return;
        }
        if (health <= 0.60f) {
            announcePhase(world, enemy, 2, "It looks back");
            windup = SLIME_WINDUP_TIME * 1.35f;
        }
        if (distance <= CAT_BOSS_POUNCE_RANGE && enemy.specialTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.SLIME_WINDUP,
                    tmp.set(playerCenter).sub(enemyCenter), windup, Color.WHITE);
            enemy.specialTimer = health <= 0.60f ? CAT_BOSS_POUNCE_COOLDOWN * 1.45f : CAT_BOSS_POUNCE_COOLDOWN;
            return;
        }
        chaseWithPath(world, enemy, enemyCenter, playerCenter, delta);
    }

    private void updateMirrorBoss(GameWorld world, Enemy enemy, Vector2 enemyCenter,
                                  Vector2 playerCenter, float distance, float delta) {
        if (world.mirrorStillVisualAlpha() > 0f) {
            enemy.updateFacing(tmp.set(playerCenter).sub(enemyCenter));
            enemy.setLocomotionState(false);
            return;
        }
        float health = healthRatio(enemy);
        if (health <= 0.50f) {
            announcePhase(world, enemy, 2, "Hands down");
        } else {
            announcePhase(world, enemy, 1, "After me");
        }
        if (distance < MIRROR_BOSS_MIN_RANGE) {
            retreat(world, enemy, desired.set(enemyCenter).sub(playerCenter), enemy.speed * 0.75f, delta);
            return;
        }
        if (distance <= MIRROR_BOSS_SHOOT_RANGE && enemy.specialTimer <= 0f) {
            beginTelegraphedAction(world, enemy, EnemyActionState.RANGED_WINDUP,
                    tmp.set(world.mirrorDelayedTarget()).sub(enemyCenter),
                    health <= 0.50f ? GOBLIN_WINDUP_TIME * 0.72f : GOBLIN_WINDUP_TIME, Color.WHITE);
            enemy.specialTimer = health <= 0.50f
                    ? MIRROR_BOSS_SHOOT_COOLDOWN * 0.72f
                    : MIRROR_BOSS_SHOOT_COOLDOWN;
            return;
        }
        enemy.updateFacing(tmp.set(world.mirrorDelayedTarget()).sub(enemyCenter));
        enemy.setLocomotionState(false);
    }

    private float healthRatio(Enemy enemy) {
        return enemy.maxHp <= 0 ? 0f : enemy.hp / (float) enemy.maxHp;
    }

    private void announcePhase(GameWorld world, Enemy enemy, int phase, String text) {
        if (enemy.storyPhase >= phase) {
            return;
        }
        enemy.storyPhase = phase;
        world.addDamageText(text, enemy.getCenter().x - 18f, enemy.getCenter().y + 32f, Color.WHITE);
        world.shake(1.8f, 0.12f);
    }

    private void summonGuard(GameWorld world, Enemy enemy, EnemyType type, int side) {
        Vector2 center = enemy.getCenter();
        float x = center.x + side * 58f;
        float y = center.y - 28f;
        if (!world.map.isWalkableWorld(x, y)) {
            y = center.y + 28f;
        }
        if (!world.map.isWalkableWorld(x, y)) {
            return;
        }
        Enemy guard = new Enemy(type, x, y, world.floor, world.biome, world.difficulty);
        guard.specialTimer = 0.7f;
        world.addEnemy(guard);
        world.addDamageText("White self", x - 22f, y + 24f, Color.WHITE);
    }

    private float chargeCooldownFor(Enemy enemy) {
        if (enemy.type == EnemyType.CAT) {
            return CAT_CRAWLER_POUNCE_COOLDOWN;
        }
        if (enemy.storyBossKind == StoryBossKind.CAT) {
            return healthRatio(enemy) <= 0.30f
                    ? CAT_BOSS_POUNCE_COOLDOWN * 1.9f
                    : CAT_BOSS_POUNCE_COOLDOWN;
        }
        return SLIME_CHARGE_COOLDOWN;
    }

    private float rangedCooldownFor(Enemy enemy) {
        if (enemy.storyBossKind == StoryBossKind.PLEASER) {
            return PLEASER_SHOOT_COOLDOWN;
        }
        if (enemy.storyBossKind == StoryBossKind.MIRROR) {
            return healthRatio(enemy) <= 0.50f
                    ? MIRROR_BOSS_SHOOT_COOLDOWN * 0.72f
                    : MIRROR_BOSS_SHOOT_COOLDOWN;
        }
        if (enemy.type == EnemyType.MIRROR) {
            return MIRROR_SHOOT_COOLDOWN;
        }
        return enemy.type == EnemyType.BOSS ? BOSS_SHOOT_COOLDOWN : GOBLIN_SHOOT_COOLDOWN;
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
        if (enemy.pathRefreshTimer <= 0f || !enemy.hasPathTarget
                || enemy.pathTarget.dst2(enemyCenter) <= PATH_TARGET_REACHED * PATH_TARGET_REACHED) {
            enemy.pathRefreshTimer = PATH_REFRESH_TIME;
            enemy.hasPathTarget = pathfinder.findNextStep(world, enemyCenter, playerCenter, enemy.pathTarget);
        }
        if (enemy.hasPathTarget) {
            move(world, enemy, desired.set(enemy.pathTarget).sub(enemyCenter), enemy.speed, delta);
            return;
        }
        enemy.setLocomotionState(false);
    }

    private void wander(GameWorld world, Enemy enemy, float delta) {
        enemy.hasPathTarget = false;
        enemy.wanderTimer -= delta;
        if (enemy.wanderTimer <= 0f) {
            enemy.randomizeWander();
        }
        if (!move(world, enemy, desired.set(enemy.wanderDirection), enemy.speed, delta)) {
            enemy.randomizeWander();
        }
    }

    private void retreat(GameWorld world, Enemy enemy, Vector2 direction, float speed, float delta) {
        if (move(world, enemy, direction, speed, delta)) {
            return;
        }
        desired.set(direction).nor();
        tmp.set(-desired.y, desired.x);
        if (enemy.wanderDirection.dot(tmp) < 0f) {
            tmp.scl(-1f);
        }
        if (!move(world, enemy, tmp, speed, delta)) {
            move(world, enemy, tmp.scl(-1f), speed, delta);
        }
    }

    private boolean move(GameWorld world, Enemy enemy, Vector2 direction, float speed, float delta) {
        if (direction.isZero(0.01f)) {
            enemy.setLocomotionState(false);
            return false;
        }
        desired.set(direction).nor();
        float dx = desired.x * speed * delta;
        float dy = desired.y * speed * delta;
        if (!isSafeGroundStep(world, enemy, dx, dy)) {
            enemy.hasPathTarget = false;
            enemy.pathRefreshTimer = 0f;
            enemy.setLocomotionState(false);
            return false;
        }
        enemy.updateFacing(desired);
        enemy.setLocomotionState(true);
        world.moveEntity(enemy, dx, dy);
        return true;
    }

    private boolean isSafeGroundStep(GameWorld world, Enemy enemy, float dx, float dy) {
        Vector2 center = enemy.getCenter();
        return world.map.isWalkableWorld(center.x + dx * 0.5f, center.y + dy * 0.5f)
                && world.map.isWalkableWorld(center.x + dx, center.y + dy);
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
