package com.kayro.dungeon.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

public class Sfx implements Disposable {
    private final Sound[] attacks;
    private final Sound[] shots;
    private final Sound[] hits;
    private final Sound[] pickups;
    private final Sound[] potions;
    private final Sound[] stairs;
    private final Sound[] deaths;
    private final Sound[] dashes;
    private final Sound[] chests;
    private final Sound[] footsteps;

    public Sfx() {
        attacks = loadAll("sounds/wooshMono.ogg");
        shots = loadAll("sounds/laser.ogg");
        hits = loadAll("sounds/blast1.ogg");
        pickups = loadAll("sounds/gotitem.ogg");
        potions = loadAll("sounds/espark.ogg");
        stairs = loadAll("sounds/electricHum.ogg", "sounds/creak.ogg");
        deaths = loadAll("sounds/blast1.ogg");
        dashes = loadAll("sounds/woosh.ogg", "sounds/wooshMono.ogg");
        chests = loadAll("sounds/creak.ogg");
        footsteps = loadAll("sounds/footstepDirt1.ogg", "sounds/footstepDirt2.ogg",
                "sounds/footstepDirt3.ogg", "sounds/footstepDirt4.ogg",
                "sounds/footstepDirt5.ogg");
    }

    public void attack() {
        playRandom(attacks, 0.42f);
    }

    public void shoot() {
        playRandom(shots, 0.40f);
    }

    public void hit() {
        playRandom(hits, 0.42f);
    }

    public void pickup() {
        playRandom(pickups, 0.34f);
    }

    public void potion() {
        playRandom(potions, 0.34f);
    }

    public void stairs() {
        playRandom(stairs, 0.36f);
    }

    public void death() {
        playRandom(deaths, 0.44f);
    }

    public void dash() {
        playRandom(dashes, 0.26f);
    }

    public void chest() {
        playRandom(chests, 0.34f);
    }

    public void footstep() {
        playRandom(footsteps, 0.20f);
    }

    private Sound[] loadAll(String... paths) {
        Sound[] sounds = new Sound[paths.length];
        for (int i = 0; i < paths.length; i++) {
            sounds[i] = load(paths[i]);
        }
        return sounds;
    }

    private Sound load(String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            throw new com.badlogic.gdx.utils.GdxRuntimeException("asset [crash]: missing resource " + path);
        }
        return Gdx.audio.newSound(file);
    }

    private void playRandom(Sound[] sounds, float volume) {
        if (sounds == null || sounds.length == 0) {
            return;
        }
        int start = MathUtils.random(sounds.length - 1);
        for (int i = 0; i < sounds.length; i++) {
            Sound sound = sounds[(start + i) % sounds.length];
            if (sound != null) {
                sound.play(volume, MathUtils.random(0.95f, 1.05f), 0f);
                return;
            }
        }
    }

    @Override
    public void dispose() {
        dispose(attacks);
        dispose(shots);
        dispose(hits);
        dispose(pickups);
        dispose(potions);
        dispose(stairs);
        dispose(deaths);
        dispose(dashes);
        dispose(chests);
        dispose(footsteps);
    }

    private void dispose(Sound[] sounds) {
        for (Sound sound : sounds) {
            dispose(sound);
        }
    }

    private void dispose(Sound sound) {
        if (sound != null) {
            sound.dispose();
        }
    }
}
