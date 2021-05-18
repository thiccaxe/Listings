package net.thiccaxe.listings.config;

public enum ServerType {
    SERVER("server"),
    PROXY("proxy");

    private final String name;

    ServerType(String name) {
        this.name = name;
    }

    public static ServerType getServerType(String name) {
        for (ServerType value : ServerType.values()) {
            if (value.name.equalsIgnoreCase(name)) {
                return value;
            }
        }
        return PROXY;
    }
}
