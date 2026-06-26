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

        if (roll < 0.50f) {
            world.items.add(new Item(ItemType.COIN, x, y, MathUtils.random(1, 4 + world.floor)));
        } else if (roll < 0.67f) {
            world.items.add(new Item(ItemType.POTION, x, y, 1));
        } else if (roll < 0.78f) {
            world.items.add(new Item(ItemType.SWORD_UPGRADE, x, y, 1));
        } else if (roll < 0.86f) {
            world.items.add(new Item(ItemType.ARMOR_UPGRADE, x, y, 1));
        } else if (roll < 0.93f) {
            world.items.add(new Item(ItemType.KEY, x, y, 1));
        } else if (roll < 0.985f) {
            world.items.add(new Item(randomRelic(), x, y));
        } else {
            world.items.add(new Item(randomWeapon(), x, y));
        }
    }

    public void addPickupText(GameWorld world, Item item) {
        switch (item.type) {
            case COIN:
                world.addDamageText("+" + item.amount + "回响", item.getCenter().x, item.getCenter().y, Color.GOLD);
                break;
            case POTION:
                world.addDamageText("+修补", item.getCenter().x, item.getCenter().y, Color.SCARLET);
                break;
            case SWORD_UPGRADE:
                world.addDamageText("+准星", item.getCenter().x, item.getCenter().y, Color.CYAN);
                break;
            case ARMOR_UPGRADE:
                world.addDamageText("+外壳", item.getCenter().x, item.getCenter().y, Color.LIGHT_GRAY);
                break;
            case KEY:
                world.addDamageText("+碎片", item.getCenter().x, item.getCenter().y, Color.GOLD);
                break;
            case RELIC:
                String label = item.relicType == null ? "残留" : item.relicType.label;
                world.addDamageText("+" + label, item.getCenter().x - 12f, item.getCenter().y, Color.VIOLET);
                break;
            case WEAPON:
                String weapon = item.weaponType == null ? "射击" : item.weaponType.label;
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
