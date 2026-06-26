package com.kayro.dungeon.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.kayro.dungeon.asset.GameAssets;
import com.kayro.dungeon.entity.AnimationState;
import com.kayro.dungeon.entity.AttackEffect;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.entity.DamageText;
import com.kayro.dungeon.entity.DecorProp;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.EnemyActionState;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.entity.Item;
import com.kayro.dungeon.entity.ItemType;
import com.kayro.dungeon.entity.Particle;
import com.kayro.dungeon.entity.Shop;
import com.kayro.dungeon.entity.Projectile;
import com.kayro.dungeon.entity.StoryBossKind;
import com.kayro.dungeon.entity.Trap;
import com.kayro.dungeon.entity.PropType;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.util.Direction;
import com.kayro.dungeon.world.GameWorld;
import com.kayro.dungeon.world.Tile;
import com.kayro.dungeon.world.TileType;

public class WorldRenderer {
    private static final Color FLOOR_TINT = new Color(0.56f, 0.56f, 0.56f, 1f);
    private static final Color STAIR_TINT = new Color(1f, 1f, 1f, 1f);
    private static final Color SKY_FLOOR_TINT = new Color(0.78f, 0.88f, 0.78f, 1f);
    private static final Color SKY_EDGE_TINT = new Color(0.62f, 0.76f, 0.66f, 1f);
    private static final Color SKY_RIM_TINT = new Color(0.92f, 1.00f, 0.90f, 1f);
    private static final Color SKY_CLIFF_TINT = new Color(0.22f, 0.30f, 0.28f, 1f);
    private static final Color SKY_CLIFF_SIDE_TINT = new Color(0.16f, 0.22f, 0.22f, 0.92f);
    private static final Color SKY_SHADOW_TINT = new Color(0.03f, 0.04f, 0.08f, 0.36f);
    private static final Color SKY_GRADIENT_BOTTOM = new Color(17f / 255f, 45f / 255f, 50f / 255f, 1f);
    private static final Color SKY_GRADIENT_MID = new Color(0.34f, 0.54f, 0.66f, 1f);
    private static final Color SKY_GRADIENT_TOP = new Color(0.70f, 0.82f, 0.90f, 1f);
    private static final Color BLIND_JUMP_MAP_MULTIPLY = new Color(188f / 255f, 188f / 255f, 198f / 255f, 1f);
    private static final Color TRAP_TRIGGERED_TINT = new Color(1f, 0.42f, 0.36f, 1f);
    private static final Color BOSS_CHEST_TINT = new Color(1f, 0.78f, 0.30f, 1f);
    private static final Color PROMPT_OUTLINE = new Color(0.04f, 0.08f, 0.14f, 0.90f);
    private static final float PIXEL_SCALE = 3f;
    private static final float BOSS_PIXEL_SCALE = PIXEL_SCALE * 1.15f;
    private static final float SKY_CLIFF_DEPTH = 18f;

    private final Color gradientBandBottom = new Color();
    private final Color gradientBandTop = new Color();

    public void render(GameWorld world, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font,
                       GameAssets assets, OrthographicCamera camera) {
        float left = camera.position.x - camera.viewportWidth * camera.zoom * 0.5f;
        float right = camera.position.x + camera.viewportWidth * camera.zoom * 0.5f;
        float bottom = camera.position.y - camera.viewportHeight * camera.zoom * 0.5f;
        float top = camera.position.y + camera.viewportHeight * camera.zoom * 0.5f;

        int startX = MathUtils.clamp(MathUtils.floor(left / Constants.TILE_WIDTH) - 1, 0, Constants.MAP_WIDTH - 1);
        int endX = MathUtils.clamp(MathUtils.ceil(right / Constants.TILE_WIDTH) + 1, 0, Constants.MAP_WIDTH - 1);
        int startY = MathUtils.clamp(MathUtils.floor(bottom / Constants.TILE_HEIGHT) - 1, 0, Constants.MAP_HEIGHT - 1);
        int endY = MathUtils.clamp(MathUtils.ceil(top / Constants.TILE_HEIGHT) + 1, 0, Constants.MAP_HEIGHT - 1);

        if (assets.hasSkySpyTiles()) {
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            drawSkyGradient(shapes, left, right, bottom, top);
            shapes.end();
        }
        drawSpriteLayer(world, batch, assets, startX, endX, startY, endY, camera);

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawMirrorCoverEyes(shapes, world);
        for (Enemy enemy : world.enemies) {
            if (!world.reviewMode && !enemy.isDead() && assets.enemy(enemy.type) != null
                    && isVisibleToPlayer(world, enemy)) {
                drawEnemyHealth(shapes, enemy);
            }
        }
        drawEnemyTelegraphs(shapes, world);
        drawAttackEffects(shapes, world);
        drawParticles(shapes, world);
        shapes.end();

        drawDamageTexts(world, batch, font, camera);
        drawPrompts(world, batch, font, camera);
    }

    private void drawSpriteLayer(GameWorld world, SpriteBatch batch, GameAssets assets,
                                 int startX, int endX, int startY, int endY, OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (assets.hasSkySpyTiles()) {
            drawSkyBackground(batch, assets, camera);
            drawSkyMap(batch, assets, world);
        } else if (assets.hasDungeonTiles()) {
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    drawTileSprite(batch, assets, world, world.map.getTile(x, y), x, y);
                }
            }
        }
        drawStairsSprite(batch, assets, world);

        for (DecorProp prop : world.props) {
            TextureRegion region = assets.prop(prop.type);
            if (region != null) {
                drawPropSprite(batch, region, prop);
            }
        }

        for (Trap trap : world.traps) {
            if (assets.trap != null) {
                Color tint = trap.isReady() ? Color.WHITE : TRAP_TRIGGERED_TINT;
                drawObjectSpriteFit(batch, assets.trap, trap.getCenter().x, trap.getCenter().y, 34f, tint);
            }
        }

        for (Chest chest : world.chests) {
            TextureRegion region = assets.chestFrame(chest);
            if (region != null) {
                Color tint = chest.bossChest ? BOSS_CHEST_TINT : Color.WHITE;
                drawObjectSprite(batch, region, chest.getCenter().x, chest.getCenter().y, 34f, 34f, tint);
            }
        }

        if (world.shop != null) {
            drawShopSprite(batch, assets, world.shop);
        }

        for (Item item : world.items) {
            TextureRegion region = assets.item(item.type);
            if (region != null) {
                batch.draw(region, item.position.x - 2f, item.position.y - 2f, item.size.x + 4f, item.size.y + 4f);
            }
        }

        drawProjectiles(batch, assets, world, true);

        drawCatSprites(batch, assets, world);

        for (Enemy enemy : world.enemies) {
            if (!isVisibleToPlayer(world, enemy)) {
                continue;
            }
            if (enemy.type == EnemyType.CAT || enemy.storyBossKind == StoryBossKind.CAT) {
                continue;
            }
            TextureRegion region = enemySprite(assets, enemy);
            if (region != null) {
                float scale = enemy.type == EnemyType.BOSS ? BOSS_PIXEL_SCALE : PIXEL_SCALE;
                if (!enemy.isDead()) {
                    drawActorShadow(batch, assets, enemy.getCenter().x, enemy.position.y - 4f,
                            scale, enemy.type == EnemyType.BOSS ? 0.62f : 0.48f);
                }
                drawActorSprite(batch, region, enemy.getCenter().x, enemy.position.y - 4f,
                        false, scale, Color.WHITE, false);
                TextureRegion whiteRegion = assets.enemyWhiteFrame(enemy.type, enemy.animationState,
                        enemy.facing, enemy.animationTime);
                drawActorSprite(batch, whiteRegion, enemy.getCenter().x, enemy.position.y - 4f,
                        false, scale, new Color(1f, 1f, 1f, enemyWhiteAlpha(world, enemy)), false);
            }
        }

        drawFinalDebatePhantoms(batch, assets, world);
        drawMirrorBossPhantoms(batch, assets, world);

        TextureRegion playerFrame = assets.playerFrame(world.player.animationState, world.player.facing,
                world.player.animationTime);
        if (playerFrame != null) {
            boolean flicker = world.player.invincibleTimer > 0f && ((int)(world.player.invincibleTimer * 20f) % 2 == 0);
            drawActorShadow(batch, assets, world.player.getCenter().x, world.player.position.y - 4f,
                    PIXEL_SCALE, 0.78f);
            drawActorSprite(batch, playerFrame, world.player.getCenter().x, world.player.position.y - 4f,
                    false, PIXEL_SCALE, Color.WHITE, flicker);
        }

        drawProjectiles(batch, assets, world, false);
        if (assets.hasSkySpyTiles()) {
            drawSkyVignette(batch, assets, camera);
            drawDistanceFog(batch, assets, world, camera);
        }
        batch.end();
    }

    private TextureRegion enemySprite(GameAssets assets, Enemy enemy) {
        return assets.enemyFrame(enemy.type, enemy.animationState, enemy.facing, enemy.animationTime);
    }

    private float enemyWhiteAlpha(GameWorld world, Enemy enemy) {
        float alpha;
        if (world.floor != 5 || enemy.type != EnemyType.BOSS) {
            alpha = enemy.whiteHint;
        } else if (world.isFinalBossDefending(enemy)) {
            alpha = 0.24f;
        } else if (world.isFinalBossDebating(enemy)) {
            int stage = world.finalDebateVisualStage();
            alpha = MathUtils.clamp(0.96f - (stage - 1) * 0.22f, 0.52f, 0.96f);
        } else {
            alpha = enemy.whiteHint;
        }
        if (enemy.shellCrackTimer > 0f) {
            float pulse = 0.30f + 0.20f * MathUtils.sin(enemy.shellCrackTimer * 38f);
            alpha *= MathUtils.clamp(pulse, 0.18f, 0.55f);
        }
        return alpha;
    }

    private void drawFinalDebatePhantoms(SpriteBatch batch, GameAssets assets, GameWorld world) {
        int stage = world.finalDebateVisualStage();
        if (stage <= 0) {
            return;
        }
        Enemy boss = null;
        for (Enemy enemy : world.enemies) {
            if (enemy.type == EnemyType.BOSS && !enemy.isDead()) {
                boss = enemy;
                break;
            }
        }
        if (boss == null || !isVisibleToPlayer(world, boss)) {
            return;
        }
        TextureRegion ghost = assets.enemyWhiteFrame(EnemyType.BOSS, boss.animationState,
                Direction.DOWN, boss.animationTime);
        if (ghost == null) {
            return;
        }
        float cx = boss.getCenter().x;
        float y = boss.position.y - 4f;
        if (stage >= 1) {
            drawActorSprite(batch, ghost, cx - 58f, y, false, PIXEL_SCALE, new Color(1f, 1f, 1f, 0.44f), false);
        }
        if (stage >= 2) {
            drawActorSprite(batch, ghost, cx + 58f, y, false, PIXEL_SCALE, new Color(1f, 0.90f, 0.86f, 0.42f), false);
        }
        if (stage >= 3) {
            drawActorSprite(batch, ghost, cx, y + 44f, false, PIXEL_SCALE, new Color(0.88f, 0.94f, 1f, 0.40f), false);
        }
    }

    private void drawMirrorBossPhantoms(SpriteBatch batch, GameAssets assets, GameWorld world) {
        for (Enemy enemy : world.enemies) {
            if (enemy.storyBossKind != StoryBossKind.MIRROR || enemy.isDead()) {
                continue;
            }
            if (!isVisibleToPlayer(world, enemy)) {
                continue;
            }
            TextureRegion left = assets.enemyWhiteFrame(EnemyType.BOSS, enemy.animationState,
                    Direction.LEFT, enemy.animationTime);
            TextureRegion right = assets.enemyWhiteFrame(EnemyType.BOSS, enemy.animationState,
                    Direction.RIGHT, enemy.animationTime);
            float y = enemy.position.y - 4f;
            drawActorSprite(batch, left, enemy.getCenter().x - 44f, y, false, PIXEL_SCALE,
                    new Color(0.88f, 0.94f, 1f, 0.42f), false);
            drawActorSprite(batch, right, enemy.getCenter().x + 44f, y, false, PIXEL_SCALE,
                    new Color(1f, 1f, 1f, 0.36f), false);
        }
    }

    private void drawSkyBackground(SpriteBatch batch, GameAssets assets, OrthographicCamera camera) {
        float left = camera.position.x - camera.viewportWidth * camera.zoom * 0.5f;
        float right = camera.position.x + camera.viewportWidth * camera.zoom * 0.5f;
        float bottom = camera.position.y - camera.viewportHeight * camera.zoom * 0.5f;
        float top = camera.position.y + camera.viewportHeight * camera.zoom * 0.5f;

        batch.setColor(1f, 1f, 1f, 0.28f);
        if (assets.skyStarsFar != null) {
            drawTiled(batch, assets.skyStarsFar, left, right, bottom, top, 128f);
        }
        batch.setColor(1f, 1f, 1f, 0.22f);
        if (assets.skyStarsNear != null) {
            drawTiled(batch, assets.skyStarsNear, left - 64f, right + 64f, bottom - 32f, top + 32f, 128f);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawSkyGradient(ShapeRenderer shapes, float left, float right, float bottom, float top) {
        float width = right - left;
        float height = top - bottom;
        int bands = 18;
        for (int i = 0; i < bands; i++) {
            float t0 = i / (float)bands;
            float t1 = (i + 1) / (float)bands;
            skyGradientColor(t0, gradientBandBottom);
            skyGradientColor(t1, gradientBandTop);
            float y = bottom + height * t0;
            shapes.rect(left, y, width, height / bands + 1f,
                    gradientBandBottom, gradientBandBottom, gradientBandTop, gradientBandTop);
        }
    }

    private void skyGradientColor(float t, Color out) {
        if (t < 0.58f) {
            lerpColor(SKY_GRADIENT_BOTTOM, SKY_GRADIENT_MID, t / 0.58f, out);
        } else {
            lerpColor(SKY_GRADIENT_MID, SKY_GRADIENT_TOP, (t - 0.58f) / 0.42f, out);
        }
    }

    private void lerpColor(Color from, Color to, float t, Color out) {
        float clamped = MathUtils.clamp(t, 0f, 1f);
        out.set(
                MathUtils.lerp(from.r, to.r, clamped),
                MathUtils.lerp(from.g, to.g, clamped),
                MathUtils.lerp(from.b, to.b, clamped),
                MathUtils.lerp(from.a, to.a, clamped));
    }

    private void drawTiled(SpriteBatch batch, TextureRegion region, float left, float right,
                           float bottom, float top, float size) {
        float startX = MathUtils.floor(left / size) * size;
        float startY = MathUtils.floor(bottom / size) * size;
        for (float x = startX; x < right + size; x += size) {
            for (float y = startY; y < top + size; y += size) {
                batch.draw(region, x, y, size, size);
            }
        }
    }

    private void drawSkyDepthLayer(SpriteBatch batch, GameAssets assets, GameWorld world,
                                   int startX, int endX, int startY, int endY) {
        TextureRegion face = assets.skyFloorAlt == null ? assets.skyFloor : assets.skyFloorAlt;
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                Tile tile = world.map.getTile(x, y);
                if (tile.type == TileType.VOID) {
                    continue;
                }
                float px = x * Constants.TILE_WIDTH;
                float py = y * Constants.TILE_HEIGHT;
                if (tile.edgeSouth) {
                    batch.setColor(SKY_SHADOW_TINT);
                    batch.draw(face, px + 4f, py - SKY_CLIFF_DEPTH - 8f,
                            Constants.TILE_WIDTH, 12f);
                    batch.setColor(SKY_CLIFF_TINT);
                    batch.draw(face, px, py - SKY_CLIFF_DEPTH,
                            Constants.TILE_WIDTH, SKY_CLIFF_DEPTH + 2f);
                }
                if (tile.edgeWest) {
                    batch.setColor(SKY_CLIFF_SIDE_TINT);
                    batch.draw(face, px - 5f, py - SKY_CLIFF_DEPTH * 0.55f,
                            8f, Constants.TILE_HEIGHT + SKY_CLIFF_DEPTH * 0.35f);
                }
                if (tile.edgeEast) {
                    batch.setColor(SKY_CLIFF_SIDE_TINT);
                    batch.draw(face, px + Constants.TILE_WIDTH - 3f, py - SKY_CLIFF_DEPTH * 0.55f,
                            8f, Constants.TILE_HEIGHT + SKY_CLIFF_DEPTH * 0.35f);
                }
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawSkyMap(SpriteBatch batch, GameAssets assets, GameWorld world) {
        GameAssets.SkyMapTextures skyMap = assets.skyMap(world.map);
        if (skyMap == null) {
            return;
        }
        batch.setColor(BLIND_JUMP_MAP_MULTIPLY);
        batch.draw(skyMap.base, 0f, 0f, skyMap.width, skyMap.height);
        batch.draw(skyMap.edge, 0f, 0f, skyMap.width, skyMap.height);
        batch.setColor(Color.WHITE);
    }

    private void drawSkyVignette(SpriteBatch batch, GameAssets assets, OrthographicCamera camera) {
        if (assets.skyWhiteFog == null) {
            return;
        }
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;
        float left = camera.position.x - width * 0.5f;
        float bottom = camera.position.y - height * 0.5f;
        batch.setColor(1f, 1f, 1f, 0.42f);
        batch.draw(assets.skyWhiteFog, left, bottom, width, height);
        batch.setColor(Color.WHITE);
    }

    private void drawDistanceFog(SpriteBatch batch, GameAssets assets, GameWorld world, OrthographicCamera camera) {
        if (assets.skyWhiteFog == null || world.player == null) {
            return;
        }
        float halfWidth = camera.viewportWidth * camera.zoom * 0.5f;
        float halfHeight = camera.viewportHeight * camera.zoom * 0.5f;
        float left = camera.position.x - halfWidth;
        float right = camera.position.x + halfWidth;
        float bottom = camera.position.y - halfHeight;
        float top = camera.position.y + halfHeight;
        float centerX = world.player.getCenter().x;
        float centerY = world.player.getCenter().y;
        float farX = Math.max(Math.abs(centerX - left), Math.abs(right - centerX));
        float farY = Math.max(Math.abs(centerY - bottom), Math.abs(top - centerY));
        float coverSize = (float)Math.sqrt(farX * farX + farY * farY) * 2.08f;

        drawFogLayer(batch, assets.skyWhiteFog, centerX, centerY, coverSize, 0.96f);
        batch.setColor(Color.WHITE);
    }

    private void drawFogLayer(SpriteBatch batch, TextureRegion region, float centerX, float centerY,
                              float size, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(region, centerX - size * 0.5f, centerY - size * 0.5f, size, size);
    }

    private void drawActorSprite(SpriteBatch batch, TextureRegion region, float centerX, float bottomY,
                                 boolean flipX, float scale, Color tint, boolean flicker) {
        if (flicker) {
            batch.setColor(0.60f, 0.82f, 1f, 0.72f);
        } else {
            batch.setColor(tint);
        }
        float width = region.getRegionWidth() * scale;
        float height = region.getRegionHeight() * scale;
        float x = centerX - width * 0.5f;
        if (flipX) {
            batch.draw(region, x + width, bottomY, -width, height);
        } else {
            batch.draw(region, x, bottomY, width, height);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawActorShadow(SpriteBatch batch, GameAssets assets, float centerX, float bottomY,
                                 float scale, float alpha) {
        if (assets.playerShadow == null) {
            return;
        }
        float actorWidth = 32f * scale;
        float actorHeight = 32f * scale;
        float shadowWidth = assets.playerShadow.getRegionWidth() * scale;
        float shadowHeight = assets.playerShadow.getRegionHeight() * scale;
        float actorLeft = centerX - actorWidth * 0.5f;
        float actorTop = bottomY + actorHeight;
        float shadowX = actorLeft + 7f * scale;
        float shadowY = actorTop - (24f + assets.playerShadow.getRegionHeight()) * scale;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(assets.playerShadow, shadowX, shadowY, shadowWidth, shadowHeight);
        batch.setColor(Color.WHITE);
    }

    private void drawObjectSprite(SpriteBatch batch, TextureRegion region, float centerX, float centerY,
                                  float width, float height, Color tint) {
        batch.setColor(tint);
        batch.draw(region, centerX - width * 0.5f, centerY - height * 0.5f, width, height);
        batch.setColor(Color.WHITE);
    }

    private void drawObjectSpriteFit(SpriteBatch batch, TextureRegion region, float centerX, float centerY,
                                     float maxSize, Color tint) {
        float rawMax = Math.max(region.getRegionWidth(), region.getRegionHeight());
        float scale = maxSize / rawMax;
        drawObjectSprite(batch, region, centerX, centerY,
                region.getRegionWidth() * scale, region.getRegionHeight() * scale, tint);
    }

    private void drawPropSprite(SpriteBatch batch, TextureRegion region, DecorProp prop) {
        drawObjectSpriteFit(batch, region, prop.getCenter().x, prop.getCenter().y,
                propMaxSize(prop.type), Color.WHITE);
    }

    private float propMaxSize(PropType type) {
        switch (type) {
            case WHITE_TREE:
            case BARREL:
                return 54f;
            case BED_OUTLINE:
                return 46f;
            case TORCH:
                return 22f;
            case PHOTO_FRAME:
                return 26f;
            case BROKEN_BOWL:
            case MIRROR_SHARD:
            case RUBBLE:
                return 24f;
            default:
                return 34f;
        }
    }

    private void drawProjectileSprite(SpriteBatch batch, TextureRegion region, Projectile projectile) {
        if (projectile.hostile) {
            batch.setColor(1f, 0.42f, 0.22f, 1f);
        }
        float width = region.getRegionWidth() * PIXEL_SCALE;
        float height = region.getRegionHeight() * PIXEL_SCALE;
        batch.draw(region, projectile.getCenter().x - width * 0.5f, projectile.getCenter().y - height * 0.5f,
                width * 0.5f, height * 0.5f, width, height, 1f, 1f, projectile.rotation);
        batch.setColor(Color.WHITE);
    }

    private void drawProjectiles(SpriteBatch batch, GameAssets assets, GameWorld world, boolean behindActors) {
        TextureRegion region = assets.skyProjectile != null ? assets.skyProjectile : assets.arrow;
        if (region == null) {
            return;
        }
        for (Projectile projectile : world.projectiles) {
            if (drawProjectileBehindActors(projectile) != behindActors) {
                continue;
            }
            drawProjectileSprite(batch, region, projectile);
        }
    }

    private boolean drawProjectileBehindActors(Projectile projectile) {
        boolean facesScreen = projectile.velocity.y < 0f
                && Math.abs(projectile.velocity.y) >= Math.abs(projectile.velocity.x);
        return !projectile.hostile && !facesScreen;
    }

    private void drawCatSprites(SpriteBatch batch, GameAssets assets, GameWorld world) {
        if (world.reviewMode) {
            drawReviewCatSprites(batch, assets, world);
        } else if (world.storyCatVisible) {
            if (isVisibleToPlayer(world, world.storyCatPosition.x, world.storyCatPosition.y)) {
                boolean resting = world.floor >= 5;
                boolean flip = world.player.getCenter().x > world.storyCatPosition.x;
                float whiteAlpha = storyCatWhiteAlpha(world);
                drawCatSprite(batch, assets, world.storyCatPosition.x, world.storyCatPosition.y,
                        !resting, resting ? 0.15f : world.visualTime, flip, PIXEL_SCALE, whiteAlpha);
            }
        }
        for (Enemy enemy : world.enemies) {
            if (enemy.type != EnemyType.CAT && enemy.storyBossKind != StoryBossKind.CAT) {
                continue;
            }
            if (!isVisibleToPlayer(world, enemy)) {
                continue;
            }
            boolean running = !enemy.isDead() && enemy.animationState == AnimationState.WALK;
            boolean flip = enemy.facing == Direction.RIGHT;
            float scale = enemy.storyBossKind == StoryBossKind.CAT ? PIXEL_SCALE * 1.18f : PIXEL_SCALE;
            float whiteAlpha = enemy.storyBossKind == StoryBossKind.CAT
                    ? catBossWhiteAlpha(enemy)
                    : 0.84f;
            drawCatSprite(batch, assets, enemy.getCenter().x, enemy.getCenter().y,
                    !running, enemy.animationTime, flip, scale, whiteAlpha);
        }
    }

    private void drawReviewCatSprites(SpriteBatch batch, GameAssets assets, GameWorld world) {
        int index = 0;
        for (Enemy enemy : world.enemies) {
            if (enemy.isDead() || !isVisibleToPlayer(world, enemy)) {
                continue;
            }
            float side = (index++ % 2 == 0) ? -1f : 1f;
            float x = enemy.getCenter().x + side * 30f;
            float y = enemy.getCenter().y - 5f;
            boolean flip = side < 0f;
            drawCatSprite(batch, assets, x, y, true, world.visualTime + index * 0.17f,
                    flip, PIXEL_SCALE * 0.86f, 0.24f);
        }
    }

    private float storyCatWhiteAlpha(GameWorld world) {
        if (world.floor >= 5) {
            return 0.18f;
        }
        if (world.floor >= 4) {
            return 0.34f;
        }
        return 0.72f;
    }

    private float catBossWhiteAlpha(Enemy enemy) {
        float hpRatio = enemy.maxHp <= 0 ? 0f : enemy.hp / (float)enemy.maxHp;
        return MathUtils.clamp(0.22f + hpRatio * 0.56f, 0.22f, 0.78f);
    }

    private void drawCatSprite(SpriteBatch batch, GameAssets assets, float originX, float originY,
                               boolean idle, float animationTime, boolean flipX, float scale,
                               float whiteAlpha) {
        boolean running = !idle;
        TextureRegion region = assets.catFrame(running, animationTime);
        TextureRegion whiteRegion = assets.catWhiteFrame(running, animationTime);
        float originPixelX = running ? 18f : 16f;
        float originPixelYFromTop = running ? 20f : 16f;
        if (assets.playerShadow != null) {
            float shadowWidth = assets.playerShadow.getRegionWidth() * scale;
            float shadowHeight = assets.playerShadow.getRegionHeight() * scale;
            batch.setColor(1f, 1f, 1f, 0.38f);
            batch.draw(assets.playerShadow, originX - shadowWidth * 0.5f,
                    originY - shadowHeight * 0.30f, shadowWidth, shadowHeight);
        }
        drawCatRegion(batch, region, originX, originY, originPixelX, originPixelYFromTop,
                scale, flipX, Color.WHITE);
        if (whiteRegion != null && whiteAlpha > 0f) {
            drawCatRegion(batch, whiteRegion, originX, originY, originPixelX, originPixelYFromTop,
                    scale, flipX, new Color(1f, 1f, 1f, whiteAlpha));
        }
        batch.setColor(Color.WHITE);
    }

    private void drawCatRegion(SpriteBatch batch, TextureRegion region, float originX, float originY,
                               float originPixelX, float originPixelYFromTop, float scale,
                               boolean flipX, Color tint) {
        if (region == null) {
            return;
        }
        float width = region.getRegionWidth() * scale;
        float height = region.getRegionHeight() * scale;
        float x = originX - originPixelX * scale;
        float y = originY - (region.getRegionHeight() - originPixelYFromTop) * scale;
        batch.setColor(tint);
        if (flipX) {
            batch.draw(region, x + width, y, -width, height);
        } else {
            batch.draw(region, x, y, width, height);
        }
    }

    private void drawTileSprite(SpriteBatch batch, GameAssets assets, GameWorld world, Tile tile, int x, int y) {
        if (assets.hasSkySpyTiles()) {
            if (tile.type == TileType.VOID) {
                return;
            }
            TextureRegion region = assets.floor(x, y);
            batch.setColor(tile.type == TileType.STAIRS_DOWN ? SKY_EDGE_TINT : SKY_FLOOR_TINT);
            batch.draw(region, x * Constants.TILE_WIDTH, y * Constants.TILE_HEIGHT,
                    Constants.TILE_WIDTH, Constants.TILE_HEIGHT);
            if (assets.skyEdge != null && tile.grass) {
                batch.setColor(SKY_EDGE_TINT);
                batch.draw(assets.skyEdge, x * Constants.TILE_WIDTH, y * Constants.TILE_HEIGHT,
                        Constants.TILE_WIDTH, Constants.TILE_HEIGHT);
            }
            drawSkyTileRims(batch, assets, tile, x, y);
            batch.setColor(Color.WHITE);
            return;
        }
        if (tile.type == TileType.VOID) {
            return;
        }
        TextureRegion region = assets.floor(x, y);
        Color floorTint = world.biome == null ? FLOOR_TINT : world.biome.floorTint;
        Color tint = floorTint;
        if (tile.type == TileType.STAIRS_DOWN) {
            region = assets.floor(x, y);
            tint = floorTint;
        }
        batch.setColor(tint);
        batch.draw(region, x * Constants.TILE_WIDTH, y * Constants.TILE_HEIGHT, Constants.TILE_WIDTH, Constants.TILE_HEIGHT);
        batch.setColor(Color.WHITE);
    }

    private void drawSkyTileRims(SpriteBatch batch, GameAssets assets, Tile tile, int x, int y) {
        TextureRegion rim = assets.skyEdge == null ? assets.skyFloor : assets.skyEdge;
        float px = x * Constants.TILE_WIDTH;
        float py = y * Constants.TILE_HEIGHT;
        if (tile.edgeNorth) {
            batch.setColor(SKY_RIM_TINT);
            batch.draw(rim, px, py + Constants.TILE_HEIGHT - 5f, Constants.TILE_WIDTH, 5f);
        }
        if (tile.edgeSouth) {
            batch.setColor(SKY_EDGE_TINT);
            batch.draw(rim, px, py - 1f, Constants.TILE_WIDTH, 6f);
        }
        if (tile.edgeWest) {
            batch.setColor(SKY_CLIFF_SIDE_TINT);
            batch.draw(rim, px - 1f, py, 5f, Constants.TILE_HEIGHT);
        }
        if (tile.edgeEast) {
            batch.setColor(SKY_CLIFF_SIDE_TINT);
            batch.draw(rim, px + Constants.TILE_WIDTH - 4f, py, 5f, Constants.TILE_HEIGHT);
        }
    }

    private void drawStairsSprite(SpriteBatch batch, GameAssets assets, GameWorld world) {
        if (assets.stairs == null) {
            return;
        }
        float height = Constants.TILE_SIZE * 1.45f;
        float width = height * assets.stairs.getRegionWidth() / (float)assets.stairs.getRegionHeight();
        float x = world.map.stairsPosition.x - width * 0.5f;
        float y = world.map.stairsPosition.y - height * 0.44f;
        batch.setColor(STAIR_TINT);
        batch.draw(assets.stairs, x, y, width, height);
        batch.setColor(Color.WHITE);
    }

    private void drawEnemyHealth(ShapeRenderer shapes, Enemy enemy) {
        float barWidth = enemy.type == EnemyType.BOSS ? 44f : 28f;
        float width = barWidth * enemy.hp / (float)enemy.maxHp;
        shapes.setColor(0.25f, 0.02f, 0.03f, 1f);
        shapes.rect(enemy.getCenter().x - barWidth * 0.5f, enemy.position.y + enemy.size.y + 4f, barWidth, 4f);
        shapes.setColor(0.85f, 0.12f, 0.10f, 1f);
        shapes.rect(enemy.getCenter().x - barWidth * 0.5f, enemy.position.y + enemy.size.y + 4f, width, 4f);
    }

    private void drawMirrorCoverEyes(ShapeRenderer shapes, GameWorld world) {
        float alpha = world.mirrorStillVisualAlpha();
        if (alpha <= 0f) {
            return;
        }
        for (Enemy enemy : world.enemies) {
            if (enemy.isDead() || (enemy.type != EnemyType.MIRROR && enemy.storyBossKind != StoryBossKind.MIRROR)) {
                continue;
            }
            if (!isVisibleToPlayer(world, enemy)) {
                continue;
            }
            float scale = enemy.type == EnemyType.BOSS ? 1.35f : 1f;
            float cx = enemy.getCenter().x;
            float cy = enemy.getCenter().y + 10f * scale;
            shapes.setColor(1f, 1f, 1f, 0.50f * alpha);
            shapes.rectLine(cx - 13f * scale, cy + 3f * scale,
                    cx + 13f * scale, cy + 3f * scale, 4f * scale);
            shapes.rectLine(cx - 10f * scale, cy - 3f * scale,
                    cx + 10f * scale, cy - 3f * scale, 3f * scale);
        }
    }

    private void drawEnemyTelegraphs(ShapeRenderer shapes, GameWorld world) {
        for (Enemy enemy : world.enemies) {
            if (enemy.isDead() || enemy.actionState == EnemyActionState.NONE
                    || enemy.actionState == EnemyActionState.SLIME_CHARGE
                    || enemy.actionDirection.isZero(0.01f)
                    || !isVisibleToPlayer(world, enemy)) {
                continue;
            }
            float length = 60f;
            float width = 4f;
            switch (enemy.actionState) {
                case SLIME_WINDUP:
                    shapes.setColor(1f, 0.35f, 0.08f, 0.72f);
                    length = 92f;
                    width = 5f;
                    break;
                case RANGED_WINDUP:
                    shapes.setColor(0.35f, 0.82f, 1f, 0.72f);
                    length = enemy.type == EnemyType.BOSS ? 150f : 120f;
                    width = 3f;
                    break;
                case HEAVY_WINDUP:
                    shapes.setColor(1f, 0.12f, 0.08f, 0.78f);
                    length = enemy.type == EnemyType.BOSS ? 78f : 56f;
                    width = enemy.type == EnemyType.BOSS ? 10f : 7f;
                    break;
                default:
                    break;
            }
            float startX = enemy.getCenter().x;
            float startY = enemy.getCenter().y;
            shapes.rectLine(startX, startY, startX + enemy.actionDirection.x * length,
                    startY + enemy.actionDirection.y * length, width);
        }
    }

    private void drawAttackEffects(ShapeRenderer shapes, GameWorld world) {
        for (AttackEffect effect : world.attackEffects) {
            shapes.setColor(1f, 0.88f, 0.42f, effect.alpha());
            shapes.rectLine(effect.start, effect.end, 3f);
        }
    }

    private boolean isVisibleToPlayer(GameWorld world, Enemy enemy) {
        return isVisibleToPlayer(world, enemy.getCenter().x, enemy.getCenter().y);
    }

    private boolean isVisibleToPlayer(GameWorld world, float worldX, float worldY) {
        int tileX = world.map.worldToTileX(worldX);
        int tileY = world.map.worldToTileY(worldY);
        return world.map.isInBounds(tileX, tileY) && world.map.getTile(tileX, tileY).visible;
    }

    private void drawParticles(ShapeRenderer shapes, GameWorld world) {
        for (Particle particle : world.particles) {
            float alpha = particle.alpha();
            shapes.setColor(particle.color.r, particle.color.g, particle.color.b, particle.color.a * alpha);
            shapes.circle(particle.position.x, particle.position.y, particle.size * (0.65f + alpha * 0.35f), 8);
        }
    }

    private void drawDamageTexts(GameWorld world, SpriteBatch batch, BitmapFont font, OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.getData().setScale(0.85f);
        for (DamageText text : world.damageTexts) {
            font.setColor(text.color);
            font.draw(batch, text.text, text.position.x - 12f, text.position.y);
        }
        font.getData().setScale(1.0f);
        batch.end();
    }

    private void drawPrompts(GameWorld world, SpriteBatch batch, BitmapFont font, OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.getData().setScale(0.68f);

        float px = world.player.getCenter().x;
        float py = world.player.getCenter().y;
        String prompt = null;
        Color promptColor = null;
        int promptPriority = -1;
        float promptDistance = Float.MAX_VALUE;

        for (Item item : world.items) {
            float ix = item.getCenter().x;
            float iy = item.getCenter().y;
            float dist = world.player.getCenter().dst(ix, iy);
            if (dist > 70f || dist >= promptDistance && promptPriority >= 10) {
                continue;
            }
            String label = itemLabel(item);
            if (label != null) {
                prompt = label;
                promptColor = new Color(0.72f, 0.56f, 0.18f, 0.95f);
                promptPriority = 10;
                promptDistance = dist;
            }
        }

        for (Chest chest : world.chests) {
            if (chest.opened) {
                continue;
            }
            float cx = chest.getCenter().x;
            float cy = chest.getCenter().y;
            float dist = world.player.getCenter().dst(cx, cy);
            if (dist > 70f) {
                continue;
            }
            if (chest.bossChest && world.hasLiveBoss()) {
                prompt = "先清门";
                promptColor = new Color(0.78f, 0.18f, 0.14f, 0.96f);
            } else {
                prompt = "E 使用";
                promptColor = new Color(0.50f, 1.00f, 0.82f, 1f);
            }
            promptPriority = 40;
            promptDistance = dist;
        }

        if (world.reviewMode) {
            for (Enemy enemy : world.enemies) {
                if (enemy.isDead()) {
                    continue;
                }
                float dist = world.player.getCenter().dst(enemy.getCenter());
                if (dist < 74f) {
                    prompt = "E 安抚";
                    promptColor = new Color(0.24f, 0.48f, 0.76f, 0.96f);
                    promptPriority = 50;
                    promptDistance = dist;
                    break;
                }
            }
        }

        if (world.shop != null) {
            float shopDist = world.player.getCenter().dst(world.shop.getCenter());
            if (shopDist < 70f) {
                prompt = "E 回响";
                promptColor = new Color(0.42f, 0.96f, 1.00f, 1f);
                promptPriority = 45;
                promptDistance = shopDist;
            }
        }

        if (!world.reviewMode && world.storyCatVisible && world.floor >= 4) {
            float catDist = world.player.getCenter().dst(world.storyCatPosition);
            if (catDist < 80f) {
                prompt = "E 等等";
                promptColor = new Color(0.24f, 0.48f, 0.76f, 0.96f);
                promptPriority = 60;
                promptDistance = catDist;
            }
        }

        for (Enemy enemy : world.enemies) {
            if (!world.isFinalBossDebating(enemy)) {
                continue;
            }
            float dist = world.player.getCenter().dst(enemy.getCenter());
            if (dist < 104f) {
                prompt = world.isFinalDebateWaiting() ? "等等" : "E 记忆";
                promptColor = new Color(0.24f, 0.48f, 0.76f, 0.96f);
                promptPriority = 70;
                promptDistance = dist;
            }
            break;
        }

        float stairsDist = world.player.getCenter().dst(world.map.stairsPosition);
        if (stairsDist < 70f) {
            float sx = world.map.stairsPosition.x;
            float sy = world.map.stairsPosition.y;
            if (world.reviewMode) {
                prompt = world.reviewFloorComplete() ? "E 裂隙" : "安抚";
                promptColor = world.reviewFloorComplete()
                        ? new Color(0.50f, 1.00f, 0.82f, 1f)
                        : new Color(0.42f, 0.48f, 0.64f, 0.82f);
            } else if (world.hasLiveBoss()) {
                prompt = "先清门";
                promptColor = new Color(0.78f, 0.18f, 0.14f, 0.96f);
            } else if (world.player.keys <= 0) {
                prompt = "需要碎片";
                promptColor = new Color(0.72f, 0.48f, 0.12f, 0.96f);
            } else {
                prompt = "E 使用";
                promptColor = new Color(0.50f, 1.00f, 0.82f, 1f);
            }
            promptPriority = 80;
            promptDistance = stairsDist;
        }

        if (prompt != null) {
            drawPromptText(font, batch, prompt, px - 80f, py + 78f, 160f, promptColor);
        }

        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);
        batch.end();
    }

    private void drawPromptText(BitmapFont font, SpriteBatch batch, String prompt,
                                float x, float y, float width, Color color) {
        font.setColor(PROMPT_OUTLINE);
        font.draw(batch, prompt, x - 2f, y, width, Align.center, false);
        font.draw(batch, prompt, x + 2f, y, width, Align.center, false);
        font.draw(batch, prompt, x, y - 2f, width, Align.center, false);
        font.draw(batch, prompt, x, y + 2f, width, Align.center, false);
        font.draw(batch, prompt, x - 1f, y - 1f, width, Align.center, false);
        font.draw(batch, prompt, x + 1f, y - 1f, width, Align.center, false);
        font.draw(batch, prompt, x - 1f, y + 1f, width, Align.center, false);
        font.draw(batch, prompt, x + 1f, y + 1f, width, Align.center, false);
        font.setColor(color);
        font.draw(batch, prompt, x, y, width, Align.center, false);
    }

    private void drawShopSprite(SpriteBatch batch, GameAssets assets, Shop shop) {
        if (assets.crate == null) {
            return;
        }
        batch.setColor(0.28f, 0.82f, 0.92f, 1f);
        batch.draw(assets.crate, shop.getCenter().x - 17f, shop.getCenter().y - 17f, 34f, 34f);
        batch.setColor(Color.WHITE);
    }

    private String propLabel(DecorProp prop) {
        switch (prop.type) {
            case BARREL: return "岩块";
            case CRATE: return "终端";
            case TORCH: return "灯";
            case RUBBLE: return "碎片";
            default: return null;
        }
    }

    private String itemLabel(Item item) {
        switch (item.type) {
            case COIN: return "回响";
            case POTION: return "修补";
            case SWORD_UPGRADE: return "攻击+";
            case ARMOR_UPGRADE: return "防御+";
            case KEY: return "碎片";
            case RELIC: return item.relicType != null ? item.relicType.label : "残留";
            case WEAPON: return item.weaponType != null ? item.weaponType.label : "射击";
            default: return null;
        }
    }
}
