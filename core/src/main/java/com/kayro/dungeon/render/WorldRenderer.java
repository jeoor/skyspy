package com.kayro.dungeon.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.kayro.dungeon.asset.GameAssets;
import com.kayro.dungeon.entity.AttackEffect;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.entity.DamageText;
import com.kayro.dungeon.entity.DecorProp;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.EnemyActionState;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.entity.Item;
import com.kayro.dungeon.entity.Projectile;
import com.kayro.dungeon.entity.Trap;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.util.Direction;
import com.kayro.dungeon.world.GameWorld;
import com.kayro.dungeon.world.Tile;
import com.kayro.dungeon.world.TileType;

public class WorldRenderer {
    private static final Color UNEXPLORED = new Color(0f, 0f, 0f, 1f);
    private static final Color FOG = new Color(0f, 0f, 0f, 0.55f);
    private static final Color FLOOR_TINT = new Color(0.56f, 0.56f, 0.56f, 1f);
    private static final Color WALL_TINT = new Color(1f, 1f, 1f, 1f);
    private static final Color STAIR_TINT = new Color(0.82f, 0.88f, 1f, 1f);
    private static final Color TRAP_TRIGGERED_TINT = new Color(1f, 0.42f, 0.36f, 1f);
    private static final Color BOSS_CHEST_TINT = new Color(1f, 0.78f, 0.30f, 1f);
    private static final float PIXEL_SCALE = 2f;

    public void render(GameWorld world, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font,
                       GameAssets assets, OrthographicCamera camera) {
        float left = camera.position.x - camera.viewportWidth * camera.zoom * 0.5f;
        float right = camera.position.x + camera.viewportWidth * camera.zoom * 0.5f;
        float bottom = camera.position.y - camera.viewportHeight * camera.zoom * 0.5f;
        float top = camera.position.y + camera.viewportHeight * camera.zoom * 0.5f;

        int startX = MathUtils.clamp(MathUtils.floor(left / Constants.TILE_SIZE) - 1, 0, Constants.MAP_WIDTH - 1);
        int endX = MathUtils.clamp(MathUtils.ceil(right / Constants.TILE_SIZE) + 1, 0, Constants.MAP_WIDTH - 1);
        int startY = MathUtils.clamp(MathUtils.floor(bottom / Constants.TILE_SIZE) - 1, 0, Constants.MAP_HEIGHT - 1);
        int endY = MathUtils.clamp(MathUtils.ceil(top / Constants.TILE_SIZE) + 1, 0, Constants.MAP_HEIGHT - 1);

        drawSpriteLayer(world, batch, assets, startX, endX, startY, endY, camera);

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawTraps(shapes, world);
        for (Enemy enemy : world.enemies) {
            if (!enemy.isDead() && assets.enemy(enemy.type) != null) {
                drawEnemyHealth(shapes, enemy);
            }
        }
        drawEnemyTelegraphs(shapes, world);
        drawAttackEffects(shapes, world);
        drawFog(shapes, world, startX, endX, startY, endY);
        shapes.end();

        drawDamageTexts(world, batch, font, camera);
    }

    private void drawSpriteLayer(GameWorld world, SpriteBatch batch, GameAssets assets,
                                 int startX, int endX, int startY, int endY, OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (assets.hasDungeonTiles()) {
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    drawTileSprite(batch, assets, world, world.map.getTile(x, y), x, y);
                }
            }
        }

        for (DecorProp prop : world.props) {
            TextureRegion region = assets.prop(prop.type);
            if (region != null) {
                drawObjectSprite(batch, region, prop.getCenter().x, prop.getCenter().y, 30f, 30f, Color.WHITE);
            }
        }

        for (Trap trap : world.traps) {
            if (assets.trap != null) {
                Color tint = trap.isReady() ? Color.WHITE : TRAP_TRIGGERED_TINT;
                drawObjectSprite(batch, assets.trap, trap.getCenter().x, trap.getCenter().y, 28f, 28f, tint);
            }
        }

        for (Chest chest : world.chests) {
            TextureRegion region = assets.chestFrame(chest);
            if (region != null) {
                Color tint = chest.bossChest ? BOSS_CHEST_TINT : Color.WHITE;
                drawObjectSprite(batch, region, chest.getCenter().x, chest.getCenter().y, 34f, 34f, tint);
            }
        }

        for (Item item : world.items) {
            TextureRegion region = assets.item(item.type);
            if (region != null) {
                batch.draw(region, item.position.x - 2f, item.position.y - 2f, item.size.x + 4f, item.size.y + 4f);
            }
        }

        for (Projectile projectile : world.projectiles) {
            if (assets.arrow != null) {
                drawProjectileSprite(batch, assets.arrow, projectile);
            }
        }

        for (Enemy enemy : world.enemies) {
            TextureRegion region = enemySprite(assets, enemy);
            if (region != null) {
                float scale = enemy.type == EnemyType.BOSS ? 2.8f : PIXEL_SCALE;
                drawActorSprite(batch, region, enemy.getCenter().x, enemy.position.y - 4f,
                        enemy.facing == Direction.LEFT, scale, assets.enemyTint(enemy.type), false);
            }
        }

        TextureRegion playerFrame = assets.playerFrame(world.player.animationState, world.player.facing,
                world.player.animationTime);
        if (playerFrame != null) {
            boolean flicker = world.player.invincibleTimer > 0f && ((int)(world.player.invincibleTimer * 20f) % 2 == 0);
            drawActorSprite(batch, playerFrame, world.player.getCenter().x, world.player.position.y - 4f,
                    world.player.facing == Direction.LEFT, PIXEL_SCALE, Color.WHITE, flicker);
        }
        batch.end();
    }

    private TextureRegion enemySprite(GameAssets assets, Enemy enemy) {
        return assets.enemyFrame(enemy.type, enemy.animationState, enemy.facing, enemy.animationTime);
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

    private void drawObjectSprite(SpriteBatch batch, TextureRegion region, float centerX, float centerY,
                                  float width, float height, Color tint) {
        batch.setColor(tint);
        batch.draw(region, centerX - width * 0.5f, centerY - height * 0.5f, width, height);
        batch.setColor(Color.WHITE);
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

    private void drawTileSprite(SpriteBatch batch, GameAssets assets, GameWorld world, Tile tile, int x, int y) {
        TextureRegion region = tile.type == TileType.WALL
                ? assets.wall(x, y)
                : assets.floor(x, y);
        Color floorTint = world.biome == null ? FLOOR_TINT : world.biome.floorTint;
        Color wallTint = world.biome == null ? WALL_TINT : world.biome.wallTint;
        Color tint = tile.type == TileType.WALL ? wallTint : floorTint;
        if (tile.type == TileType.STAIRS_DOWN && assets.stairs != null) {
            region = assets.floor(x, y);
            tint = floorTint;
        }
        batch.setColor(tint);
        batch.draw(region, x * Constants.TILE_SIZE, y * Constants.TILE_SIZE, Constants.TILE_SIZE, Constants.TILE_SIZE);
        if (tile.type == TileType.STAIRS_DOWN && assets.stairs != null) {
            batch.setColor(STAIR_TINT);
            batch.draw(assets.stairs, x * Constants.TILE_SIZE + 2f, y * Constants.TILE_SIZE + 2f,
                    Constants.TILE_SIZE - 4f, Constants.TILE_SIZE - 4f);
        }
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

    private void drawTraps(ShapeRenderer shapes, GameWorld world) {
        for (Trap trap : world.traps) {
            float left = trap.getCenter().x - 13f;
            float bottom = trap.getCenter().y - 10f;
            if (trap.isReady()) {
                shapes.setColor(0.10f, 0.10f, 0.12f, 1f);
            } else {
                shapes.setColor(0.34f, 0.10f, 0.08f, 1f);
            }
            shapes.rect(left, bottom + 13f, 26f, 5f);
            Color accent = world.biome == null ? Color.LIGHT_GRAY : world.biome.accent;
            if (trap.isReady()) {
                shapes.setColor(accent.r, accent.g, accent.b, 1f);
            } else {
                shapes.setColor(0.95f, 0.36f, 0.28f, 1f);
            }
            for (int i = 0; i < 5; i++) {
                float x = left + 3f + i * 5f;
                shapes.triangle(x, bottom + 13f, x + 2.5f, bottom + 2f, x + 5f, bottom + 13f);
            }
        }
    }

    private void drawEnemyTelegraphs(ShapeRenderer shapes, GameWorld world) {
        for (Enemy enemy : world.enemies) {
            if (enemy.isDead() || enemy.actionState == EnemyActionState.NONE
                    || enemy.actionState == EnemyActionState.SLIME_CHARGE
                    || enemy.actionDirection.isZero(0.01f)) {
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

    private void drawFog(ShapeRenderer shapes, GameWorld world, int startX, int endX, int startY, int endY) {
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                Tile tile = world.map.getTile(x, y);
                if (!tile.explored) {
                    shapes.setColor(UNEXPLORED);
                } else if (!tile.visible) {
                    shapes.setColor(FOG);
                } else {
                    continue;
                }
                shapes.rect(x * Constants.TILE_SIZE, y * Constants.TILE_SIZE, Constants.TILE_SIZE, Constants.TILE_SIZE);
            }
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
}
