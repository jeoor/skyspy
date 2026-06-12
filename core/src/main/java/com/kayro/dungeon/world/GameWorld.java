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
import com.kayro.dungeon.entity.Player;
import com.kayro.dungeon.entity.PropType;
import com.kayro.dungeon.entity.Projectile;
import com.kayro.dungeon.entity.RelicType;
import com.kayro.dungeon.entity.Trap;
import com.kayro.dungeon.entity.WeaponType;
import com.kayro.dungeon.system.AISystem;
import com.kayro.dungeon.system.CombatSystem;
import com.kayro.dungeon.system.CollisionSystem;
import com.kayro.dungeon.system.FogOfWarSystem;
import com.kayro.dungeon.system.InputHandler;
import com.kayro.dungeon.system.LevelSystem;
import com.kayro.dungeon.system.LootSystem;
import com.kayro.dungeon.system.SpawnerSystem;
import com.kayro.dungeon.util.Constants;

public class GameWorld {
    public int floor = 1;
    public int kills = 0;
    public boolean gameOver;
    public BiomeType biome = BiomeType.CATACOMBS;
    public DungeonMap map;
    public Player player;
    public final Array<Enemy> enemies = new Array<>();
    public final Array<DecorProp> props = new Array<>();
    public final Array<Chest> chests = new Array<>();
    public final Array<Item> items = new Array<>();
    public final Array<Trap> traps = new Array<>();
    public final Array<Projectile> projectiles = new Array<>();
    public final Array<DamageText> damageTexts = new Array<>();
    public final Array<AttackEffect> attackEffects = new Array<>();
    public final Vector2 cameraShake = new Vector2();
    public Sfx sfx;
    public final InputHandler input = new InputHandler();
    public final LevelSystem levelSystem = new LevelSystem();
    public final LootSystem lootSystem = new LootSystem();
    public final CombatSystem combatSystem = new CombatSystem();
    public CollisionSystem collision;
    private final DungeonGenerator generator = new DungeonGenerator();
    private final FogOfWarSystem fog = new FogOfWarSystem();
    private final AISystem aiSystem = new AISystem();
    private final SpawnerSystem spawnerSystem = new SpawnerSystem();
    private final WeaponType startingWeapon;
    private float hitStopTimer;
    private float shakeTimer;
    private float shakeDuration = 1f;
    private float shakeMagnitude;

    public GameWorld() {
        this(null);
    }

    public GameWorld(Sfx sfx) {
        this(sfx, WeaponType.SWORD);
    }

    public GameWorld(Sfx sfx, WeaponType startingWeapon) {
        this.sfx = sfx;
        this.startingWeapon = startingWeapon == null ? WeaponType.SWORD : startingWeapon;
        newRun();
    }

    public void newRun() {
        floor = 1;
        kills = 0;
        gameOver = false;
        loadFloor();
    }

    public void nextFloor() {
        floor++;
        int hp = player.hp;
        int maxHp = player.maxHp;
        int attack = player.attack;
        int defense = player.defense;
        int level = player.level;
        int exp = player.exp;
        int gold = player.gold;
        int keys = player.keys;
        int potions = player.potions;
        WeaponType weapon = player.weapon;
        Array<RelicType> relics = new Array<>();
        relics.addAll(player.relics);
        loadFloor();
        player.hp = hp;
        player.maxHp = maxHp;
        player.attack = attack;
        player.defense = defense;
        player.level = level;
        player.exp = exp;
        player.gold = gold;
        player.keys = keys;
        player.potions = potions;
        player.equipWeapon(weapon);
        for (RelicType relic : relics) {
            player.addRelic(relic);
        }
    }

    public void update(float delta) {
        input.update();
        updateCameraShake(delta);
        if (updateHitStop(delta)) {
            return;
        }
        if (player.isDead()) {
            player.update(delta, this);
            updateDamageTexts(delta);
            updateAttackEffects(delta);
            fog.update(map, player);
            if (player.isDeathAnimationDone()) {
                gameOver = true;
            }
            return;
        }

        if (input.interactPressed && player.getCenter().dst(map.stairsPosition) < 40f) {
            if (hasLiveBoss()) {
                addDamageText("Defeat Boss", player.getCenter().x - 16f, player.getCenter().y + 28f, Color.SCARLET);
                shake(3f, 0.10f);
                return;
            }
            if (player.keys <= 0) {
                addDamageText("Find Key", player.getCenter().x - 10f, player.getCenter().y + 28f, Color.GOLD);
                shake(2f, 0.08f);
                return;
            }
            player.keys--;
            if (sfx != null) {
                sfx.stairs();
            }
            nextFloor();
        } else {
            player.update(delta, this);
            updateTraps(delta);
            updateChests(delta);
            if (player.isDead()) {
                updateDamageTexts(delta);
                updateAttackEffects(delta);
                fog.update(map, player);
                return;
            }
            aiSystem.update(this, delta);
            combatSystem.update(this, delta);
            if (!player.isDead()) {
                spawnerSystem.update(this, delta);
                openChests();
                pickupItems();
            }
            updateDamageTexts(delta);
            updateAttackEffects(delta);
            fog.update(map, player);
        }

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

    public void moveEntity(Entity entity, float dx, float dy) {
        if (dx != 0f && canMovePastChests(entity, dx, 0f)) {
            entity.position.x += dx;
        }
        if (dy != 0f && canMovePastChests(entity, 0f, dy)) {
            entity.position.y += dy;
        }
    }

    private void loadFloor() {
        biome = BiomeType.forFloor(floor);
        map = generator.generate();
        player = new Player(map.playerSpawn.x, map.playerSpawn.y);
        player.equipWeapon(startingWeapon);
        collision = new CollisionSystem(map);
        enemies.clear();
        props.clear();
        chests.clear();
        items.clear();
        traps.clear();
        projectiles.clear();
        damageTexts.clear();
        attackEffects.clear();
        cameraShake.setZero();
        hitStopTimer = 0f;
        shakeTimer = 0f;
        shakeMagnitude = 0f;
        fog.update(map, player);
        seedProps();
        spawnerSystem.spawnInitial(this);
        seedItems();
        seedChests();
        seedTraps();
        seedBossRoom();
    }

    private void seedItems() {
        int count = MathUtils.random(5, 8);
        for (int i = 0; i < count; i++) {
            for (int attempt = 0; attempt < 60; attempt++) {
                int x = MathUtils.random(2, Constants.MAP_WIDTH - 3);
                int y = MathUtils.random(2, Constants.MAP_HEIGHT - 3);
                Tile tile = map.getTile(x, y);
                if (!tile.isWalkable() || tile.visible) {
                    continue;
                }
                float worldX = (x + 0.5f) * Constants.TILE_SIZE;
                float worldY = (y + 0.5f) * Constants.TILE_SIZE;
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
        for (Room room : map.rooms) {
            if (player.getCenter().dst(room.centerWorld(Constants.TILE_SIZE)) < Constants.TILE_SIZE * 3f) {
                continue;
            }
            int propCount = MathUtils.random(1, Math.max(1, Math.min(4, room.width * room.height / 24)));
            for (int i = 0; i < propCount; i++) {
                int tileX = MathUtils.random(room.x + 1, room.x + room.width - 2);
                int tileY = MathUtils.random(room.y + 1, room.y + room.height - 2);
                if (!map.isWalkableTile(tileX, tileY)) {
                    continue;
                }
                PropType type = randomPropType(room, tileX, tileY);
                props.add(new DecorProp(type, (tileX + 0.5f) * Constants.TILE_SIZE,
                        (tileY + 0.5f) * Constants.TILE_SIZE));
            }
        }
    }

    private PropType randomPropType(Room room, int tileX, int tileY) {
        boolean nearWall = tileX <= room.x + 1 || tileX >= room.x + room.width - 2
                || tileY <= room.y + 1 || tileY >= room.y + room.height - 2;
        float roll = MathUtils.random();
        if (nearWall && roll < 0.35f) {
            return PropType.TORCH;
        }
        if (roll < 0.62f) {
            return MathUtils.randomBoolean() ? PropType.BARREL : PropType.CRATE;
        }
        return PropType.RUBBLE;
    }

    private void seedTraps() {
        int count = MathUtils.random(5, 7 + Math.min(5, floor)) + biome.extraTraps;
        for (int i = 0; i < count; i++) {
            Vector2 position = randomFloorPosition(Constants.TILE_SIZE * 5f, 80);
            if (position != null) {
                traps.add(new Trap(position.x, position.y));
            }
        }
    }

    private void seedBossRoom() {
        if (floor < 3 || floor % 3 != 0 || map.bossRoom == null) {
            return;
        }
        Vector2 bossPosition = roomPosition(map.bossRoom, 1, 1);
        enemies.add(new Enemy(EnemyType.BOSS, bossPosition.x, bossPosition.y, floor, biome));
        Vector2 chestPosition = roomPosition(map.bossRoom, -1, -1);
        chests.add(new Chest(chestPosition.x, chestPosition.y, true));
        addDamageText("Boss Floor", player.getCenter().x - 18f, player.getCenter().y + 30f, Color.SCARLET);
    }

    private Vector2 randomFloorPosition(float minPlayerDistance, int attempts) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = MathUtils.random(2, Constants.MAP_WIDTH - 3);
            int y = MathUtils.random(2, Constants.MAP_HEIGHT - 3);
            Tile tile = map.getTile(x, y);
            if (!tile.isWalkable() || tile.visible) {
                continue;
            }
            float worldX = (x + 0.5f) * Constants.TILE_SIZE;
            float worldY = (y + 0.5f) * Constants.TILE_SIZE;
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
        return new Vector2((tileX + 0.5f) * Constants.TILE_SIZE, (tileY + 0.5f) * Constants.TILE_SIZE);
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
        return player.keys > 0 && !hasLiveBoss();
    }

    private void updateTraps(float delta) {
        for (Trap trap : traps) {
            trap.update(delta, this);
            if (!trap.isReady() || player.invincibleTimer > 0f || !trap.getBounds().overlaps(player.getBounds())) {
                continue;
            }
            int damage = Math.max(4, 10 + floor * 2 + biome.trapDamageBonus - player.defense);
            trap.trigger();
            player.takeDamage(damage);
            player.invincibleTimer = Math.max(player.invincibleTimer, 0.25f);
            addDamageText("-" + damage, player.getCenter().x, player.getCenter().y + 22f, Color.ORANGE);
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

    private void openChests() {
        if (!input.interactPressed) {
            return;
        }
        for (Chest chest : chests) {
            if (chest.opened || chest.getCenter().dst(player.getCenter()) > 48f) {
                continue;
            }
            if (chest.bossChest && hasLiveBoss()) {
                addDamageText("Boss Locked", chest.getCenter().x - 18f, chest.getCenter().y + 22f, Color.SCARLET);
                return;
            }
            chest.open();
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
        if (chest.bossChest || player.keys == 0 || MathUtils.randomBoolean(0.25f)) {
            items.add(new Item(ItemType.KEY, x, y - 16f, 1));
        }
        if (chest.bossChest || MathUtils.randomBoolean(0.35f)) {
            ItemType upgrade = MathUtils.randomBoolean() ? ItemType.SWORD_UPGRADE : ItemType.ARMOR_UPGRADE;
            items.add(new Item(upgrade, x, y + 16f, 1));
        }
        if (chest.bossChest || MathUtils.randomBoolean(0.28f)) {
            items.add(new Item(lootSystem.randomRelic(), x, y + 32f));
        }
        if (chest.bossChest || MathUtils.randomBoolean(0.18f)) {
            items.add(new Item(lootSystem.randomWeapon(), x + 18f, y + 32f));
        }
        addDamageText(chest.bossChest ? "Boss Loot" : "Chest", x - 12f, y + 24f, Color.GOLD);
    }

    private void pickupItems() {
        for (int i = items.size - 1; i >= 0; i--) {
            Item item = items.get(i);
            float distance = item.getCenter().dst(player.getCenter());
            if (distance <= 30f || (input.interactPressed && distance <= 48f)) {
                item.apply(player);
                lootSystem.addPickupText(this, item);
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

    private void updateAttackEffects(float delta) {
        for (int i = attackEffects.size - 1; i >= 0; i--) {
            AttackEffect effect = attackEffects.get(i);
            effect.update(delta);
            if (effect.isDone()) {
                attackEffects.removeIndex(i);
            }
        }
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

    private boolean canMovePastChests(Entity entity, float dx, float dy) {
        if (!collision.canMove(entity, dx, dy)) {
            return false;
        }
        Rectangle next = entity.getBounds();
        next.x += dx;
        next.y += dy;
        for (Chest chest : chests) {
            if (!chest.blocksMovement()) {
                continue;
            }
            if (next.overlaps(chest.getBounds())) {
                return false;
            }
        }
        return true;
    }
}
