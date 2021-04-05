package net.thiccaxe.listings;

public interface ListingsPlugin {

    default void log(String message) {
        System.out.println("[Listings] " +  message);
    }
}
