package com.kayro.dungeon.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kayro.dungeon.audio.Sfx;
import com.kayro.dungeon.entity.AttackEffect;
import com.kayro.dungeon.entity.Chest;
import com.kayro.dungeon.entity.DamageText;
import com.kayro.dungeon.entity.DecorProp;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.EnemyType;
import com.kayro.dungeon.entity.Entity;
import com.kayro.dungeon.entity.Item;
import com.kayro.dungeon.entity.ItemType;
import com.kayro.dungeon.entity.Particle;
import com.kayro.dungeon.entity.Player;
import com.kayro.dungeon.entity.PropType;
import com.kayro.dungeon.entity.Projectile;
import com.kayro.dungeon.entity.Shop;
import com.kayro.dungeon.entity.ShopItem;
import com.kayro.dungeon.entity.StoryBossKind;
import com.kayro.dungeon.entity.Trap;
import com.kayro.dungeon.entity.WeaponType;
import com.kayro.dungeon.system.AISystem;
import com.kayro.dungeon.system.CombatSystem;
import com.kayro.dungeon.system.FogOfWarSystem;
import com.kayro.dungeon.system.InputHandler;
import com.kayro.dungeon.system.LevelSystem;
import com.kayro.dungeon.system.LootSystem;
import com.kayro.dungeon.system.SpawnerSystem;
import com.kayro.dungeon.util.Constants;
import com.kayro.dungeon.util.Difficulty;

public class GameWorld {
    private static final String[] OPENING_START_LINES = {
            "他们在靠近。",
            "我必须穿过他们。"
    };
    private static final String[] OPENING_ECHO_LINES = {
            "上面有东西在叹气。",
            "他说别上来。",
            "凭什么。"
    };
    private static final String[] CAT_BOSS_INTRO_LINES = {
            "它贴着地面移动。",
            "它回头的样子很熟。"
    };
    private static final String[] CAT_GLIMPSE_LINES = {
            "伏地的东西回头了。",
            "我好像见过它。"
    };
    private static final String[] PLEASER_BOSS_INTRO_LINES = {
            "他在鞠躬。",
            "他守着门。"
    };
    private static final String[] COLD_FLOOR_MEMORY_LINES = {
            "地板很冷。",
            "我没有出声。"
    };
    private static final String[] CAT_MEMORY_LINES = {
            "碗碎了。",
            "有人在喊。"
    };
    private static final String[] CAT_SPARE_LINES = {
            "它看着我。",
            "我没有动手。",
            "它靠近了一步。"
    };
    private static final String[] CAT_FLEE_LINES = {
            "它跑了。",
            "我该等一等。"
    };
    private static final String[] FINAL_CAT_LINES = {
            "只有你。",
            "你没有拦我。"
    };
    private static final String[] MIRROR_BOSS_INTRO_LINES = {
            "他们跟着我动。",
            "他们用手遮住眼睛。"
    };
    private static final String[] MIRROR_STILL_LINES = {
            "他们遮住眼睛。",
            "我也是。"
    };
    private static final String[] MIRROR_MEMORY_LINES = {
            "手张开了。",
            "指缝里有光。"
    };
    private static final String[] EMPTY_SELF_INTRO_LINES = {
            "有人在上面等。",
            "他看起来很累。"
    };
    private static final String[] EMPTY_SELF_MEMORY_LINES = {
            "不是只有疼。",
            "我一直是你。"
    };
    private static final String[] FINAL_DEBATE_ATTACK_LINES = {
            "太早了。",
            "白色还在。"
    };
    private static final String[] FINAL_DEBATE_RESOLVE_LINES = {
            "不是只有疼。",
            "从来不是只有疼。"
    };
    private static final String[][] FINAL_DEBATE_LINES = {
            {
                    "他替你低头。",
                    "地板很冷。",
                    "猫靠近了。"
            },
            {
                    "他替你生气。",
                    "碗碎了。",
                    "两只手捡起碎片。"
            },
            {
                    "他们遮住你的眼睛。",
                    "指缝里有光。"
            }
    };
    private static final String[] REVIEW_START_LINES = {
            "不用再打了。",
            "把他们接回来。"
    };
    private static final String[] REVIEW_NO_ATTACK_LINES = {
            "不用再打了。"
    };
    private static final String[] REVIEW_SOOTHE_LINES = {
            "他不再低头。",
            "他放下手。",
            "白色褪下来了。",
            "猫一直守着。"
    };
    private static final float STORY_LINE_SECONDS = 1.85f;
    private static final float STORY_FLASH_SECONDS = 1.35f;
    private static final float OPENING_ECHO_DISTANCE = Constants.TILE_WIDTH * 5f;
    private static final float FINAL_BOSS_INTERACT_DISTANCE = Constants.TILE_WIDTH * 5f;
    private static final float STORY_CAT_INTERACT_DISTANCE = Constants.TILE_WIDTH * 3f;
    private static final float STORY_CAT_SHOT_DISTANCE = Constants.TILE_WIDTH * 8f;
    private static final float FINAL_DEBATE_WAIT_SECONDS = 3f;
    private static final float MIRROR_MEMORY_STEP = 0.28f;
    private static final float MIRROR_STILL_SECONDS = 2f;
    private static final float MIRROR_STILL_VISUAL_SECONDS = 3f;
    private static final int SILENCE_LEAK_DEATHS = 15;
    private static final int SILENCE_LEAK_FLOOR = 4;
    private static final int MEMORY_COLD_FLOOR = 1;
    private static final int MEMORY_BROKEN_BOWL = 1 << 1;
    private static final int MEMORY_UNDER_BED = 1 << 2;

    public int floor = 1;
    public int kills = 0;
    public int voidKills;
    public int shellBreaks;
    public int relicsFound;
    public int strongestHit;
    public boolean gameOver;
    public boolean runComplete;
    public boolean trueEnding;
    public boolean reviewComplete;
    public final boolean reviewMode;
    public BiomeType biome = BiomeType.CATACOMBS;
    public DungeonMap map;
    public Player player;
    public final Array<Enemy> enemies = new Array<>();
    public final Array<DecorProp> props = new Array<>();
    public final Array<Chest> chests = new Array<>();
    public final Array<Item> items = new Array<>();
    public final Array<Trap> traps = new Array<>();
    public Shop shop;
    public boolean shopOpen;
    public final Array<Projectile> projectiles = new Array<>();
    public final Array<DamageText> damageTexts = new Array<>();
    public final Array<AttackEffect> attackEffects = new Array<>();
    public final Array<Particle> particles = new Array<>();
    public final Vector2 cameraShake = new Vector2();
    public final Vector2 storyCatPosition = new Vector2();
    public float visualTime;
    public Sfx sfx;
    public final InputHandler input = new InputHandler();
    public final LevelSystem levelSystem = new LevelSystem();
    public final LootSystem lootSystem = new LootSystem();
    public final CombatSystem combatSystem = new CombatSystem();
    private final DungeonGenerator generator = new DungeonGenerator();
    private final FogOfWarSystem fog = new FogOfWarSystem();
    private final AISystem aiSystem = new AISystem();
    private final SpawnerSystem spawnerSystem = new SpawnerSystem();
    public final Difficulty difficulty;
    private final WeaponType startingWeapon;
    private final int deathCountAtRunStart;
    private final int highFloorAtRunStart;
    private int reviewSoothedCount;
    private int reviewFloorSoothedCount;
    private int reviewTargetCount;
    private boolean reviewNoAttackShown;
    private float mirrorMemoryTimer;
    private final Vector2 mirrorMemoryNow = new Vector2();
    private final Vector2 mirrorMemoryOne = new Vector2();
    private final Vector2 mirrorMemoryTwo = new Vector2();
    private float hitStopTimer;
    private float shakeTimer;
    private float shakeDuration = 1f;
    private float shakeMagnitude;
    private String[] storySequence;
    private int storyIndex;
    private float storyLineTimer;
    private float storyFlashTimer;
    private String storyLine;
    private int openingEchoRequiredKills;
    private boolean openingEchoShown;
    private boolean stageBossSpawned;
    private boolean stageBossMemoryShown;
    private boolean mirrorStillHintShown;
    private float mirrorStillTimer;
    private float mirrorStillVisualTimer;
    private boolean finalDebateActive;
    private boolean finalDebateComplete;
    private boolean finalDebateAggressed;
    private boolean finalDebateWaitChosen;
    private boolean finalDebateWaiting;
    private float finalDebateWaitTimer;
    public boolean storyCatVisible;
    public boolean storyCatSpared;
    private boolean storyCatFled;
    private boolean earlyCatGlanceShown;
    private boolean finalCatSeen;
    private int finalDebateStage;
    private int runMemories;
    private final Vector2 catShotVector = new Vector2();

    public GameWorld() {
        this(null);
    }

    public GameWorld(Sfx sfx) {
        this(sfx, WeaponType.SWORD);
    }

    public GameWorld(Sfx sfx, WeaponType startingWeapon) {
        this(sfx, startingWeapon, Difficulty.NORMAL);
    }

    public GameWorld(Sfx sfx, WeaponType startingWeapon, Difficulty difficulty) {
        this(sfx, startingWeapon, difficulty, 0);
    }

    public GameWorld(Sfx sfx, WeaponType startingWeapon, Difficulty difficulty, int deathCountAtRunStart) {
        this(sfx, startingWeapon, difficulty, deathCountAtRunStart, 1, false);
    }

    public GameWorld(Sfx sfx, WeaponType startingWeapon, Difficulty difficulty,
                     int deathCountAtRunStart, boolean reviewMode) {
        this(sfx, startingWeapon, difficulty, deathCountAtRunStart, 1, reviewMode);
    }

    public GameWorld(Sfx sfx, WeaponType startingWeapon, Difficulty difficulty,
                     int deathCountAtRunStart, int highFloorAtRunStart, boolean reviewMode) {
        this.sfx = sfx;
        this.startingWeapon = startingWeapon == null ? WeaponType.SWORD : startingWeapon;
        this.difficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        this.deathCountAtRunStart = Math.max(0, deathCountAtRunStart);
        this.highFloorAtRunStart = Math.max(1, highFloorAtRunStart);
        this.reviewMode = reviewMode;
        newRun();
    }

    public void newRun() {
        floor = 1;
        kills = 0;
        voidKills = 0;
        shellBreaks = 0;
        relicsFound = 0;
        strongestHit = 0;
        gameOver = false;
        runComplete = false;
        trueEnding = false;
        reviewComplete = false;
        reviewSoothedCount = 0;
        reviewFloorSoothedCount = 0;
        reviewTargetCount = 0;
        reviewNoAttackShown = false;
        storyCatVisible = false;
        storyCatSpared = false;
        storyCatFled = false;
        finalCatSeen = false;
        runMemories = 0;
        loadFloor();
    }

    public void nextFloor() {
        floor++;
        Player.State saved = player.saveState();
        loadFloor();
        player.restoreState(saved);
    }

    public void update(float delta) {
        input.update();
        visualTime += delta;
        if (runComplete) {
            return;
        }
        updateCameraShake(delta);
        updateStory(delta);
        updateMirrorMemory(delta);
        if (updateHitStop(delta)) {
            return;
        }
        if (reviewMode) {
            updateReview(delta);
            return;
        }
        if (player.isDead()) {
            player.update(delta, this);
            updateDamageTexts(delta);
            updateAttackEffects(delta);
            updateParticles(delta);
            fog.update(map, player);
            if (player.isDeathAnimationDone()) {
                gameOver = true;
            }
            return;
        }

        boolean usedStairs = false;
        boolean interactionHandled = input.interactPressed && (tryInteractStoryCat() || tryAdvanceFinalDebate());
        if (!interactionHandled && input.interactPressed && player.getCenter().dst(map.stairsPosition) < 52f) {
            if (hasLiveBoss()) {
                addDamageText("先清门", player.getCenter().x - 16f, player.getCenter().y + 28f, Color.SCARLET);
                shake(3f, 0.10f);
            } else if (player.keys <= 0) {
                addDamageText("找碎片", player.getCenter().x - 10f, player.getCenter().y + 28f, Color.GOLD);
                shake(2f, 0.08f);
            } else {
                player.keys--;
                if (sfx != null) {
                    sfx.stairs();
                }
                if (floor >= 5) {
                    completeRun();
                } else {
                    preserveUnharmedStoryCatBeforeLeaving();
                    nextFloor();
                }
                usedStairs = true;
            }
        }
        if (!usedStairs) {
            player.update(delta, this);
            updateVoidFalls();
            updateMirrorStillHint(delta);
            updateTraps(delta);
            updateChests(delta);
            updateFinalDebateWait(delta);
            if (player.isDead()) {
                updateDamageTexts(delta);
                updateAttackEffects(delta);
                updateParticles(delta);
                fog.update(map, player);
                return;
            }
            aiSystem.update(this, delta);
            combatSystem.update(this, delta);
            updateVoidFalls();
            if (!player.isDead()) {
                spawnerSystem.update(this, delta);
                openChests();
                openShop();
                pickupItems();
            }
            updateDamageTexts(delta);
            updateAttackEffects(delta);
            updateParticles(delta);
            fog.update(map, player);
        }

        maybeTriggerOpeningEcho();
        maybeTriggerStageBossMemory();
        maybeTriggerEarlyCatGlance();
        if (player.isDeathAnimationDone()) {
            gameOver = true;
        }
    }

    public void shake(float magnitude, float duration) {
        if (duration <= 0f || magnitude <= 0f) {
            return;
        }
        shakeMagnitude = Math.max(shakeMagnitude, magnitude);
        shakeDuration = duration;
        shakeTimer = Math.max(shakeTimer, duration);
    }

    public void hitStop(float duration) {
        if (duration <= 0f) {
            return;
        }
        hitStopTimer = Math.max(hitStopTimer, duration);
    }

    public void recordStrongHit(int damage) {
        strongestHit = Math.max(strongestHit, damage);
    }

    public void moveEntity(Entity entity, float dx, float dy) {
        if (dx != 0f && canMovePastChests(entity, dx, 0f)) {
            entity.position.x += dx;
        }
        if (dy != 0f && canMovePastChests(entity, 0f, dy)) {
            entity.position.y += dy;
        }
    }

    public void addEnemy(Enemy enemy) {
        enemy.maxHp = Math.round(enemy.maxHp * difficulty.enemyHpMultiplier);
        enemy.hp = enemy.maxHp;
        enemy.attack = Math.round(enemy.attack * difficulty.enemyDamageMultiplier);
        enemies.add(enemy);
    }

    private void loadFloor() {
        biome = BiomeType.forFloor(floor);
        map = generator.generate();
        player = new Player(map.playerSpawn.x, map.playerSpawn.y);
        player.maxHp = Math.round(player.maxHp * difficulty.playerHpMultiplier);
        player.hp = player.maxHp;
        player.potions = difficulty.startingPotions;
        player.equipWeapon(startingWeapon);
        enemies.clear();
        props.clear();
        chests.clear();
        items.clear();
        traps.clear();
        projectiles.clear();
        damageTexts.clear();
        attackEffects.clear();
        particles.clear();
        storyCatVisible = false;
        resetStoryState();
        cameraShake.setZero();
        hitStopTimer = 0f;
        shakeTimer = 0f;
        shakeMagnitude = 0f;
        fog.update(map, player);
        resetMirrorMemory();
        spawnerSystem.spawnInitial(this);
        if (reviewMode) {
            if (enemies.size == 0) {
                Vector2 position = randomFloorPosition(Constants.TILE_WIDTH * 4f, 80);
                if (position != null) {
                    addEnemy(new Enemy(EnemyType.SLIME, position.x, position.y, floor, biome, difficulty));
                }
            }
            reviewFloorSoothedCount = 0;
            reviewTargetCount = enemies.size;
            seedReviewCat();
            seedProps();
            startStory(REVIEW_START_LINES, false);
            return;
        }
        if (floor == 1) {
            openingEchoRequiredKills = Math.max(1, enemies.size);
            startStory(OPENING_START_LINES, false);
        }
        seedItems();
        seedChests();
        seedShop();
        seedBossRoom();
        seedStoryCat();
        seedProps();
    }

    private void seedItems() {
        int count = MathUtils.random(5, 8);
        for (int i = 0; i < count; i++) {
            for (int attempt = 0; attempt < 60; attempt++) {
                int x = MathUtils.random(2, Constants.MAP_WIDTH - 3);
                int y = MathUtils.random(2, Constants.MAP_HEIGHT - 3);
                Tile tile = map.getTile(x, y);
                if (!tile.isWalkable() || (!map.fallingVoid && tile.visible)) {
                    continue;
                }
                float worldX = map.tileCenterX(x);
                float worldY = map.tileCenterY(y);
                if (player.getCenter().dst(worldX, worldY) < Constants.TILE_SIZE * 6f) {
                    continue;
                }
                ItemType type = MathUtils.randomBoolean(0.72f) ? ItemType.COIN : ItemType.POTION;
                int amount = type == ItemType.COIN ? MathUtils.random(1, 3 + floor) : 1;
                items.add(new Item(type, worldX, worldY, amount));
                break;
            }
        }
    }

    private void seedChests() {
        int count = MathUtils.random(2, 3);
        for (int i = 0; i < count; i++) {
            Vector2 position = randomFloorPosition(Constants.TILE_SIZE * 7f, 80);
            if (position != null) {
                chests.add(new Chest(position.x, position.y, false));
            }
        }
    }

    private void seedProps() {
        if (floor <= 1) {
            return;
        }
        int remaining = floor == 3 ? 4 : floor >= 5 ? 2 : 3;
        for (Room room : map.rooms) {
            if (remaining <= 0) {
                break;
            }
            if (player.getCenter().dst(room.centerWorld(Constants.TILE_WIDTH, Constants.TILE_HEIGHT)) < Constants.TILE_WIDTH * 3f) {
                continue;
            }
            int propCount = Math.min(remaining, MathUtils.random(1, 2));
            for (int i = 0; i < propCount; i++) {
                int tileX = MathUtils.random(room.x + 1, room.x + room.width - 2);
                int tileY = MathUtils.random(room.y + 1, room.y + room.height - 2);
                if (!map.isWalkableTile(tileX, tileY)) {
                    continue;
                }
                float worldX = map.tileCenterX(tileX);
                float worldY = map.tileCenterY(tileY);
                if (!isPropPlacementSafe(worldX, worldY)) {
                    continue;
                }
                PropType type = randomPropType(room, tileX, tileY);
                props.add(new DecorProp(type, worldX, worldY));
                remaining--;
            }
        }
    }

    private boolean isPropPlacementSafe(float worldX, float worldY) {
        if (player.getCenter().dst(worldX, worldY) < Constants.TILE_WIDTH * 3f
                || map.stairsPosition.dst(worldX, worldY) < Constants.TILE_WIDTH * 2f
                || isPlacementOccupied(worldX, worldY)) {
            return false;
        }
        if (storyCatVisible && storyCatPosition.dst(worldX, worldY) < Constants.TILE_WIDTH * 2f) {
            return false;
        }
        if (shop != null && shop.getCenter().dst(worldX, worldY) < Constants.TILE_WIDTH * 2f) {
            return false;
        }
        for (Item item : items) {
            if (item.getCenter().dst(worldX, worldY) < Constants.TILE_WIDTH) {
                return false;
            }
        }
        for (DecorProp prop : props) {
            if (prop.getCenter().dst(worldX, worldY) < Constants.TILE_WIDTH * 1.5f) {
                return false;
            }
        }
        return true;
    }

    private PropType randomPropType(Room room, int tileX, int tileY) {
        boolean nearEdge = tileX <= room.x + 1 || tileX >= room.x + room.width - 2
                || tileY <= room.y + 1 || tileY >= room.y + room.height - 2;
        float roll = MathUtils.random();
        switch (floor) {
            case 2:
                return roll < 0.68f ? PropType.WHITE_TREE : PropType.SWING;
            case 3:
                if (roll < 0.28f && nearEdge) {
                    return PropType.DOOR_FRAME;
                }
                if (roll < 0.58f) {
                    return PropType.PHOTO_FRAME;
                }
                return PropType.BROKEN_BOWL;
            case 4:
                return roll < 0.74f ? PropType.MIRROR_SHARD : PropType.PHOTO_FRAME;
            default:
                return PropType.BED_OUTLINE;
        }
    }

    private void seedShop() {
        shopOpen = false;
        Vector2 position = randomFloorPosition(Constants.TILE_SIZE * 6f, 80);
        shop = position != null ? new Shop(position.x, position.y) : null;
    }

    public boolean tryBuy(ShopItem item) {
        int cost = item.price(floor);
        if (player.gold < cost) {
            return false;
        }
        player.gold -= cost;
        item.apply(player);
        addDamageText(item.label, player.getCenter().x - 12f, player.getCenter().y + 24f, Color.GREEN);
        addParticleBurst(player.getCenter().x, player.getCenter().y, Color.GREEN, 12);
        if (sfx != null) {
            sfx.chest();
        }
        return true;
    }

    private void seedTraps() {
        if (map.fallingVoid) {
            return;
        }
        int count = MathUtils.random(5, 7 + Math.min(5, floor)) + biome.extraTraps;
        for (int i = 0; i < count; i++) {
            Vector2 position = randomFloorPosition(Constants.TILE_SIZE * 5f, 80);
            if (position != null) {
                traps.add(new Trap(position.x, position.y));
            }
        }
    }

    private void seedBossRoom() {
        if (!isStoryBossFloor() || map.bossRoom == null) {
            return;
        }
        Vector2 bossPosition = roomPosition(map.bossRoom, 1, 1);
        Enemy boss = new Enemy(EnemyType.BOSS, bossPosition.x, bossPosition.y, floor, biome, difficulty);
        boss.storyBossKind = storyBossKindForFloor();
        addEnemy(boss);
        stageBossSpawned = true;
        if (floor == 5) {
            finalDebateActive = true;
        }
        Vector2 chestPosition = roomPosition(map.bossRoom, -1, -1);
        chests.add(new Chest(chestPosition.x, chestPosition.y, true));
        addDamageText(gateText(), player.getCenter().x - 18f, player.getCenter().y + 30f, Color.SCARLET);
        String[] intro = stageBossIntroLines();
        if (intro != null) {
            startStory(intro, true);
        }
    }

    private void seedStoryCat() {
        storyCatVisible = false;
        if (floor == 2 && !earlyCatGlanceShown) {
            Vector2 position = randomFloorPosition(Constants.TILE_SIZE * 7f, 80);
            if (position != null) {
                storyCatPosition.set(position);
                storyCatVisible = true;
            }
            return;
        }
        if (floor == 4 && !storyCatSpared && !storyCatFled) {
            Vector2 position = randomFloorPosition(Constants.TILE_SIZE * 7f, 80);
            if (position != null) {
                storyCatPosition.set(position);
                storyCatVisible = true;
            }
            return;
        }
        if (floor == 5 && storyCatSpared && !finalCatSeen) {
            Vector2 position = map.bossRoom == null
                    ? randomFloorPosition(Constants.TILE_SIZE * 5f, 80)
                    : roomPosition(map.bossRoom, -2, 0);
            if (position != null) {
                storyCatPosition.set(position);
                storyCatVisible = true;
            }
        }
    }

    private void seedReviewCat() {
        Vector2 position = randomFloorPosition(Constants.TILE_SIZE * 5f, 80);
        if (position != null) {
            storyCatPosition.set(position);
            storyCatVisible = true;
        }
    }

    private boolean isStoryBossFloor() {
        return floor >= 2 && floor <= 5;
    }

    private StoryBossKind storyBossKindForFloor() {
        switch (floor) {
            case 2:
                return StoryBossKind.PLEASER;
            case 3:
                return StoryBossKind.CAT;
            case 4:
                return StoryBossKind.MIRROR;
            case 5:
                return StoryBossKind.EMPTY_SELF;
            default:
                return StoryBossKind.NONE;
        }
    }

    private String gateText() {
        switch (floor) {
            case 2:
                return "讨好之门";
            case 3:
                return "猫的轮廓";
            case 4:
                return "镜面之门";
            case 5:
                return "空自己";
            default:
                return "门醒了";
        }
    }

    private Vector2 randomFloorPosition(float minPlayerDistance, int attempts) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = MathUtils.random(2, Constants.MAP_WIDTH - 3);
            int y = MathUtils.random(2, Constants.MAP_HEIGHT - 3);
            Tile tile = map.getTile(x, y);
            if (!tile.isWalkable() || (!map.fallingVoid && tile.visible)) {
                continue;
            }
            float worldX = map.tileCenterX(x);
            float worldY = map.tileCenterY(y);
            if (player.getCenter().dst(worldX, worldY) < minPlayerDistance) {
                continue;
            }
            if (new Vector2(worldX, worldY).dst(map.stairsPosition) < Constants.TILE_SIZE * 2f) {
                continue;
            }
            if (isPlacementOccupied(worldX, worldY)) {
                continue;
            }
            return new Vector2(worldX, worldY);
        }
        return null;
    }

    private boolean isPlacementOccupied(float worldX, float worldY) {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getCenter().dst(worldX, worldY) < Constants.TILE_SIZE) {
                return true;
            }
        }
        for (Chest chest : chests) {
            if (chest.getCenter().dst(worldX, worldY) < Constants.TILE_SIZE) {
                return true;
            }
        }
        for (Trap trap : traps) {
            if (trap.getCenter().dst(worldX, worldY) < Constants.TILE_SIZE) {
                return true;
            }
        }
        return false;
    }

    private Vector2 roomPosition(Room room, int offsetX, int offsetY) {
        int tileX = MathUtils.clamp(room.centerX() + offsetX, room.x + 1, room.x + room.width - 2);
        int tileY = MathUtils.clamp(room.centerY() + offsetY, room.y + 1, room.y + room.height - 2);
        return map.tileCenter(tileX, tileY);
    }

    public boolean hasLiveBoss() {
        for (Enemy enemy : enemies) {
            if (enemy.type == EnemyType.BOSS && !enemy.isDead()) {
                return true;
            }
        }
        return false;
    }

    public boolean exitReady() {
        if (reviewMode) {
            return reviewFloorComplete();
        }
        return player.keys > 0 && !hasLiveBoss();
    }

    public boolean reviewFloorComplete() {
        return reviewMode && reviewTargetCount > 0 && reviewFloorSoothedCount >= reviewTargetCount;
    }

    public Vector2 mirrorDelayedTarget() {
        return mirrorMemoryTwo;
    }

    private void resetMirrorMemory() {
        Vector2 center = player.getCenter();
        mirrorMemoryNow.set(center);
        mirrorMemoryOne.set(center);
        mirrorMemoryTwo.set(center);
        mirrorMemoryTimer = 0f;
    }

    private void updateMirrorMemory(float delta) {
        mirrorMemoryTimer -= delta;
        if (mirrorMemoryTimer > 0f || player == null) {
            return;
        }
        mirrorMemoryTimer = MIRROR_MEMORY_STEP;
        mirrorMemoryTwo.set(mirrorMemoryOne);
        mirrorMemoryOne.set(mirrorMemoryNow);
        mirrorMemoryNow.set(player.getCenter());
    }

    private void updateMirrorStillHint(float delta) {
        if (mirrorStillVisualTimer > 0f) {
            mirrorStillVisualTimer = Math.max(0f, mirrorStillVisualTimer - delta);
        }
        if (floor != 4 || mirrorStillHintShown || storySequence != null || player == null || player.isDead()) {
            mirrorStillTimer = 0f;
            return;
        }
        boolean hasMirrorNearby = false;
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                continue;
            }
            if (enemy.type != EnemyType.MIRROR && enemy.storyBossKind != StoryBossKind.MIRROR) {
                continue;
            }
            if (player.getCenter().dst(enemy.getCenter()) < Constants.TILE_WIDTH * 8f) {
                hasMirrorNearby = true;
                break;
            }
        }
        if (!hasMirrorNearby || !input.moveDirection.isZero(0.01f) || input.shootHeld || input.dashPressed) {
            mirrorStillTimer = 0f;
            return;
        }
        mirrorStillTimer += delta;
        if (mirrorStillTimer < MIRROR_STILL_SECONDS) {
            return;
        }
        mirrorStillHintShown = true;
        mirrorStillVisualTimer = MIRROR_STILL_VISUAL_SECONDS;
        startStory(MIRROR_STILL_LINES, true);
        addDamageText("遮住了", player.getCenter().x - 18f, player.getCenter().y + 30f, Color.WHITE);
        shake(1.8f, 0.12f);
    }

    private void updateReview(float delta) {
        boolean soothePressed = input.attackPressed || input.skillPressed;
        if (soothePressed) {
            player.shootPrepTimer = 0f;
            player.shootBulletTimer = 0f;
            if (!sootheNearestReviewSelf()) {
                showReviewNoAttack();
            }
        } else if (input.shootHeld) {
            player.shootPrepTimer = 0f;
            player.shootBulletTimer = 0f;
            showReviewNoAttack();
        }
        player.update(delta, this);
        recoverReviewFall();
        if (input.interactPressed) {
            if (!tryUseReviewRift()) {
                sootheNearestReviewSelf();
            }
        }
        for (Enemy enemy : enemies) {
            enemy.update(delta, this);
            enemy.setLocomotionState(false);
        }
        updateDamageTexts(delta);
        updateAttackEffects(delta);
        updateParticles(delta);
        fog.update(map, player);
    }

    private boolean tryUseReviewRift() {
        if (reviewTargetCount <= 0 || reviewFloorSoothedCount < reviewTargetCount) {
            return false;
        }
        if (player.getCenter().dst(map.stairsPosition) > 52f) {
            return false;
        }
        if (floor >= 4) {
            reviewComplete = true;
            return true;
        }
        floor++;
        loadFloor();
        return true;
    }

    private boolean sootheNearestReviewSelf() {
        Enemy target = null;
        float bestDistance = Constants.TILE_WIDTH * 2.3f;
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                continue;
            }
            float distance = player.getCenter().dst(enemy.getCenter());
            if (distance < bestDistance) {
                bestDistance = distance;
                target = enemy;
            }
        }
        if (target == null) {
            return false;
        }
        addParticleBurst(target.getCenter().x, target.getCenter().y, Color.WHITE, 24);
        addDamageText(reviewSootheLine(), target.getCenter().x - 28f, target.getCenter().y + 26f, Color.WHITE);
        enemies.removeValue(target, true);
        reviewSoothedCount++;
        reviewFloorSoothedCount++;
        if (sfx != null) {
            sfx.pickup();
        }
        return true;
    }

    private void showReviewNoAttack() {
        if (!reviewNoAttackShown && storySequence == null) {
            reviewNoAttackShown = true;
            startStory(REVIEW_NO_ATTACK_LINES, false);
            addDamageText("不用再打了", player.getCenter().x - 26f, player.getCenter().y + 30f, Color.WHITE);
        }
    }

    private String reviewSootheLine() {
        int index = MathUtils.clamp(reviewSoothedCount, 0, REVIEW_SOOTHE_LINES.length - 1);
        return REVIEW_SOOTHE_LINES[index];
    }

    private void recoverReviewFall() {
        if (!map.fallingVoid || map.isWalkableWorld(player.getCenter().x, player.getCenter().y)) {
            return;
        }
        player.position.set(map.playerSpawn.x - 14f, map.playerSpawn.y - 14f);
        player.hp = player.maxHp;
        addDamageText("留下", player.getCenter().x - 10f, player.getCenter().y + 24f, Color.WHITE);
        shake(2f, 0.10f);
    }

    private void completeRun() {
        runComplete = true;
        trueEnding = hasUnlockedSilenceLeak()
                && finalDebateComplete
                && finalDebateWaitChosen
                && storyCatSpared
                && !finalDebateAggressed;
        startStory(EMPTY_SELF_MEMORY_LINES, true);
        shake(3.5f, 0.20f);
    }

    private boolean hasUnlockedSilenceLeak() {
        return deathCountAtRunStart >= SILENCE_LEAK_DEATHS
                && Math.max(highFloorAtRunStart, floor) >= SILENCE_LEAK_FLOOR;
    }

    public void onPlayerShot(Vector2 direction) {
        if (!storyCatVisible || storyCatSpared || storyCatFled || floor != 4 || direction == null) {
            return;
        }
        float distance = player.getCenter().dst(storyCatPosition);
        if (distance > STORY_CAT_SHOT_DISTANCE) {
            return;
        }
        catShotVector.set(storyCatPosition).sub(player.getCenter());
        if (catShotVector.isZero(0.01f)) {
            return;
        }
        catShotVector.nor();
        if (catShotVector.dot(direction) < 0.82f) {
            return;
        }
        storyCatVisible = false;
        storyCatFled = true;
        startStory(CAT_FLEE_LINES, true);
        addDamageText("跑了", storyCatPosition.x - 10f, storyCatPosition.y + 20f, Color.SCARLET);
        shake(2f, 0.12f);
    }

    private boolean tryInteractStoryCat() {
        if (floor != 4 && floor != 5) {
            return false;
        }
        if (!storyCatVisible || player.getCenter().dst(storyCatPosition) > STORY_CAT_INTERACT_DISTANCE) {
            return false;
        }
        if (storySequence != null) {
            return true;
        }
        storyCatVisible = false;
        if (floor == 4) {
            storyCatSpared = true;
            startStory(CAT_SPARE_LINES, true);
            addDamageText("等等", storyCatPosition.x - 8f, storyCatPosition.y + 20f, Color.WHITE);
        } else if (floor == 5) {
            finalCatSeen = true;
            startStory(FINAL_CAT_LINES, true);
            addDamageText("猫", storyCatPosition.x - 6f, storyCatPosition.y + 20f, Color.WHITE);
        }
        if (sfx != null) {
            sfx.pickup();
        }
        return true;
    }

    private void preserveUnharmedStoryCatBeforeLeaving() {
        if (floor == 4 && storyCatVisible && !storyCatFled) {
            storyCatSpared = true;
        }
    }

    public boolean isFinalBossDebating(Enemy enemy) {
        return enemy != null
                && floor == 5
                && enemy.type == EnemyType.BOSS
                && finalDebateActive
                && !finalDebateComplete
                && !enemy.isDead();
    }

    public boolean isFinalBossDefending(Enemy enemy) {
        return enemy != null
                && floor == 5
                && enemy.type == EnemyType.BOSS
                && finalDebateActive
                && finalDebateComplete
                && !enemy.isDead();
    }

    public int finalDebateVisualStage() {
        if (!finalDebateActive || finalDebateComplete) {
            return 0;
        }
        return MathUtils.clamp(finalDebateStage + 1, 1, FINAL_DEBATE_LINES.length);
    }

    public boolean isFinalDebateWaiting() {
        return finalDebateWaiting && !finalDebateComplete;
    }

    public boolean blockDamageTo(Enemy enemy) {
        if (!isFinalBossDebating(enemy)) {
            return false;
        }
        if (finalDebateWaiting) {
            finalDebateAggressed = true;
            finalDebateWaiting = false;
            finalDebateComplete = true;
            addDamageText("太早", enemy.getCenter().x - 16f, enemy.getCenter().y + 28f, Color.SCARLET);
            if (storySequence == null) {
                startStory(FINAL_DEBATE_ATTACK_LINES, false);
            }
            return false;
        }
        finalDebateAggressed = true;
        addDamageText("等等", enemy.getCenter().x - 8f, enemy.getCenter().y + 28f, Color.WHITE);
        if (storySequence == null) {
            startStory(FINAL_DEBATE_ATTACK_LINES, false);
        }
        return true;
    }

    private boolean tryAdvanceFinalDebate() {
        Enemy boss = finalBoss();
        if (!isFinalBossDebating(boss)) {
            return false;
        }
        if (player.getCenter().dst(boss.getCenter()) > FINAL_BOSS_INTERACT_DISTANCE) {
            return false;
        }
        if (storySequence != null) {
            return true;
        }
        if (finalDebateWaiting) {
            return true;
        }
        if (!hasMemoryForDebateStage(finalDebateStage)) {
            String memoryName = memoryNameForDebateStage(finalDebateStage);
            addDamageText("缺少" + memoryName, boss.getCenter().x - 28f,
                    boss.getCenter().y + 32f, Color.SCARLET);
            shake(1.8f, 0.10f);
            return true;
        }
        startStory(FINAL_DEBATE_LINES[finalDebateStage], true);
        finalDebateStage++;
        if (finalDebateStage >= FINAL_DEBATE_LINES.length) {
            finalDebateWaiting = true;
            finalDebateWaitTimer = FINAL_DEBATE_WAIT_SECONDS;
            addDamageText("等等", boss.getCenter().x - 8f, boss.getCenter().y + 32f, Color.WHITE);
        } else {
            addDamageText("记忆", boss.getCenter().x - 14f, boss.getCenter().y + 32f, Color.CYAN);
        }
        shake(2.5f, 0.16f);
        if (sfx != null) {
            sfx.pickup();
        }
        return true;
    }

    private boolean hasMemoryForDebateStage(int stage) {
        int required = stage == 0 ? MEMORY_COLD_FLOOR
                : stage == 1 ? MEMORY_BROKEN_BOWL : MEMORY_UNDER_BED;
        return (runMemories & required) != 0;
    }

    private String memoryNameForDebateStage(int stage) {
        if (stage == 0) {
            return "冷地板";
        }
        if (stage == 1) {
            return "碎碗";
        }
        return "床底";
    }

    private void updateFinalDebateWait(float delta) {
        if (!finalDebateWaiting || finalDebateComplete) {
            return;
        }
        Enemy boss = finalBoss();
        if (boss == null || boss.isDead()) {
            finalDebateWaiting = false;
            return;
        }
        if (storySequence != null) {
            return;
        }
        if (input.shootHeld || input.attackPressed || input.skillPressed) {
            finalDebateAggressed = true;
            finalDebateWaiting = false;
            finalDebateComplete = true;
            addDamageText("太早", boss.getCenter().x - 16f, boss.getCenter().y + 28f, Color.SCARLET);
            startStory(FINAL_DEBATE_ATTACK_LINES, false);
            return;
        }
        finalDebateWaitTimer = Math.max(0f, finalDebateWaitTimer - delta);
        if (finalDebateWaitTimer > 0f) {
            return;
        }
        finalDebateWaiting = false;
        finalDebateWaitChosen = true;
        finalDebateComplete = true;
        startStory(FINAL_DEBATE_RESOLVE_LINES, true);
        addDamageText("想起", boss.getCenter().x - 16f, boss.getCenter().y + 32f, Color.GOLD);
        shake(2.5f, 0.16f);
        if (sfx != null) {
            sfx.pickup();
        }
    }

    private Enemy finalBoss() {
        if (floor != 5) {
            return null;
        }
        for (Enemy enemy : enemies) {
            if (enemy.type == EnemyType.BOSS && !enemy.isDead()) {
                return enemy;
            }
        }
        return null;
    }

    public boolean hasStoryLine() {
        return storyLine != null && storyLineTimer > 0f;
    }

    public String storyLine() {
        return storyLine;
    }

    public float storyAlpha() {
        if (!hasStoryLine()) {
            return 0f;
        }
        float fade = 0.30f;
        float fadeIn = STORY_LINE_SECONDS - storyLineTimer;
        return MathUtils.clamp(Math.min(fadeIn, storyLineTimer) / fade, 0f, 1f);
    }

    public float storyFlashAlpha() {
        if (storyFlashTimer <= 0f) {
            return 0f;
        }
        return MathUtils.clamp(storyFlashTimer / STORY_FLASH_SECONDS, 0f, 1f) * 0.24f;
    }

    public float mirrorStillVisualAlpha() {
        if (mirrorStillVisualTimer <= 0f) {
            return 0f;
        }
        return MathUtils.clamp(mirrorStillVisualTimer / MIRROR_STILL_VISUAL_SECONDS, 0f, 1f);
    }

    private void resetStoryState() {
        storySequence = null;
        storyLine = null;
        storyIndex = 0;
        storyLineTimer = 0f;
        storyFlashTimer = 0f;
        openingEchoRequiredKills = 0;
        openingEchoShown = floor != 1;
        stageBossSpawned = false;
        stageBossMemoryShown = false;
        mirrorStillHintShown = false;
        mirrorStillTimer = 0f;
        mirrorStillVisualTimer = 0f;
        finalDebateActive = false;
        finalDebateComplete = false;
        finalDebateAggressed = false;
        finalDebateWaitChosen = false;
        finalDebateWaiting = false;
        finalDebateWaitTimer = 0f;
        earlyCatGlanceShown = false;
        finalDebateStage = 0;
    }

    private void updateStory(float delta) {
        if (storyFlashTimer > 0f) {
            storyFlashTimer = Math.max(0f, storyFlashTimer - delta);
        }
        if (storyLineTimer <= 0f) {
            return;
        }
        storyLineTimer = Math.max(0f, storyLineTimer - delta);
        if (storyLineTimer == 0f) {
            advanceStoryLine();
        }
    }

    private void maybeTriggerOpeningEcho() {
        if (openingEchoShown || floor != 1 || player == null || map == null) {
            return;
        }
        if (openingEchoRequiredKills <= 0 || kills < openingEchoRequiredKills) {
            return;
        }
        if (storySequence != null || player.getCenter().dst(map.stairsPosition) > OPENING_ECHO_DISTANCE) {
            return;
        }
        openingEchoShown = true;
        startStory(OPENING_ECHO_LINES, true);
        shake(2.5f, 0.18f);
        if (sfx != null) {
            sfx.stairs();
        }
    }

    private void maybeTriggerStageBossMemory() {
        if (!stageBossSpawned || stageBossMemoryShown || hasLiveBoss()) {
            return;
        }
        unlockMemoryForFloor();
        if (storySequence != null) {
            return;
        }
        String[] lines = stageBossMemoryLines();
        if (lines == null) {
            return;
        }
        stageBossMemoryShown = true;
        startStory(lines, true);
        shake(2.8f, 0.18f);
        if (sfx != null) {
            sfx.pickup();
        }
    }

    private void unlockMemoryForFloor() {
        int memory = floor == 2 ? MEMORY_COLD_FLOOR
                : floor == 3 ? MEMORY_BROKEN_BOWL
                : floor == 4 ? MEMORY_UNDER_BED : 0;
        if (memory == 0 || (runMemories & memory) != 0) {
            return;
        }
        runMemories |= memory;
        addDamageText("记忆 " + Integer.bitCount(runMemories) + "/3",
                player.getCenter().x - 22f, player.getCenter().y + 30f, Color.CYAN);
        addParticleBurst(player.getCenter().x, player.getCenter().y, Color.WHITE, 18);
    }

    private void maybeTriggerEarlyCatGlance() {
        if (floor != 2 || earlyCatGlanceShown || !storyCatVisible || storySequence != null || player == null) {
            return;
        }
        if (player.getCenter().dst(storyCatPosition) > Constants.TILE_WIDTH * 4f) {
            return;
        }
        earlyCatGlanceShown = true;
        storyCatVisible = false;
        startStory(CAT_GLIMPSE_LINES, true);
        addDamageText("熟悉", storyCatPosition.x - 18f, storyCatPosition.y + 20f, Color.WHITE);
        shake(1.6f, 0.10f);
    }

    private String[] stageBossIntroLines() {
        switch (floor) {
            case 2:
                return PLEASER_BOSS_INTRO_LINES;
            case 3:
                return CAT_BOSS_INTRO_LINES;
            case 4:
                return MIRROR_BOSS_INTRO_LINES;
            case 5:
                return EMPTY_SELF_INTRO_LINES;
            default:
                return null;
        }
    }

    private String[] stageBossMemoryLines() {
        switch (floor) {
            case 2:
                return COLD_FLOOR_MEMORY_LINES;
            case 3:
                return CAT_MEMORY_LINES;
            case 4:
                return MIRROR_MEMORY_LINES;
            case 5:
                return EMPTY_SELF_MEMORY_LINES;
            default:
                return null;
        }
    }

    private void startStory(String[] lines, boolean flash) {
        storySequence = lines;
        storyIndex = -1;
        advanceStoryLine();
        if (flash) {
            storyFlashTimer = STORY_FLASH_SECONDS;
        }
    }

    private void advanceStoryLine() {
        if (storySequence == null) {
            storyLine = null;
            storyLineTimer = 0f;
            return;
        }
        storyIndex++;
        if (storyIndex >= storySequence.length) {
            storySequence = null;
            storyLine = null;
            storyLineTimer = 0f;
            return;
        }
        storyLine = storySequence[storyIndex];
        storyLineTimer = STORY_LINE_SECONDS;
    }

    private void updateTraps(float delta) {
        for (Trap trap : traps) {
            trap.update(delta, this);
            if (!trap.isReady() || player.invincibleTimer > 0f || !trap.getBounds().overlaps(player.getBounds())) {
                continue;
            }
            int damage = Math.max(4, Math.round((10 + floor * 2 + biome.trapDamageBonus) * difficulty.trapDamageMultiplier) - player.defense);
            trap.trigger();
            player.takeDamage(damage);
            player.invincibleTimer = Math.max(player.invincibleTimer, 0.25f);
            addDamageText("-" + damage, player.getCenter().x, player.getCenter().y + 22f, Color.ORANGE);
            addParticleBurst(player.getCenter().x, player.getCenter().y, Color.ORANGE, 12);
            shake(player.isDead() ? 7f : 4f, 0.14f);
            if (sfx != null) {
                sfx.hit();
                if (player.isDead()) {
                    sfx.death();
                }
            }
        }
    }

    private void updateChests(float delta) {
        for (int i = chests.size - 1; i >= 0; i--) {
            Chest chest = chests.get(i);
            chest.update(delta, this);
            if (chest.isOpenAnimationDone()) {
                chests.removeIndex(i);
            }
        }
    }

    private void openShop() {
        if (!input.interactPressed || shop == null) {
            return;
        }
        if (player.getCenter().dst(shop.getCenter()) <= Shop.INTERACT_RANGE) {
            shopOpen = true;
        }
    }

    private void openChests() {
        if (!input.interactPressed) {
            return;
        }
        for (Chest chest : chests) {
            if (chest.opened || chest.getCenter().dst(player.getCenter()) > 56f) {
                continue;
            }
            if (chest.bossChest && hasLiveBoss()) {
                addDamageText("门锁着", chest.getCenter().x - 18f, chest.getCenter().y + 22f, Color.SCARLET);
                return;
            }
            chest.open();
            addParticleBurst(chest.getCenter().x, chest.getCenter().y, Color.GOLD, chest.bossChest ? 24 : 16);
            dropChestLoot(chest);
            if (sfx != null) {
                sfx.chest();
            }
            shake(chest.bossChest ? 3f : 1.5f, 0.08f);
            return;
        }
    }

    private void dropChestLoot(Chest chest) {
        float x = chest.getCenter().x;
        float y = chest.getCenter().y;
        int gold = chest.bossChest ? MathUtils.random(8, 14 + floor * 2) : MathUtils.random(4, 8 + floor);
        items.add(new Item(ItemType.COIN, x - 12f, y, gold));
        items.add(new Item(ItemType.POTION, x + 12f, y, 1));
        if (chest.bossChest || (player.keys == 0 && MathUtils.randomBoolean(0.55f)) || MathUtils.randomBoolean(0.08f)) {
            items.add(new Item(ItemType.KEY, x, y - 16f, 1));
        }
        if (chest.bossChest || MathUtils.randomBoolean(0.35f)) {
            ItemType upgrade = MathUtils.randomBoolean() ? ItemType.SWORD_UPGRADE : ItemType.ARMOR_UPGRADE;
            items.add(new Item(upgrade, x, y + 16f, 1));
        }
        if (chest.bossChest || MathUtils.randomBoolean(0.58f)) {
            items.add(new Item(lootSystem.randomRelic(), x, y + 32f));
        }
        if (chest.bossChest || MathUtils.randomBoolean(0.12f)) {
            items.add(new Item(lootSystem.randomWeapon(), x + 18f, y + 32f));
        }
        addDamageText(chest.bossChest ? "门匣" : "匣子", x - 12f, y + 24f, Color.GOLD);
    }

    private void pickupItems() {
        for (int i = items.size - 1; i >= 0; i--) {
            Item item = items.get(i);
            float distance = item.getCenter().dst(player.getCenter());
            if (distance <= 30f || (input.interactPressed && distance <= 48f)) {
                item.apply(player);
                if (item.type == ItemType.RELIC) {
                    relicsFound++;
                }
                lootSystem.addPickupText(this, item);
                addParticleBurst(item.getCenter().x, item.getCenter().y, item.type == ItemType.POTION ? Color.GREEN : Color.GOLD, 6);
                if (sfx != null) {
                    sfx.pickup();
                }
                items.removeIndex(i);
            }
        }
    }

    private void updateDamageTexts(float delta) {
        for (int i = damageTexts.size - 1; i >= 0; i--) {
            DamageText text = damageTexts.get(i);
            text.update(delta);
            if (text.isDone()) {
                damageTexts.removeIndex(i);
            }
        }
    }

    public void addDamageText(String text, float x, float y, Color color) {
        damageTexts.add(new DamageText(text, x, y, color));
    }

    public void addAttackEffect(Vector2 origin, Vector2 direction) {
        attackEffects.add(new AttackEffect(origin, direction));
    }

    public void addParticleBurst(float x, float y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(34f, 112f);
            float px = x + MathUtils.random(-4f, 4f);
            float py = y + MathUtils.random(-4f, 4f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            particles.add(new Particle(px, py, vx, vy, MathUtils.random(1.8f, 3.8f),
                    MathUtils.random(0.24f, 0.52f), color));
        }
    }

    private void updateAttackEffects(float delta) {
        for (int i = attackEffects.size - 1; i >= 0; i--) {
            AttackEffect effect = attackEffects.get(i);
            effect.update(delta);
            if (effect.isDone()) {
                attackEffects.removeIndex(i);
            }
        }
    }

    private void updateParticles(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle particle = particles.get(i);
            particle.update(delta);
            if (particle.isDone()) {
                particles.removeIndex(i);
            }
        }
    }

    private void updateVoidFalls() {
        if (!map.fallingVoid) {
            return;
        }
        if (!player.isDead() && isInVoid(player)) {
            player.takeDamage(player.maxHp + player.hp);
            player.invincibleTimer = 0f;
            addDamageText("坠落", player.getCenter().x - 10f, player.getCenter().y + 22f, Color.SCARLET);
            addParticleBurst(player.getCenter().x, player.getCenter().y, Color.SCARLET, 24);
            shake(8f, 0.18f);
            if (sfx != null) {
                sfx.death();
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy.isDead() || !isInVoid(enemy)) {
                continue;
            }
            enemy.takeDamage(enemy.maxHp + enemy.hp);
            if (!enemy.rewardGranted) {
                enemy.rewardGranted = true;
                voidKills++;
                kills++;
                levelSystem.gainExp(player, enemy.expReward);
                if (enemy.storyBossKind != StoryBossKind.NONE) {
                    items.add(new Item(ItemType.KEY, player.getCenter().x, player.getCenter().y, 1));
                    addDamageText("记忆", player.getCenter().x - 18f, player.getCenter().y + 26f, Color.WHITE);
                } else {
                    dropKnockoffReward();
                }
            }
            addDamageText("击落", enemy.getCenter().x - 10f, enemy.getCenter().y + 18f, Color.CYAN);
            addParticleBurst(enemy.getCenter().x, enemy.getCenter().y, Color.CYAN, 22);
            shake(3.5f, 0.12f);
            if (sfx != null) {
                sfx.death();
            }
        }
    }

    private void dropKnockoffReward() {
        float x = player.getCenter().x + MathUtils.random(-18f, 18f);
        float y = player.getCenter().y + MathUtils.random(-12f, 18f);
        if (floor == 1 && kills == 2) {
            items.add(new Item(lootSystem.randomRelic(), x, y));
            addDamageText("残留", x - 12f, y + 22f, Color.WHITE);
            return;
        }
        if (MathUtils.randomBoolean(0.14f)) {
            items.add(new Item(lootSystem.randomRelic(), x, y));
        } else if (MathUtils.randomBoolean(0.55f)) {
            items.add(new Item(ItemType.COIN, x, y, MathUtils.random(1, 2 + floor)));
        }
    }

    private boolean isInVoid(Entity entity) {
        Vector2 center = entity.getCenter();
        return !map.isWalkableWorld(center.x, center.y);
    }

    private void updateCameraShake(float delta) {
        if (shakeTimer <= 0f) {
            cameraShake.setZero();
            shakeMagnitude = 0f;
            return;
        }

        shakeTimer = Math.max(0f, shakeTimer - delta);
        float strength = shakeMagnitude * (shakeTimer / shakeDuration);
        cameraShake.set(MathUtils.random(-strength, strength), MathUtils.random(-strength, strength));
    }

    private boolean updateHitStop(float delta) {
        if (hitStopTimer <= 0f) {
            return false;
        }
        hitStopTimer = Math.max(0f, hitStopTimer - delta);
        return true;
    }

    private final Rectangle moveCheckBounds = new Rectangle();

    private boolean canMovePastChests(Entity entity, float dx, float dy) {
        Rectangle bounds = entity.getBounds();
        moveCheckBounds.set(bounds.x + dx, bounds.y + dy, bounds.width, bounds.height);
        for (Chest chest : chests) {
            if (!chest.blocksMovement()) {
                continue;
            }
            if (moveCheckBounds.overlaps(chest.getBounds())) {
                return false;
            }
        }
        return true;
    }
}
