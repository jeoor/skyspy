package com.kayro.dungeon.system;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraController {
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Vector2 anchor = new Vector2();
    private float mapWidth;
    private float mapHeight;

    public CameraController(OrthographicCamera camera, Viewport viewport) {
        this.camera = camera;
        this.viewport = viewport;
    }

    public void setBounds(float mapWidth, float mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public void snapTo(Vector2 target) {
        anchor.set(target);
        clampAnchor();
        camera.position.set(anchor.x, anchor.y, 0f);
        camera.update();
    }

    public void update(Vector2 target, float delta) {
        update(target, delta, 0f, 0f);
    }

    public void update(Vector2 target, float delta, float shakeX, float shakeY) {
        float lerp = Math.min(1f, 8f * delta);
        anchor.x += (target.x - anchor.x) * lerp;
        anchor.y += (target.y - anchor.y) * lerp;
        clampAnchor();
        camera.position.set(anchor.x + shakeX, anchor.y + shakeY, 0f);
        clamp();
        camera.update();
    }

    private void clamp() {
        float halfW = viewport.getWorldWidth() * 0.5f;
        float halfH = viewport.getWorldHeight() * 0.5f;
        camera.position.x = MathUtils.clamp(camera.position.x, halfW, mapWidth - halfW);
        camera.position.y = MathUtils.clamp(camera.position.y, halfH, mapHeight - halfH);
    }

    private void clampAnchor() {
        float halfW = viewport.getWorldWidth() * 0.5f;
        float halfH = viewport.getWorldHeight() * 0.5f;
        anchor.x = MathUtils.clamp(anchor.x, halfW, mapWidth - halfW);
        anchor.y = MathUtils.clamp(anchor.y, halfH, mapHeight - halfH);
    }
}
