package net.thiccaxe.listings.velocity;


import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.thiccaxe.listings.Formatter;
import net.thiccaxe.listings.JustifyType;
import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.Text;
import net.thiccaxe.listings.velocity.config.VelocityConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(id="listings", name="Listings", version="1.1.0", authors = {"thiccaxe"})
public class ListingsVelocity implements ListingsPlugin, SimpleCommand {
    private final ProxyServer server;
    private static Logger logger;
    private final File dataFolder;
    private final VelocityConfiguration configuration;

    @Inject
    public ListingsVelocity(ProxyServer server, Logger l, @DataDirectory Path dataFolder) {
        this.server = server;
        logger = l;
        this.dataFolder = dataFolder.toFile();
        configuration = new VelocityConfiguration(this.dataFolder, this);
        configuration.setUpFile();

    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        server.getCommandManager().register(server.getCommandManager().metaBuilder("listings").build(), this);
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing.Builder ping =  event.getPing().asBuilder();
        final List<Text> info = new LinkedList<>();
        info.add(new Text("Info Line 1"));
        info.add(new Text("Info Line 2"));
        info.add(new Text("etc"));

        ping.clearSamplePlayers();

        Formatter.createColumnGroup(
                Collections.emptyList(),
                info,
                new Text(configuration.getConfig().node("header").getString("Velocity Header")),
                new Text(configuration.getConfig().node("footer").getString("Velocity Footer")),
                15,
                2,
                JustifyType.getType(configuration.getConfig().node("justify").getString("left"))
        )
                .forEach(row -> ping.samplePlayers(new ServerPing.SamplePlayer(row.name(), UUID.randomUUID())));


        event.setPing(ping.build());
    }

    @Override
    public void execute(final Invocation invocation) {
        configuration.reload();
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("listings.reload");
    }

    public void log(String s) {
        logger.info(s);
    }
}
