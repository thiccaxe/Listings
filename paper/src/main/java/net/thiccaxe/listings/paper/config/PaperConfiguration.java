package net.thiccaxe.listings.paper.config;

import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.config.Configuration;
import net.thiccaxe.listings.config.ServerType;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;

public class PaperConfiguration extends Configuration {
    private boolean vanish = true;
    private boolean query = false;

    public PaperConfiguration(File dataFolder, ListingsPlugin plugin, ServerType serverType) {
        super(dataFolder, plugin, serverType);
    }

    @Override
    protected void updateOptions() {
        super.updateOptions();
        try {
            if (root !=  null) {
                ConfigurationNode config = root.node("config");
                vanish = config.node("vanish").node("enabled").getBoolean(true);
                query = config.node("vanish").node("query").getBoolean(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean vanishEnabled() {
        return vanish;
    }

    public boolean queryEnabled() {
        return query;
    }
}
