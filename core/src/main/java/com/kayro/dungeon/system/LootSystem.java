package com.kayro.dungeon.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.kayro.dungeon.entity.Enemy;
import com.kayro.dungeon.entity.Item;
import com.kayro.dungeon.entity.ItemType;
import com.kayro.dungeon.entity.RelicType;
import com.kayro.dungeon.entity.WeaponType;
import com.kayro.dungeon.world.GameWorld;

public class LootSystem {
    public void dropForEnemy(GameWorld world, Enemy enemy) {
        float roll = MathUtils.random();
        float x = enemy.getCenter().x;
        float y = enemy.getCenter().y;

        if (roll < 0.64f) {
            world.items.add(new Item(ItemType.COIN, x, y, MathUtils.random(1, 4 + world.floor)));
        } else if (roll < 0.80f) {
            world.items.add(new Item(ItemType.POTION, x, y, 1));
        } else if (roll < 0.87f) {
            world.items.add(new Item(ItemType.SWORD_UPGRADE, x, y, 1));
        } else if (roll < 0.94f) {
            world.items.add(new Item(ItemType.ARMOR_UPGRADE, x, y, 1));
        } else if (roll < 0.975f) {
            world.items.add(new Item(ItemType.KEY, x, y, 1));
        } else if (roll < 0.992f) {
            world.items.add(new Item(randomRelic(), x, y));
        } else {
            world.items.add(new Item(randomWeapon(), x, y));
        }
    }

    public void addPickupText(GameWorld world, Item item) {
        switch (item.type) {
            case COIN:
                world.addDamageText("+" + item.amount + "g", item.getCenter().x, item.getCenter().y, Color.GOLD);
                break;
            case POTION:
                world.addDamageText("+Potion", item.getCenter().x, item.getCenter().y, Color.SCARLET);
                break;
            case SWORD_UPGRADE:
                world.addDamageText("+ATK", item.getCenter().x, item.getCenter().y, Color.CYAN);
                break;
            case ARMOR_UPGRADE:
                world.addDamageText("+DEF", item.getCenter().x, item.getCenter().y, Color.LIGHT_GRAY);
                break;
            case KEY:
                world.addDamageText("+Key", item.getCenter().x, item.getCenter().y, Color.GOLD);
                break;
            case RELIC:
                String label = item.relicType == null ? "Relic" : item.relicType.label;
                world.addDamageText("+" + label, item.getCenter().x - 12f, item.getCenter().y, Color.VIOLET);
                break;
            case WEAPON:
                String weapon = item.weaponType == null ? "Weapon" : item.weaponType.label;
                world.addDamageText("+" + weapon, item.getCenter().x - 12f, item.getCenter().y, Color.GOLD);
                break;
            default:
                break;
        }
    }

    public RelicType randomRelic() {
        RelicType[] values = RelicType.values();
        return values[MathUtils.random(values.length - 1)];
    }

    public WeaponType randomWeapon() {
        WeaponType[] values = WeaponType.values();
        return values[MathUtils.random(values.length - 1)];
    }
}
