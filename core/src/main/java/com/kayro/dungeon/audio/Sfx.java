package com.kayro.dungeon.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

public class Sfx implements Disposable {
    private final Sound[] attacks;
    private final Sound[] hits;
    private final Sound[] pickups;
    private final Sound[] potions;
    private final Sound[] stairs;
    private final Sound[] deaths;
    private final Sound[] dashes;
    private final Sound[] chests;
    private final Sound[] footsteps;

    public Sfx() {
        attacks = loadAll("Audio/knifeSlice.ogg", "Audio/knifeSlice2.ogg",
                "Audio/drawKnife1.ogg", "Audio/drawKnife2.ogg", "Audio/drawKnife3.ogg");
        hits = loadAll("Audio/chop.ogg", "Audio/cloth1.ogg", "Audio/cloth2.ogg",
                "Audio/cloth3.ogg", "Audio/cloth4.ogg");
        pickups = loadAll("Audio/handleCoins.ogg", "Audio/handleCoins2.ogg",
                "Audio/handleSmallLeather.ogg", "Audio/handleSmallLeather2.ogg",
                "Audio/beltHandle1.ogg", "Audio/beltHandle2.ogg");
        potions = loadAll("Audio/metalPot1.ogg", "Audio/metalPot2.ogg", "Audio/metalPot3.ogg",
                "Audio/metalClick.ogg");
        stairs = loadAll("Audio/doorOpen_1.ogg", "Audio/doorOpen_2.ogg",
                "Audio/creak1.ogg", "Audio/creak2.ogg", "Audio/creak3.ogg");
        deaths = loadAll("Audio/dropLeather.ogg", "Audio/cloth3.ogg", "Audio/cloth4.ogg",
                "Audio/metalPot3.ogg");
        dashes = loadAll("Audio/clothBelt.ogg", "Audio/clothBelt2.ogg",
                "Audio/beltHandle1.ogg", "Audio/beltHandle2.ogg");
        chests = loadAll("Audio/metalLatch.ogg", "Audio/metalClick.ogg",
                "Audio/creak1.ogg", "Audio/creak2.ogg");
        footsteps = loadAll("Audio/footstep00.ogg", "Audio/footstep01.ogg",
                "Audio/footstep02.ogg", "Audio/footstep03.ogg", "Audio/footstep04.ogg",
                "Audio/footstep05.ogg", "Audio/footstep06.ogg", "Audio/footstep07.ogg",
                "Audio/footstep08.ogg", "Audio/footstep09.ogg");
    }

    public void attack() {
        playRandom(attacks, 0.42f);
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
        return file.exists() ? Gdx.audio.newSound(file) : null;
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
