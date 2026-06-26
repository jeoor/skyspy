package com.kayro.dungeon.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.entity.DecorProp;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.Item;
import com.kayro.dungeon.entity.Trap;
import com.kayro.dungeon.world.GameWorld;

public class DebugRenderer {
    public void render(GameWorld world, ShapeRenderer shapes, OrthographicCamera camera) {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.CYAN);
        Rectangle playerBounds = world.player.getBounds();
        shapes.rect(playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);
        shapes.setColor(Color.RED);
        for (Enemy enemy : world.enemies) {
            Rectangle bounds = enemy.getBounds();
            shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        shapes.setColor(Color.GOLD);
        for (Chest chest : world.chests) {
            shapes.rect(chest.position.x, chest.position.y, chest.size.x, chest.size.y);
        }
        shapes.setColor(Color.GRAY);
        for (DecorProp prop : world.props) {
            shapes.rect(prop.position.x, prop.position.y, prop.size.x, prop.size.y);
        }
        shapes.setColor(Color.ORANGE);
        for (Trap trap : world.traps) {
            shapes.rect(trap.position.x, trap.position.y, trap.size.x, trap.size.y);
        }
        shapes.setColor(Color.WHITE);
        for (Item item : world.items) {
            shapes.rect(item.position.x, item.position.y, item.size.x, item.size.y);
        }
        shapes.end();
    }
}
