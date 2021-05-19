/*
 * This file is part of Listings, licensed under the MIT License.
 *
 * Copyright (c) 2021 thiccaxe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.thiccaxe.listings.velocity;


import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.config.Configuration;
import net.thiccaxe.listings.config.ServerType;
import net.thiccaxe.listings.velocity.config.VelocityConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin(id="listings", name="Listings", version="1.1.0", authors = {"thiccaxe"})
public class ListingsVelocity implements ListingsPlugin<ServerPing.SamplePlayer, Player>, SimpleCommand {
    private final ProxyServer proxy;
    private static Logger logger;
    private final File dataFolder;
    private final VelocityConfiguration configuration;

    private long lastCacheUpdate = 0;

    private List<ServerPing.SamplePlayer> cache = Collections.emptyList();


    @Inject
    public ListingsVelocity(ProxyServer proxy, Logger l, @DataDirectory Path dataFolder) {
        this.proxy = proxy;
        logger = l;
        this.dataFolder = dataFolder.toFile();
        configuration = new VelocityConfiguration(this.dataFolder, this, ServerType.PROXY);
        configuration.setUpFile();

    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        proxy.getCommandManager().register(proxy.getCommandManager().metaBuilder("listings").build(), this);
    }

    public void log(String message) {
        logger.info(message);
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public boolean cached() {
        return (System.currentTimeMillis() - lastCacheUpdate < (configuration.getCacheTimeout()*1000));
    }

    @Override
    public void setCache() {
        lastCacheUpdate = System.currentTimeMillis();
    }

    @Override
    public ServerPing.SamplePlayer fromString(String name) {
        return new ServerPing.SamplePlayer(name, UUID.randomUUID());
    }


    @Override
    public String fullParse(Player player, String string) {
        if (player != null) {
            return LegacyComponentSerializer.legacySection().serialize(
                    MiniMessage.get().parse(
                            string, "player", player.getUsername(),
                            "online", String.valueOf(proxy.getAllPlayers().size())
                    )
            );
        }
        else {
            return LegacyComponentSerializer.legacySection().serialize(
                    MiniMessage.get().parse(
                            string,
                            "online", String.valueOf(proxy.getAllPlayers().size())
                    )
            );
        }
    }



    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing.Builder ping = event.getPing().asBuilder();
        ping.clearSamplePlayers();

        if (!cached()) {
            setCache();
            cache = handlePing(event);
        }
        cache.forEach(ping::samplePlayers);

        event.setPing(ping.build());
    }

    private List<ServerPing.SamplePlayer> handlePing(ProxyPingEvent event) {
        return handleProxyServerPing(() -> new ArrayList<>(proxy.getAllPlayers()));

    }
    @Override
    public List<BackendInfo> pingBackends() {
        Set<String> servers = configuration.getServers();
        List<BackendInfo> backends = new ArrayList<>();
        //log("Pinging Backends ...");
        servers.forEach(server -> {
            //log("Pinging server:" + server);
            Optional<RegisteredServer> optionalBackend =  proxy.getServer(server);
            optionalBackend.ifPresent(backend -> {
                //log("Found server: " + backend.getServerInfo().getName() + ", /" + backend.getServerInfo().getAddress().getAddress().getHostAddress());
                try {
                    ServerPing ping = backend.ping().get();
                    Optional<ServerPing.Players> optionalPlayers = ping.getPlayers();
                    //log("Pinged server...");
                    optionalPlayers.ifPresent(players -> {
                        //log("Found Players: " + players.getSample().stream().map(ServerPing.SamplePlayer::getName).collect(Collectors.joining(", ")));
                        backends.add(BackendInfo.getData(players.getSample().stream().map(ServerPing.SamplePlayer::getName).collect(Collectors.toList())));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
        return backends;
    }






    @Override
    public void execute(final Invocation invocation) {
        Audience sender = invocation.source();
        try {
            if (invocation.source().hasPermission("listings.reload") || invocation.source().equals(proxy.getConsoleCommandSource())) {
                configuration.reload();
                sender.sendMessage(MiniMessage.get().parse(configuration.getReloadedMessage()));
            } else {
                sender.sendMessage(MiniMessage.get().parse(configuration.getErrorMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(MiniMessage.get().parse(configuration.getErrorMessage()));
        }
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("listings.reload") || invocation.source() instanceof ConsoleCommandSource;
    }


}
