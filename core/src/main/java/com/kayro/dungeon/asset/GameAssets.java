package com.kayro.dungeon.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.math.MathUtils;
import com.kayro.dungeon.entity.AnimationState;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.entity.ItemType;
import com.kayro.dungeon.entity.PropType;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.util.Direction;
import com.kayro.dungeon.world.DungeonMap;
import com.kayro.dungeon.world.Tile;
import com.kayro.dungeon.world.TileType;

public class GameAssets implements Disposable {
    private static final String ROOT = "";
    private static final int BLIND_TILE_WIDTH = 32;
    private static final int BLIND_TILE_HEIGHT = 26;
    private static final Color HINT_BASIC = new Color(0.92f, 0.96f, 1f, 0.88f);
    private static final Color HINT_ESCAPE = new Color(0.68f, 0.86f, 1f, 0.88f);
    private static final Color HINT_ANGER = new Color(1f, 0.62f, 0.56f, 0.90f);
    private static final Color HINT_EMPTY = new Color(1f, 1f, 1f, 0.96f);

    public static final int SKY_TILE_WIDTH = BLIND_TILE_WIDTH;
    public static final int SKY_TILE_HEIGHT = BLIND_TILE_HEIGHT;

    public final TextureRegion floor;
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
    public final TextureRegion chestClosed;
    public final TextureRegion chestOpen;
    public final TextureRegion[] chestOpenFrames;
    public final TextureRegion trap;
    public final TextureRegion barrel;
    public final TextureRegion crate;
    public final TextureRegion torch;
    public final TextureRegion rubble;
    public final TextureRegion skyFloor;
    public final TextureRegion skyFloorAlt;
    public final TextureRegion skyEdge;
    public final TextureRegion skyStarsNear;
    public final TextureRegion skyStarsFar;
    public final TextureRegion skyOrbit;
    public final TextureRegion memoryRift;
    public final TextureRegion memoryRiftBeam;
    public final TextureRegion skyProjectile;
    public final TextureRegion skyWhiteGlow;
    public final TextureRegion skyBlueGlow;
    public final TextureRegion skyRedGlow;
    public final TextureRegion skyYellowGlow;
    public final TextureRegion skyVignette;
    public final TextureRegion skyWhiteFog;
    public final TextureRegion uiVignette;
    public final TextureRegion playerShadow;
    public final TextureRegion chestShadow;
    public final TextureRegion terminal;
    public final TextureRegion terminalAlt;
    public final TextureRegion terminalScreen;
    public final TextureRegion turret;
    public final TextureRegion spark;
    public final TextureRegion titleArt;
    public final DirectionalFrameAnimationSet playerAnimations = null;
    public final DirectionalFrameAnimationSet slimeAnimations = null;

    private final Array<Texture> textures = new Array<>();
    private final Texture gameObjects;
    private final Texture whiteGameObjects;
    private final Texture soilTileset;
    private final Texture grassSet;
    private final Texture grassSetEdge;
    private final Texture starsNear;
    private final Texture starsFar;
    private final Texture orbit;
    private final Texture lampLight;
    private final Texture fireExplosionGlow;
    private final Texture scootShadow;
    private final Texture powerupSheet;
    private final Texture teleporterGlow;
    private final Texture teleporterBeamGlow;
    private final Texture whiteGlow;
    private final Texture blueGlow;
    private final Texture redGlow;
    private final Texture yellowGlow;
    private final Texture vignetteMask;
    private final Texture vignetteShadow;
    private final Texture vignetteWhiteFog;
    private final Texture introLevelMask;
    private final Texture introLevel;
    private final Pixmap soilPixmap;
    private final Pixmap grassPixmap;
    private final Pixmap grassEdgePixmap;
    private final Pixmap iconPixmap;

    private final TextureRegion[] walkDown;
    private final TextureRegion[] walkUp;
    private final TextureRegion[] walkLeft;
    private final TextureRegion[] walkRight;
    private final TextureRegion[] whiteWalkDown;
    private final TextureRegion[] whiteWalkUp;
    private final TextureRegion[] whiteWalkLeft;
    private final TextureRegion[] whiteWalkRight;
    private final TextureRegion[] whiteDeathFrames;
    private final TextureRegion[] dashFrames;
    private final TextureRegion[] deathFrames;
    private final TextureRegion[] catIdleFrames;
    private final TextureRegion[] catRunFrames;
    private final TextureRegion[] whiteCatIdleFrames;
    private final TextureRegion[] whiteCatRunFrames;
    private final TextureRegion[] memoryProps = new TextureRegion[PropType.values().length];
    private DungeonMap cachedSkyMap;
    private SkyMapTextures cachedSkyMapTextures;

    public GameAssets() {
        gameObjects = texture("textures/gameObjects.png");
        whiteGameObjects = whiteTexture("textures/gameObjects.png");
        soilTileset = texture("textures/soilTileset.png");
        grassSet = texture("textures/grassSet.png");
        grassSetEdge = texture("textures/grassSetEdge.png");
        starsNear = texture("textures/bkg_stars.png");
        starsFar = texture("textures/bkg_stars_distant.png");
        orbit = texture("textures/bkg_orbit2.png");
        lampLight = texture("textures/lampLight.png");
        fireExplosionGlow = texture("textures/fireExplosionGlow.png");
        scootShadow = texture("textures/charger_enemy_shadow.png");
        powerupSheet = texture("textures/powerupSheet.png");
        teleporterGlow = texture("textures/teleporterGlow.png");
        teleporterBeamGlow = texture("textures/teleporterBeamGlow.png");
        whiteGlow = texture("textures/whiteFloorGlow.png");
        blueGlow = texture("textures/blueFloorGlow.png");
        redGlow = texture("textures/redFloorGlow.png");
        yellowGlow = texture("textures/yellowGlow.png");
        vignetteMask = texture("textures/vignetteMask.png");
        vignetteShadow = texture("textures/vignetteShadow.png");
        vignetteWhiteFog = whiteFogTexture("textures/vignetteShadow.png");
        vignetteMask.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        vignetteShadow.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        vignetteWhiteFog.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        introLevelMask = texture("textures/introLevelMask.png");
        introLevel = texture("textures/introLevel.png");
        soilPixmap = pixmap("textures/soilTileset.png");
        grassPixmap = pixmap("textures/grassSet.png");
        grassEdgePixmap = pixmap("textures/grassSetEdge.png");
        iconPixmap = pixmap("textures/gameIcon.png");

        walkDown = row(gameObjects, 400, 108, 32, 32, 10);
        walkUp = row(gameObjects, 208, 108, 32, 32, 10);
        walkLeft = row(gameObjects, 208, 76, 32, 32, 7);
        walkRight = row(gameObjects, 432, 76, 32, 32, 7);
        whiteWalkDown = row(whiteGameObjects, 400, 108, 32, 32, 10);
        whiteWalkUp = row(whiteGameObjects, 208, 108, 32, 32, 10);
        whiteWalkLeft = row(whiteGameObjects, 208, 76, 32, 32, 7);
        whiteWalkRight = row(whiteGameObjects, 432, 76, 32, 32, 7);
        dashFrames = row(gameObjects, 208, 140, 32, 33, 16);
        deathFrames = row(gameObjects, 208, 38, 40, 38, 11);
        whiteDeathFrames = row(whiteGameObjects, 208, 38, 40, 38, 11);
        catIdleFrames = row(gameObjects, 423, 224, 32, 32, 10);
        catRunFrames = row(gameObjects, 391, 253, 36, 32, 5);
        whiteCatIdleFrames = row(whiteGameObjects, 423, 224, 32, 32, 10);
        whiteCatRunFrames = row(whiteGameObjects, 391, 253, 36, 32, 5);
        chestOpenFrames = row(gameObjects, 656, 76, 16, 30, 6);

        floor = pixels(soilTileset, 0, 0, BLIND_TILE_WIDTH, BLIND_TILE_HEIGHT);
        stairs = pixelsTransparent("textures/gameObjects.png", 390, 224, 33, 29);
        skyFloor = floor;
        skyFloorAlt = pixels(soilTileset, 32, 0, BLIND_TILE_WIDTH, BLIND_TILE_HEIGHT);
        skyEdge = pixels(soilTileset, 96, 0, BLIND_TILE_WIDTH, BLIND_TILE_HEIGHT);
        skyStarsNear = all(starsNear);
        skyStarsFar = all(starsFar);
        skyOrbit = all(orbit);
        memoryRift = all(teleporterGlow);
        memoryRiftBeam = all(teleporterBeamGlow);
        skyWhiteGlow = all(whiteGlow);
        skyBlueGlow = all(blueGlow);
        skyRedGlow = all(redGlow);
        skyYellowGlow = all(yellowGlow);
        skyVignette = all(vignetteShadow);
        skyWhiteFog = all(vignetteWhiteFog);
        uiVignette = all(vignetteMask);
        titleArt = all(texture("title.png"));
        skyProjectile = pixels(gameObjects, 44, 75, 16, 16);
        arrow = skyProjectile;
        coin = pixels(gameObjects, 13, 171, 13, 13);
        potion = pixels(gameObjects, 0, 171, 13, 13);
        sword = pixels(gameObjects, 0, 116, 16, 16);
        armor = pixels(gameObjects, 164, 145, 44, 50);
        key = pixels(gameObjects, 42, 143, 21, 27);
        heartIcon = potion;
        expIcon = coin;
        shieldIcon = pixels(gameObjects, 26, 171, 13, 13);
        chestClosed = chestOpenFrames[0];
        chestOpen = chestOpenFrames[4];
        chestShadow = pixels(gameObjects, 18, 107, 16, 8);
        playerShadow = pixels(gameObjects, 0, 100, 18, 16);
        terminal = pixels(gameObjects, 0, 132, 21, 27);
        terminalAlt = pixels(gameObjects, 21, 143, 21, 27);
        terminalScreen = pixels(gameObjects, 21, 132, 19, 11);
        turret = pixels(gameObjects, 0, 0, 16, 32);
        spark = pixels(gameObjects, 0, 116, 16, 16);
        trap = null;
        barrel = pixels(gameObjects, 80, 38, 32, 64);
        crate = terminal;
        torch = pixels(gameObjects, 40, 91, 10, 9);
        rubble = pixels(gameObjects, 54, 57, 18, 18);
        initializeMemoryProps();

        player = playerFrame(AnimationState.IDLE, Direction.DOWN, 0f);
        slime = enemyFrame(EnemyType.SLIME, AnimationState.IDLE, Direction.DOWN, 0f);
        goblin = enemyFrame(EnemyType.GOBLIN, AnimationState.IDLE, Direction.DOWN, 0f);
        skeleton = enemyFrame(EnemyType.SKELETON, AnimationState.IDLE, Direction.DOWN, 0f);
    }

    public TextureRegion floor(int x, int y) {
        return floor;
    }

    public boolean hasSkySpyTiles() {
        return true;
    }

    public boolean hasDungeonTiles() {
        return false;
    }

    public boolean hasSkySpyActors() {
        return true;
    }

    public SkyMapTextures skyMap(DungeonMap map) {
        if (map == null) {
            return null;
        }
        if (cachedSkyMap != map || cachedSkyMapTextures == null) {
            disposeSkyMapTextures();
            cachedSkyMap = map;
            cachedSkyMapTextures = buildBlindJumpMap(map);
        }
        return cachedSkyMapTextures;
    }

    public TextureRegion playerFrame(AnimationState state, float animationTime) {
        return playerFrame(state, Direction.DOWN, animationTime);
    }

    public TextureRegion playerFrame(AnimationState state, Direction direction, float animationTime) {
        return actorFrame(state, direction, animationTime,
                walkDown, walkUp, walkLeft, walkRight, deathFrames);
    }

    public TextureRegion enemyFrame(EnemyType type, AnimationState state, float animationTime) {
        return enemyFrame(type, state, Direction.DOWN, animationTime);
    }

    public TextureRegion enemyFrame(EnemyType type, AnimationState state, Direction direction, float animationTime) {
        return playerFrame(state, direction, animationTime);
    }

    public TextureRegion enemyWhiteFrame(EnemyType type, AnimationState state, Direction direction, float animationTime) {
        return actorFrame(state, direction, animationTime,
                whiteWalkDown, whiteWalkUp, whiteWalkLeft, whiteWalkRight, whiteDeathFrames);
    }

    private TextureRegion actorFrame(AnimationState state, Direction direction, float animationTime,
                                     TextureRegion[] downFrames, TextureRegion[] upFrames,
                                     TextureRegion[] leftFrames, TextureRegion[] rightFrames,
                                     TextureRegion[] deadFrames) {
        if (state == AnimationState.DEATH) {
            return frame(deadFrames, Math.min(deadFrames.length - 1, MathUtils.floor(animationTime / 0.08f)));
        }
        if (state == AnimationState.IDLE || state == AnimationState.ATTACK
                || state == AnimationState.SKILL || state == AnimationState.HURT) {
            return still(direction, downFrames, upFrames, leftFrames, rightFrames);
        }
        int tick = MathUtils.floor(animationTime / 0.10f);
        switch (direction) {
            case UP:
                return frame(upFrames, verticalFrame(tick));
            case LEFT:
                return frame(leftFrames, tick % 6);
            case RIGHT:
                return frame(rightFrames, tick % 6);
            case DOWN:
            default:
                return frame(downFrames, verticalFrame(tick));
        }
    }

    public TextureRegion enemy(EnemyType type) {
        return enemyFrame(type, AnimationState.IDLE, Direction.DOWN, 0f);
    }

    public Color enemyTint(EnemyType type) {
        return type == EnemyType.BOSS ? HINT_EMPTY : HINT_BASIC;
    }

    public TextureRegion catFrame(boolean running, float animationTime) {
        TextureRegion[] frames = running ? catRunFrames : catIdleFrames;
        return frame(frames, catFrameIndex(running, animationTime));
    }

    public TextureRegion catWhiteFrame(boolean running, float animationTime) {
        TextureRegion[] frames = running ? whiteCatRunFrames : whiteCatIdleFrames;
        return frame(frames, catFrameIndex(running, animationTime));
    }

    private int catFrameIndex(boolean running, float animationTime) {
        int max = running ? 5 : 10;
        return MathUtils.floor(animationTime / 0.05f) % max;
    }

    public TextureRegion item(ItemType type) {
        switch (type) {
            case COIN:
                return coin;
            case POTION:
                return potion;
            case KEY:
                return key;
            case ARMOR_UPGRADE:
                return armor;
            case SWORD_UPGRADE:
            case RELIC:
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
        float frameDuration = Chest.OPEN_ANIMATION_DURATION / chestOpenFrames.length;
        int index = MathUtils.clamp(MathUtils.floor(chest.openAnimationTime() / frameDuration),
                0, chestOpenFrames.length - 1);
        return chestOpenFrames[index];
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
            case WHITE_TREE:
            case SWING:
            case DOOR_FRAME:
            case PHOTO_FRAME:
            case BROKEN_BOWL:
            case MIRROR_SHARD:
            case BED_OUTLINE:
                return memoryProps[type.ordinal()];
            default:
                return null;
        }
    }

    private void initializeMemoryProps() {
        memoryProps[PropType.WHITE_TREE.ordinal()] = createMemoryProp(PropType.WHITE_TREE);
        memoryProps[PropType.SWING.ordinal()] = createMemoryProp(PropType.SWING);
        memoryProps[PropType.DOOR_FRAME.ordinal()] = createMemoryProp(PropType.DOOR_FRAME);
        memoryProps[PropType.PHOTO_FRAME.ordinal()] = createMemoryProp(PropType.PHOTO_FRAME);
        memoryProps[PropType.BROKEN_BOWL.ordinal()] = createMemoryProp(PropType.BROKEN_BOWL);
        memoryProps[PropType.MIRROR_SHARD.ordinal()] = createMemoryProp(PropType.MIRROR_SHARD);
        memoryProps[PropType.BED_OUTLINE.ordinal()] = createMemoryProp(PropType.BED_OUTLINE);
    }

    private TextureRegion createMemoryProp(PropType type) {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        pixmap.setColor(0.90f, 0.95f, 1f, 0.92f);
        switch (type) {
            case WHITE_TREE:
                pixmap.fillRectangle(14, 5, 4, 20);
                pixmap.drawLine(16, 22, 7, 29);
                pixmap.drawLine(16, 19, 25, 28);
                pixmap.drawLine(15, 16, 8, 21);
                pixmap.drawLine(17, 14, 24, 20);
                break;
            case SWING:
                pixmap.fillRectangle(4, 27, 24, 2);
                pixmap.drawLine(10, 27, 10, 10);
                pixmap.drawLine(22, 27, 22, 10);
                pixmap.fillRectangle(8, 8, 16, 3);
                break;
            case DOOR_FRAME:
                pixmap.fillRectangle(5, 4, 3, 25);
                pixmap.fillRectangle(24, 4, 3, 25);
                pixmap.fillRectangle(5, 27, 22, 3);
                break;
            case PHOTO_FRAME:
                pixmap.drawRectangle(5, 8, 22, 17);
                pixmap.drawRectangle(7, 10, 18, 13);
                pixmap.drawLine(9, 12, 15, 18);
                pixmap.drawLine(15, 18, 19, 14);
                pixmap.drawLine(19, 14, 24, 20);
                break;
            case BROKEN_BOWL:
                pixmap.drawLine(5, 18, 10, 9);
                pixmap.drawLine(10, 9, 15, 6);
                pixmap.drawLine(18, 7, 23, 10);
                pixmap.drawLine(23, 10, 27, 18);
                pixmap.drawLine(5, 18, 13, 18);
                pixmap.drawLine(18, 18, 27, 18);
                pixmap.drawLine(13, 18, 17, 14);
                break;
            case MIRROR_SHARD:
                pixmap.drawLine(16, 3, 27, 13);
                pixmap.drawLine(27, 13, 18, 29);
                pixmap.drawLine(18, 29, 6, 21);
                pixmap.drawLine(6, 21, 16, 3);
                pixmap.drawLine(10, 20, 22, 10);
                break;
            case BED_OUTLINE:
                pixmap.drawRectangle(3, 7, 27, 17);
                pixmap.fillRectangle(3, 5, 3, 24);
                pixmap.drawRectangle(7, 17, 8, 5);
                pixmap.drawLine(7, 9, 28, 9);
                break;
            default:
                break;
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(texture);
        pixmap.dispose();
        return new TextureRegion(texture);
    }

    private TextureRegion still(Direction direction, TextureRegion[] downFrames, TextureRegion[] upFrames,
                                TextureRegion[] leftFrames, TextureRegion[] rightFrames) {
        switch (direction) {
            case UP:
                return frame(upFrames, 5);
            case LEFT:
                return frame(leftFrames, 6);
            case RIGHT:
                return frame(rightFrames, 6);
            case DOWN:
            default:
                return frame(downFrames, 5);
        }
    }

    private int verticalFrame(int tick) {
        switch (tick % 10) {
            case 0: return 1;
            case 1:
            case 2: return 2;
            case 3: return 1;
            case 4: return 0;
            case 5: return 3;
            case 6:
            case 7: return 4;
            case 8: return 3;
            case 9:
            default: return 0;
        }
    }

    private TextureRegion frame(TextureRegion[] frames, int index) {
        return frames[MathUtils.clamp(index, 0, frames.length - 1)];
    }

    private SkyMapTextures buildBlindJumpMap(DungeonMap map) {
        int width = Constants.MAP_WIDTH * BLIND_TILE_WIDTH;
        int height = Constants.MAP_HEIGHT * BLIND_TILE_HEIGHT;
        Pixmap base = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        Pixmap edge = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        base.setBlending(Pixmap.Blending.None);
        edge.setBlending(Pixmap.Blending.None);
        base.setColor(0, 0, 0, 0);
        edge.setColor(0, 0, 0, 0);
        base.fill();
        edge.fill();
        base.setBlending(Pixmap.Blending.SourceOver);
        edge.setBlending(Pixmap.Blending.SourceOver);
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                Tile tile = map.getTile(x, y);
                if (tile.skyKind != 0) {
                    drawBlindJumpTile(base, edge, tile, x, y);
                }
            }
        }
        Texture baseTexture = new Texture(base);
        Texture edgeTexture = new Texture(edge);
        baseTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        edgeTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        base.dispose();
        edge.dispose();
        return new SkyMapTextures(baseTexture, edgeTexture, width, height);
    }

    private void drawBlindJumpTile(Pixmap base, Pixmap edge, Tile tile, int x, int y) {
        int dx = x * BLIND_TILE_WIDTH;
        int dy = (Constants.MAP_HEIGHT - 1 - y) * BLIND_TILE_HEIGHT;
        switch (tile.skyKind) {
            case Tile.SKY_SAND:
                copyTile(base, soilPixmap, dx, dy, 32, 0);
                break;
            case Tile.SKY_SAND_GRASS:
                copyTile(base, soilPixmap, dx, dy, 64, 0);
                break;
            case Tile.SKY_PLATE_LOWER_EDGE:
                copyTile(base, soilPixmap, dx, dy, 128, 0);
                break;
            case Tile.SKY_PLATE_UPPER_EDGE:
                copyTile(edge, soilPixmap, dx, dy, tile.variant % 3 == 0 ? 96 : tile.variant % 3 == 1 ? 288 : 320, 0);
                break;
            case Tile.SKY_GRASS:
                copyTile(base, soilPixmap, dx, dy, 0, 0);
                copyTile(base, tile.variant % 3 == 2 ? grassPixmap : grassEdgePixmap, dx, dy, tile.skyMask * 32, 0);
                break;
            case Tile.SKY_GRASS_FLOWERS:
                copyTile(base, soilPixmap, dx, dy, 32, 0);
                copyTile(base, tile.variant % 3 == 2 ? grassPixmap : grassEdgePixmap, dx, dy, tile.skyMask * 32, 0);
                break;
            case Tile.SKY_GRASS_LOWER_EDGE:
                copyTile(base, soilPixmap, dx, dy, 224, 0);
                break;
            case Tile.SKY_GRASS_UPPER_EDGE:
                copyTile(edge, soilPixmap, dx, dy, tile.variant % 3 == 2 ? 160 : 192, 0);
                break;
            case Tile.SKY_GRATE:
                copyTile(base, soilPixmap, dx, dy, 256, 0);
                break;
            case Tile.SKY_PLATE:
            default:
                copyTile(base, soilPixmap, dx, dy, 0, 0);
                break;
        }
    }

    private void copyTile(Pixmap target, Pixmap source, int dx, int dy, int sx, int sy) {
        for (int x = 0; x < BLIND_TILE_WIDTH; x++) {
            for (int y = 0; y < BLIND_TILE_HEIGHT; y++) {
                int px = source.getPixel(sx + x, sy + y);
                int alpha = px & 0xff;
                if (alpha != 0) {
                    target.drawPixel(dx + x, dy + y, px);
                }
            }
        }
    }

    private Texture texture(String path) {
        FileHandle file = Gdx.files.internal(ROOT + path);
        if (!file.exists()) {
            throw new GdxRuntimeException("asset [crash]: missing resource " + ROOT + path);
        }
        Texture texture = new Texture(file);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(texture);
        return texture;
    }

    private Texture whiteTexture(String path) {
        Pixmap source = pixmap(path);
        Pixmap white = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
        white.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getPixel(x, y);
                int alpha = pixel & 0xff;
                white.drawPixel(x, y, alpha == 0 ? 0 : (0xffffff00 | alpha));
            }
        }
        Texture texture = new Texture(white);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(texture);
        source.dispose();
        white.dispose();
        return texture;
    }

    private Texture whiteFogTexture(String path) {
        Pixmap source = pixmap(path);
        Pixmap fog = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
        fog.setBlending(Pixmap.Blending.None);
        float centerX = (source.getWidth() - 1f) * 0.5f;
        float centerY = (source.getHeight() - 1f) * 0.5f;
        float maxDistance = (float)Math.sqrt(centerX * centerX + centerY * centerY);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int sourceAlpha = source.getPixel(x, y) & 0xff;
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float)Math.sqrt(dx * dx + dy * dy) / maxDistance;
                float radialFog = smoothStep(0.10f, 0.52f, distance);
                float sourceFog = sourceAlpha / 255f * 2.6f;
                float fogAlpha = Math.max(radialFog, sourceFog);
                int alpha = MathUtils.clamp(Math.round(fogAlpha * 246f), 0, 246);
                fog.drawPixel(x, y, alpha <= 1 ? 0 : (0xffffff00 | alpha));
            }
        }
        Texture texture = new Texture(fog);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        source.dispose();
        fog.dispose();
        return texture;
    }

    private float smoothStep(float edge0, float edge1, float value) {
        float t = MathUtils.clamp((value - edge0) / (edge1 - edge0), 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    private Pixmap pixmap(String path) {
        FileHandle file = Gdx.files.internal(ROOT + path);
        if (!file.exists()) {
            throw new GdxRuntimeException("asset [crash]: missing resource " + ROOT + path);
        }
        return new Pixmap(file);
    }

    private TextureRegion[] row(Texture texture, int x, int y, int width, int height, int count) {
        TextureRegion[] frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) {
            frames[i] = pixels(texture, x + i * width, y, width, height);
        }
        return frames;
    }

    private TextureRegion pixels(Texture texture, int x, int y, int width, int height) {
        if (texture == null || x + width > texture.getWidth() || y + height > texture.getHeight()) {
            throw new GdxRuntimeException("asset [crash]: sprite frame outside texture");
        }
        return new TextureRegion(texture, x, y, width, height);
    }

    private TextureRegion pixelsTransparent(String path, int x, int y, int width, int height) {
        Pixmap source = pixmap(path);
        if (x + width > source.getWidth() || y + height > source.getHeight()) {
            source.dispose();
            throw new GdxRuntimeException("asset [crash]: sprite frame outside texture");
        }
        Pixmap target = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        target.setBlending(Pixmap.Blending.None);
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int pixel = source.getPixel(x + px, y + py);
                int rgb = pixel >>> 8;
                int alpha = pixel & 0xff;
                target.drawPixel(px, py, alpha == 0 || rgb == 0 ? 0 : pixel);
            }
        }
        Texture texture = new Texture(target);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(texture);
        source.dispose();
        target.dispose();
        return new TextureRegion(texture);
    }

    private TextureRegion all(Texture texture) {
        return new TextureRegion(texture);
    }

    @Override
    public void dispose() {
        disposeSkyMapTextures();
        soilPixmap.dispose();
        grassPixmap.dispose();
        grassEdgePixmap.dispose();
        iconPixmap.dispose();
        for (Texture texture : textures) {
            texture.dispose();
        }
    }

    private void disposeSkyMapTextures() {
        if (cachedSkyMapTextures != null) {
            cachedSkyMapTextures.dispose();
            cachedSkyMapTextures = null;
        }
        cachedSkyMap = null;
    }

    public static final class SkyMapTextures implements Disposable {
        public final Texture base;
        public final Texture edge;
        public final int width;
        public final int height;

        private SkyMapTextures(Texture base, Texture edge, int width, int height) {
            this.base = base;
            this.edge = edge;
            this.width = width;
            this.height = height;
        }

        @Override
        public void dispose() {
            base.dispose();
            edge.dispose();
        }
    }
}
