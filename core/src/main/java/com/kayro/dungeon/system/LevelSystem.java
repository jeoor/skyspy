package com.kayro.dungeon.system;

import com.kayro.dungeon.entity.Player;

public class LevelSystem {
    public int expToNext(Player player) {
        return 50 + player.level * 35;
    }

    public void gainExp(Player player, int amount) {
        player.exp += amount;
        while (player.exp >= expToNext(player)) {
            player.exp -= expToNext(player);
            player.level++;
            player.maxHp += 15;
            player.attack += 4;
            player.defense += 1;
            player.hp = player.maxHp;
        }
    }
}
