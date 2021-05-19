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

package net.thiccaxe.listings.paper;

import com.destroystokyo.paper.event.server.GS4QueryEvent;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.thiccaxe.listings.Formatter;
import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.config.Configuration;
import net.thiccaxe.listings.config.ServerType;
import net.thiccaxe.listings.paper.config.PaperConfiguration;
import net.thiccaxe.listings.text.DefaultFont;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ListingsPaper extends JavaPlugin implements Listener, CommandExecutor, ListingsPlugin<PlayerProfile, Player> {

    private File dataFolder;
    private static Logger logger;
    private PaperConfiguration configuration;

    private long lastCacheUpdate = 0;

    private List<PlayerProfile> cache = Collections.emptyList();
    private ServerType lastType;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.dataFolder = getDataFolder();
            logger = getLogger();
            configuration = new PaperConfiguration(this.dataFolder, this, ServerType.SERVER);
            configuration.setUpFile();
            lastType = configuration.getServerType();
            Objects.requireNonNull(getCommand("listings")).setExecutor(this);

            getServer().getPluginManager().registerEvents(this, this);
        } else {
            getLogger().info("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }


    @EventHandler
    public void onServerListPing(PaperServerListPingEvent event) {
        event.getPlayerSample().clear();
        if (!cached()) {
            setCache();
            cache = handlePing(event);
        }
        event.getPlayerSample().addAll(cache);
    }

    @EventHandler
    public void onQuery(GS4QueryEvent event) {
        if (configuration.vanishEnabled() && configuration.queryEnabled()) {
            event.setResponse(event.getResponse().toBuilder().clearPlayers().players(Collections.unmodifiableCollection(Bukkit.getOnlinePlayers()).stream().filter(this::isNotVanished).map(HumanEntity::getName) .collect(Collectors.toList())).build());
        }
    }

    public void log(String message) {
        logger.info(message);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (sender.hasPermission("listings.reload") || sender.isOp() || sender instanceof ConsoleCommandSender) {
                configuration.reload();
                sender.sendMessage(MiniMessage.get().parse(configuration.getReloadedMessage()));

                return true;
            } else {
                sender.sendMessage(MiniMessage.get().parse(configuration.getErrorMessage()));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(MiniMessage.get().parse(configuration.getErrorMessage()));
        }
        return false;

    }

    private List<PlayerProfile> handlePing(PaperServerListPingEvent event) {
        return handleBackendServerPing(() -> new ArrayList<>(Bukkit.getServer().getOnlinePlayers())
                .stream().filter(this::isNotVanished).collect(Collectors.toList()));
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
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public PlayerProfile fromString(String name) {
        return Bukkit.createProfile(UUID.randomUUID(), name);
    }


    public String fullParse(Player player, String string) {
        return LegacyComponentSerializer.legacySection().serialize(
                MiniMessage.get().parse(
                        PlaceholderAPI.setPlaceholders(player, string)
                )
        );
    }

    private boolean isNotVanished(Player player) {
        if (!configuration.vanishEnabled()) return true; // May be vanished, but doesn't matter, return not vanished - true
        for (MetadataValue val : player.getMetadata("vanished")) {
            if (val.asBoolean()) return false; // vanished, false
        }
        return true; // vanished, return true
    }
}
