package net.thiccaxe.listings;

public enum JustifyType {
    LEFT("left"),
    RIGHT("right"),
    CENTER("center");

    private final String type;

    JustifyType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static JustifyType getType(String type) {
        for (JustifyType justifyType : JustifyType.values()) {
            if (justifyType.type.equalsIgnoreCase(type)) {
                return justifyType;
            }
        }
        return LEFT;
    }
}
