package com.kayro.dungeon.entity;

public enum ShopItem {
    POTION("修补", "回血", 40),
    ATK_BOOST("准星", "伤害+", 80),
    DEF_BOOST("外壳", "防御+", 65),
    KEY("碎片", "开裂隙", 55),
    MAX_HP("身体", "生命+", 90);

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
