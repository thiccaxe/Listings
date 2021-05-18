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
import com.velocitypowered.api.event.query.ProxyQueryEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.thiccaxe.listings.Formatter;
import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.config.ServerType;
import net.thiccaxe.listings.velocity.config.VelocityConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin(id="listings", name="Listings", version="1.1.0", authors = {"thiccaxe"})
public class ListingsVelocity implements ListingsPlugin, SimpleCommand {
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
        List<String> response = new ArrayList<>();
        if (configuration.getServerType() == ServerType.SERVER) {
            @Unmodifiable Collection<Player> players = Collections.unmodifiableCollection(proxy.getAllPlayers());
            if (configuration.getHeader() != null) {
                response.add("[HEADER]");
                response.add(fullParse(null, configuration.getHeader()));
            }
            if (configuration.getFooter() != null) {
                response.add("[FOOTER]");
                response.add(fullParse(null, configuration.getFooter()));
            }
            if (configuration.getExtra() != null) {
                response.add("[EXTRA]");
                response.add(fullParse(null, configuration.getExtra()));
            }
            if (configuration.getInfo().size() > 0) {
                response.add("[HEADER]");
                response.add(String.valueOf(configuration.getInfo().size()));
                response.addAll(configuration.getInfo().stream().map(info -> fullParse(null, info)).collect(Collectors.toList()));
            }
            if (players.size() > 0) {
                response.add("[PLAYERS]");
                players.forEach(player -> response.add(fullParse(player, configuration.getFormat())));
            }

        } else { // ServerType.PROXY
            List<BackendInfo> backends = pingBackends(configuration.getServers());

            List<List<String>> columns = new ArrayList<>();

            backends.forEach(backend -> {
                columns.add(Formatter.format(
                        backend.getPlayers(),
                        backend.getInfo(),
                        backend.getHeader(),
                        backend.getFooter(),
                        backend.getExtra(),
                        configuration.getMaximumColumns(),
                        configuration.getMaximumRows(),
                        configuration.getJustifyType()
                ));
            });


            List<String> backendHover = Formatter.joinColumns(columns);

            response.addAll(Formatter.format(
                    new ArrayList<>(backendHover),
                    configuration.getInfo(),
                    configuration.getHeader(),
                    configuration.getFooter(),
                    configuration.getExtra(),
                    configuration.getMaximumColumns(),
                    configuration.getMaximumRows(),
                    configuration.getJustifyType()
            ));

        }
        return response.stream().map(string -> new ServerPing.SamplePlayer(string, UUID.randomUUID())).collect(Collectors.toList());

    }

    private List<BackendInfo> pingBackends(Set<String> servers) {
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
                        backends.add(getData(players.getSample().stream().map(ServerPing.SamplePlayer::getName).collect(Collectors.toList())));
                    });
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
        /*
        backends.forEach(backend -> {
            log(backend.getHeader());
            log(backend.getFooter());
            log(backend.getExtra());
            log(String.join(", ", backend.getInfo()));
            log(String.join(", ", backend.getPlayers()));
        });
         */
        return backends;
    }

    private BackendInfo getData(List<String> sample) {
        //"[HEADER]", "[FOOTER]", "[EXTRA]", "[INFO]", "[PLAYERS]");
        String current = "";
        int length = 0;
        int consumed = 0;
        BackendInfo backend = new BackendInfo();
        for (String line : sample) {
            //System.out.println("            " + line);


            if (consumed == length) {
                //System.out.println("Getting new Header...");
                if (line.equalsIgnoreCase("[HEADER]")) {
                    current = "[HEADER]";
                    length = 1;
                } else if (line.equalsIgnoreCase("[FOOTER]")) {
                    current = "[FOOTER]";
                    length = 1;
                } else if (line.equalsIgnoreCase("[EXTRA]")) {
                    current = "[EXTRA]";
                    length = 1;
                } else if (line.equalsIgnoreCase("[INFO]")) {
                    current = "[INFO]";
                    length = Integer.MAX_VALUE;
                } else if (line.equalsIgnoreCase("[PLAYERS]")) {
                    current = "[PLAYERS]";
                    length = Integer.MAX_VALUE;
                }
                //System.out.println("Current: " + current);
                consumed = 0;
            } else if (consumed == 0 && length == Integer.MAX_VALUE) {
                //System.out.println("Getting length value for " + current + "...");
                try {
                    length = Integer.parseInt(line) + 1;
                    consumed++;
                } catch (NumberFormatException ignored) {
                    current = "";
                    consumed = length;
                }
            } else if (consumed < length) {
                consumed ++;
                if (current.equalsIgnoreCase("[HEADER]")) {
                    backend.setHeader(line);
                } else if (current.equalsIgnoreCase("[FOOTER]")) {
                    backend.setFooter(line);
                } else if (current.equalsIgnoreCase("[EXTRA]")) {
                    backend.setExtra(line);
                } else if (current.equalsIgnoreCase("[INFO]")) {
                    backend.addInfo(line);
                } else if (current.equalsIgnoreCase("[PLAYERS]")) {
                    backend.addPlayer(line);
                }
            }

        }
        return backend;
    }



    private String fullParse(Player player, String string) {
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



    @Override
    public void execute(final Invocation invocation) {
        configuration.reload();
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("listings.reload") || invocation.source() instanceof ConsoleCommandSource;
    }

    public void log(String message) {
        logger.info(message);
    }

    @Override
    public boolean cached() {
        return (System.currentTimeMillis() - lastCacheUpdate < (configuration.getCacheTimeout()*1000));
    }

    @Override
    public void setCache() {
        lastCacheUpdate = System.currentTimeMillis();
    }

    public static class BackendInfo {
        private @Nullable String header;
        private @Nullable String footer;
        private @Nullable String extra;
        private @NotNull
        final List<String> players = new ArrayList<>();
        private @NotNull
        final List<String> infos = new ArrayList<>();

        public @Nullable String getHeader() {
            return header;
        }

        public void setHeader(@Nullable String header) {
            this.header = header;
        }

        public @Nullable String getFooter() {
            return footer;
        }

        public void setFooter(@Nullable String footer) {
            this.footer = footer;
        }

        public @Nullable String getExtra() {
            return extra;
        }

        public void setExtra(@Nullable String extra) {
            this.extra = extra;
        }

        public @NotNull List<String> getPlayers() {
            return players;
        }

        public void addPlayer(@NotNull String player) {
            players.add(player);
        }

        public @NotNull List<String> getInfo() {
            return infos;
        }

        public void addInfo(@NotNull String info) {
            infos.add(info);
        }

    }
}
