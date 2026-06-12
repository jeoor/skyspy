package com.kayro.dungeon.entity;

public enum RelicType {
    CRIT_CHARM("Crit Charm"),
    VAMPIRE_FANG("Vampire Fang"),
    PIERCING_QUIVER("Piercing Quiver"),
    IRON_GRIP("Iron Grip"),
    LAST_STAND("Last Stand");

    public final String label;

    RelicType(String label) {
        this.label = label;
    }
}
