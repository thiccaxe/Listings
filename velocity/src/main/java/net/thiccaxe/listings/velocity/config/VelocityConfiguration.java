package net.thiccaxe.listings.velocity.config;

import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.config.Configuration;

import java.io.File;

public class VelocityConfiguration extends Configuration {



    public VelocityConfiguration(File dataFolder, ListingsPlugin plugin) {
        super(dataFolder, plugin);

    }
    protected String getFileName() {return "config.conf";}



}
