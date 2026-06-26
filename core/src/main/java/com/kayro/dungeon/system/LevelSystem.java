package com.kayro.dungeon.system;

import com.kayro.dungeon.entity.Player;

public class LevelSystem {
    public int expToNext(Player player) {
        return 50 + player.level * 35;
    }

    public int gainExp(Player player, int amount) {
        int levelsGained = 0;
        player.exp += amount;
        while (player.exp >= expToNext(player)) {
            player.exp -= expToNext(player);
            player.level++;
            levelsGained++;
            player.maxHp += 15;
            player.attack += 4;
            player.defense += 1;
            player.hp = player.maxHp;
        }
        return levelsGained;
    }
}
