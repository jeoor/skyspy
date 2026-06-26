package com.kayro.dungeon.entity;

public enum RelicType {
    CRIT_CHARM("不出声"),
    VAMPIRE_FANG("抱紧猫"),
    PIERCING_QUIVER("穿过习惯"),
    IRON_GRIP("抓紧"),
    LAST_STAND("最后的我"),
    ECHO_LENS("回声瞳"),
    WHITE_SPLINTER("裂白"),
    AFTERIMAGE_STEP("残影步");

    public final String label;

    RelicType(String label) {
        this.label = label;
    }
}
