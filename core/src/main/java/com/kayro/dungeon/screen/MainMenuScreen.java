package com.kayro.dungeon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.kayro.dungeon.DungeonForgeGame;
import com.kayro.dungeon.util.Constants;

public class MainMenuScreen extends BaseMenuScreen {
    private final Rectangle reviewButton = new Rectangle(500f, 170f, 280f, 42f);
    private final Rectangle startButton = new Rectangle(500f, 116f, 280f, 42f);
    private final Rectangle quitButton = new Rectangle(500f, 62f, 280f, 42f);

    public MainMenuScreen(DungeonForgeGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        updateMouse();
        boolean click = clicked();
        boolean reviewAvailable = game.reviewAvailable();
        Rectangle activeStartButton = reviewAvailable ? startButton : reviewButton;
        Rectangle activeQuitButton = reviewAvailable ? quitButton : startButton;
        boolean reviewHovered = reviewAvailable && reviewButton.contains(mouse);
        boolean startHovered = activeStartButton.contains(mouse);
        boolean quitHovered = activeQuitButton.contains(mouse);

        if (reviewAvailable && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || (click && reviewHovered))) {
            game.startReviewMode();
            return;
        }
        if ((!reviewAvailable && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)))
                || (click && startHovered)) {
            game.startNewGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (click && quitHovered)) {
            Gdx.app.exit();
            return;
        }

        beginFrame();
        drawBackground();
        drawButtons(reviewAvailable, reviewHovered, startHovered, quitHovered, activeStartButton, activeQuitButton);
        drawText(reviewAvailable, reviewHovered, startHovered, quitHovered, activeStartButton, activeQuitButton);
    }

    private void drawButtons(boolean reviewAvailable, boolean reviewHovered, boolean startHovered, boolean quitHovered,
                             Rectangle activeStartButton, Rectangle activeQuitButton) {
        game.shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        if (reviewAvailable) {
            drawButton(reviewButton, reviewHovered);
        }
        drawButton(activeStartButton, startHovered);
        drawButton(activeQuitButton, quitHovered);
        game.shapes.end();
    }

    private void drawText(boolean reviewAvailable, boolean reviewHovered, boolean startHovered, boolean quitHovered,
                          Rectangle activeStartButton, Rectangle activeQuitButton) {
        game.batch.begin();
        drawTitleArt();

        game.font.getData().setScale(0.90f);
        game.font.setColor(DIM);
        game.font.draw(game.batch, reviewAvailable ? "不用再打了。" : "白雾浮岛。击落陌生的自己。",
                0f, 34f, Constants.HUD_WIDTH, Align.center, false);

        game.font.getData().setScale(1.0f);
        if (reviewAvailable) {
            drawButtonLabel(reviewButton, "回顾", reviewHovered);
        }
        drawButtonLabel(activeStartButton, "开始", startHovered);
        drawButtonLabel(activeQuitButton, "退出", quitHovered);

        game.font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        game.font.getData().setScale(1.0f);
        game.batch.end();
    }

    private void drawTitleArt() {
        TextureRegion title = game.assets.titleArt;
        float scale = 6f;
        float width = title.getRegionWidth() * scale;
        float height = title.getRegionHeight() * scale;
        float x = (Constants.HUD_WIDTH - width) * 0.5f;
        float y = Constants.HUD_HEIGHT - 470f;
        game.batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        game.batch.draw(title, x, y, width, height);
    }
}
