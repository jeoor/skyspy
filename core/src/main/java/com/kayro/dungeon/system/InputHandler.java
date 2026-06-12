package com.kayro.dungeon.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class InputHandler {
    public final Vector2 moveDirection = new Vector2();
    public final Vector2 mouseWorld = new Vector2();
    public boolean attackPressed;
    public boolean mouseAttackPressed;
    public boolean skillPressed;
    public boolean mouseSkillPressed;
    public boolean interactPressed;
    public boolean usePotionPressed;
    public boolean dashPressed;

    public void update() {
        mouseAttackPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        mouseSkillPressed = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        attackPressed = Gdx.input.isKeyJustPressed(Input.Keys.J) || mouseAttackPressed;
        skillPressed = Gdx.input.isKeyJustPressed(Input.Keys.K) || mouseSkillPressed;
        interactPressed = Gdx.input.isKeyJustPressed(Input.Keys.E);
        usePotionPressed = Gdx.input.isKeyJustPressed(Input.Keys.Q);
        dashPressed = Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT);

        moveDirection.set(0f, 0f);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveDirection.y += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveDirection.y -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveDirection.x -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveDirection.x += 1f;
        }
        if (!moveDirection.isZero()) {
            moveDirection.nor();
        }
    }
}
