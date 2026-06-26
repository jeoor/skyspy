package com.kayro.dungeon.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.kayro.dungeon.DungeonForgeGame;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.world.GameWorld;

public class PauseMenuOverlay extends BaseMenuScreen {
    public PauseMenuOverlay(DungeonForgeGame game) {
        super(game);
    }

    public void render(GameWorld world, boolean resumeHovered, boolean menuHovered,
                       Rectangle resumeButton, Rectangle menuButton) {
        applyViewport();

        game.shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        drawButton(resumeButton, resumeHovered);
        drawButton(menuButton, menuHovered);
        game.shapes.end();

        game.batch.begin();
        game.font.getData().setScale(1.55f);
        game.font.setColor(TEXT);
        game.font.draw(game.batch, "暂停", 0f, Constants.HUD_HEIGHT - 58f, Constants.HUD_WIDTH,
                Align.center, false);

        game.font.getData().setScale(0.90f);
        game.font.setColor(DIM);
        game.font.draw(game.batch, "层" + world.floor + "   击" + world.kills + "   生命 "
                        + world.player.hp + "/" + world.player.maxHp,
                0f, Constants.HUD_HEIGHT - 126f, Constants.HUD_WIDTH, Align.center, false);

        game.font.getData().setScale(1.0f);
        drawButtonLabel(resumeButton, "继续", resumeHovered);
        drawButtonLabel(menuButton, "菜单", menuHovered);
        game.font.setColor(DIM);
        game.font.draw(game.batch, "ESC继续     回车菜单",
                0f, 38f, Constants.HUD_WIDTH, Align.center, false);
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(1.0f);
        game.batch.end();
    }
}
