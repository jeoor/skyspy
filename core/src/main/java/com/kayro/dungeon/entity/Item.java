package com.kayro.dungeon.entity;

import com.kayro.dungeon.world.GameWorld;

public class Item extends Entity {
    public final ItemType type;
    public final int amount;
    public RelicType relicType;
    public WeaponType weaponType;

    public Item(ItemType type, float centerX, float centerY, int amount) {
        super(centerX - 10f, centerY - 10f, 20f, 20f);
        this.type = type;
        this.amount = amount;
    }

    public Item(RelicType relicType, float centerX, float centerY) {
        this(ItemType.RELIC, centerX, centerY, 1);
        this.relicType = relicType;
    }

    public Item(WeaponType weaponType, float centerX, float centerY) {
        this(ItemType.WEAPON, centerX, centerY, 1);
        this.weaponType = weaponType;
    }

    @Override
    public void update(float delta, GameWorld world) {
    }

    public void apply(Player player) {
        switch (type) {
            case COIN:
                player.gold += amount;
                break;
            case POTION:
                player.potions += amount;
                break;
            case SWORD_UPGRADE:
                player.attack += amount;
                break;
            case ARMOR_UPGRADE:
                player.defense += amount;
                break;
            case KEY:
                player.keys += amount;
                break;
            case RELIC:
                if (relicType != null) {
                    player.addRelic(relicType);
                }
                break;
            case WEAPON:
                if (weaponType != null) {
                    player.equipWeapon(weaponType);
                }
                break;
            default:
                break;
        }
    }
}
