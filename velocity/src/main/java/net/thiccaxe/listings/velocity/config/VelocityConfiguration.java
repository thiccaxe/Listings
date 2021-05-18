package net.thiccaxe.listings.velocity.config;

import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.config.Configuration;
import net.thiccaxe.listings.config.ServerType;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VelocityConfiguration extends Configuration {

    private Set<String> servers = Collections.emptySet();

    public VelocityConfiguration(File dataFolder, ListingsPlugin plugin, ServerType serverType) {
        super(dataFolder, plugin, serverType);
    }

    @Override
    protected void updateOptions() {
        super.updateOptions();
        try {
            if (root != null) {
                ConfigurationNode config = root.node("configuration");
                servers = new HashSet<>(config.node("backends").getList(String.class, Collections.emptyList()));
                //System.out.println("servers: " + String.join(", ", servers));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<String> getServers() {
        return servers;
    }
}
