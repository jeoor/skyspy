package com.kayro.dungeon.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.kayro.dungeon.entity.AnimationState;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.entity.ItemType;
import com.kayro.dungeon.entity.PropType;
import com.kayro.dungeon.util.Direction;

public class GameAssets implements Disposable {
    private static final String PACK = "";
    private static final int TILE = 16;
    private static final Color SLIME_TINT = Color.WHITE;
    private static final Color GOBLIN_TINT = new Color(0.88f, 0.66f, 0.48f, 1f);
    private static final Color SKELETON_TINT = new Color(1.0f, 0.96f, 0.82f, 1f);
    private static final Color BOSS_TINT = new Color(1.0f, 0.36f, 0.30f, 1f);

    public final TextureRegion floor;
    public final TextureRegion wall;
    public final TextureRegion stairs;
    public final TextureRegion player;
    public final TextureRegion goblin;
    public final TextureRegion skeleton;
    public final TextureRegion slime;
    public final TextureRegion arrow;
    public final TextureRegion coin;
    public final TextureRegion potion;
    public final TextureRegion sword;
    public final TextureRegion armor;
    public final TextureRegion key;
    public final TextureRegion heartIcon;
    public final TextureRegion expIcon;
    public final TextureRegion shieldIcon;
    public final TextureRegion emptyHeartGauge;
    public final TextureRegion chestClosed;
    public final TextureRegion chestOpen;
    public final TextureRegion[] chestOpenFrames;
    public final TextureRegion trap;
    public final TextureRegion barrel;
    public final TextureRegion crate;
    public final TextureRegion torch;
    public final TextureRegion rubble;
    public final DirectionalFrameAnimationSet playerAnimations;
    public final DirectionalFrameAnimationSet slimeAnimations;

    private final Array<Texture> textures = new Array<>();
    private final Texture tileset;
    private final Texture playerSheet;
    private final Texture slimeSheet;
    private final Texture itemsSheet;
    private final Texture propsSheet;
    private final Texture uiSheet;
    private final TextureRegion[] floorVariants;

    public GameAssets() {
        tileset = loadTexture(PACK + "dungeon-pack_free_tileset_1.png", ProceduralTextures::dungeonTileset);
        playerSheet = loadTexture(PACK + "dungeon-pack_free_player.png",
                () -> ProceduralTextures.actorSheet(new Color(0.64f, 0.68f, 0.64f, 1f),
                        new Color(0.72f, 0.18f, 0.12f, 1f)));
        slimeSheet = loadTexture(PACK + "dungeon-pack_free_slime.png",
                () -> ProceduralTextures.actorSheet(new Color(0.26f, 0.70f, 0.20f, 1f),
                        new Color(0.12f, 0.28f, 0.10f, 1f)));
        itemsSheet = loadTexture(PACK + "dungeon-pack_free_items.png", ProceduralTextures::itemSheet);
        propsSheet = loadTexture(PACK + "dungeon-pack_free_props.png", ProceduralTextures::propSheet);
        uiSheet = loadTexture(PACK + "dungeon-pack_free_ui.png", ProceduralTextures::uiSheet);

        floorVariants = new TextureRegion[] {
                cell(tileset, 10, 2), cell(tileset, 11, 2), cell(tileset, 12, 2),
                cell(tileset, 10, 3), cell(tileset, 11, 3), cell(tileset, 12, 3)
        };
        floor = first(floorVariants);
        wall = cell(tileset, 8, 5);
        stairs = cell(tileset, 13, 1);
        potion = cell(itemsSheet, 1, 2);
        coin = cell(itemsSheet, 5, 2);
        sword = cell(itemsSheet, 3, 2);
        armor = cell(itemsSheet, 3, 2);
        arrow = cell(itemsSheet, 6, 2);
        key = cell(itemsSheet, 4, 2);
        heartIcon = cell(uiSheet, 5, 1);
        expIcon = cell(uiSheet, 5, 2);
        shieldIcon = cell(uiSheet, 6, 2);
        emptyHeartGauge = region(uiSheet, TILE * 5, 0, TILE * 5, TILE * 2);
        chestOpenFrames = new TextureRegion[] {
                cell(propsSheet, 0, 6), cell(propsSheet, 1, 6), cell(propsSheet, 2, 6), cell(propsSheet, 3, 6)
        };
        chestClosed = chestOpenFrames[0];
        chestOpen = chestOpenFrames[chestOpenFrames.length - 1];
        trap = null;
        barrel = cell(propsSheet, 1, 1);
        crate = cell(propsSheet, 2, 1);
        torch = cell(propsSheet, 1, 3);
        rubble = cell(propsSheet, 4, 5);

        playerAnimations = directional(playerSheet);
        slimeAnimations = directional(slimeSheet);
        player = playerFrame(AnimationState.IDLE, Direction.DOWN, 0f);
        slime = enemyFrame(EnemyType.SLIME, AnimationState.IDLE, Direction.DOWN, 0f);
        goblin = enemyFrame(EnemyType.GOBLIN, AnimationState.IDLE, Direction.DOWN, 0f);
        skeleton = enemyFrame(EnemyType.SKELETON, AnimationState.IDLE, Direction.DOWN, 0f);
    }

    public TextureRegion floor(int x, int y) {
        int count = availableCount(floorVariants);
        if (count == 0) {
            return floor;
        }
        int index = Math.abs((x * 31 + y * 17) % count);
        return floorVariants[index];
    }

    public TextureRegion wall(int x, int y) {
        return wall;
    }

    public TextureRegion playerFrame(AnimationState state, float animationTime) {
        return playerFrame(state, Direction.DOWN, animationTime);
    }

    public TextureRegion playerFrame(AnimationState state, Direction direction, float animationTime) {
        return playerAnimations == null ? null : playerAnimations.frame(state, direction, animationTime);
    }

    public TextureRegion enemyFrame(EnemyType type, AnimationState state, float animationTime) {
        return enemyFrame(type, state, Direction.DOWN, animationTime);
    }

    public TextureRegion enemyFrame(EnemyType type, AnimationState state, Direction direction, float animationTime) {
        return slimeAnimations == null ? null : slimeAnimations.frame(state, direction, animationTime);
    }

    public TextureRegion enemy(EnemyType type) {
        return enemyFrame(type, AnimationState.IDLE, Direction.DOWN, 0f);
    }

    public Color enemyTint(EnemyType type) {
        switch (type) {
            case BOSS:
                return BOSS_TINT;
            case GOBLIN:
                return GOBLIN_TINT;
            case SKELETON:
                return SKELETON_TINT;
            case SLIME:
            default:
                return SLIME_TINT;
        }
    }

    public TextureRegion item(ItemType type) {
        switch (type) {
            case COIN:
                return coin;
            case POTION:
                return potion;
            case SWORD_UPGRADE:
                return sword;
            case ARMOR_UPGRADE:
                return armor;
            case KEY:
                return key;
            case RELIC:
                return sword;
            case WEAPON:
                return sword;
            default:
                return null;
        }
    }

    public TextureRegion chestFrame(Chest chest) {
        if (!chest.opened) {
            return chestClosed;
        }
        if (chestOpenFrames.length == 0 || chestOpenFrames[0] == null) {
            return chestOpen;
        }
        float frameDuration = Chest.OPEN_ANIMATION_DURATION / chestOpenFrames.length;
        int index = MathUtils.clamp(MathUtils.floor(chest.openAnimationTime() / frameDuration),
                0, chestOpenFrames.length - 1);
        TextureRegion frame = chestOpenFrames[index];
        return frame == null ? chestOpen : frame;
    }

    public TextureRegion prop(PropType type) {
        switch (type) {
            case BARREL:
                return barrel;
            case CRATE:
                return crate;
            case TORCH:
                return torch;
            case RUBBLE:
                return rubble;
            default:
                return null;
        }
    }

    public boolean hasDungeonTiles() {
        return floor != null && wall != null;
    }

    private DirectionalFrameAnimationSet directional(Texture texture) {
        if (texture == null) {
            return null;
        }
        return SpriteAnimations.directional16(texture);
    }

    private TextureRegion cell(Texture texture, int col, int row) {
        if (texture == null) {
            return null;
        }
        int x = col * TILE;
        int y = row * TILE;
        if (x + TILE > texture.getWidth() || y + TILE > texture.getHeight()) {
            return null;
        }
        return new TextureRegion(texture, x, y, TILE, TILE);
    }

    private TextureRegion region(Texture texture, int x, int y, int width, int height) {
        if (texture == null || x + width > texture.getWidth() || y + height > texture.getHeight()) {
            return null;
        }
        return new TextureRegion(texture, x, y, width, height);
    }

    private TextureRegion first(TextureRegion[] regions) {
        for (TextureRegion region : regions) {
            if (region != null) {
                return region;
            }
        }
        return null;
    }

    private int availableCount(TextureRegion[] regions) {
        int count = 0;
        for (TextureRegion region : regions) {
            if (region != null) {
                count++;
            }
        }
        return count;
    }

    private Texture loadTexture(String path, PixmapFactory fallbackFactory) {
        FileHandle file = Gdx.files.internal(path);
        Texture texture;
        if (file.exists()) {
            texture = new Texture(file);
        } else {
            Pixmap pixmap = fallbackFactory.create();
            texture = new Texture(pixmap);
            pixmap.dispose();
        }
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(texture);
        return texture;
    }

    private interface PixmapFactory {
        Pixmap create();
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
    }
}
