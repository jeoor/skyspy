package com.kayro.dungeon.entity;

public enum ShopItem {
    POTION("Potion", "Restore 40 HP", 40),
    ATK_BOOST("+3 Attack", "Permanent ATK up", 80),
    DEF_BOOST("+2 Defense", "Permanent DEF up", 65),
    KEY("Key", "Opens stairs", 55),
    MAX_HP("+20 Max HP", "Permanent HP up", 90);

    public final String label;
    public final String description;
    public final int basePrice;

    ShopItem(String label, String description, int basePrice) {
        this.label = label;
        this.description = description;
        this.basePrice = basePrice;
    }

    public int price(int floor) {
        return basePrice + floor * 8;
    }

    public boolean apply(Player player) {
        switch (this) {
            case POTION:
                player.potions++;
                return true;
            case ATK_BOOST:
                player.attack += 3;
                return true;
            case DEF_BOOST:
                player.defense += 2;
                return true;
            case KEY:
                player.keys++;
                return true;
            case MAX_HP:
                player.maxHp += 20;
                player.hp = Math.min(player.hp + 20, player.maxHp);
                return true;
            default:
                return false;
        }
    }
}
