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

public class ListingsPaper extends JavaPlugin implements Listener, CommandExecutor, ListingsPlugin {

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
                log("Reloaded!");
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

    @Override
    public boolean cached() {
        return (System.currentTimeMillis() - lastCacheUpdate < (configuration.getCacheTimeout()*1000));
    }

    @Override
    public void setCache() {
        lastCacheUpdate = System.currentTimeMillis();
    }

    private List<PlayerProfile> handlePing(PaperServerListPingEvent event) {
        List<String> response = new ArrayList<>();
        List<Player> players = new ArrayList<>(Bukkit.getServer().getOnlinePlayers())
                .stream().filter(this::isNotVanished).collect(Collectors.toList());
        if (configuration.getServerType() == ServerType.SERVER) {
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
                response.add("[INFO]");
                response.add(String.valueOf(configuration.getInfo().size()));
                response.addAll(configuration.getInfo().stream().map(info -> fullParse(null, info)).collect(Collectors.toList()));
            }
            if (players.size() > 0) {
                response.add("[PLAYERS]");
                response.add(String.valueOf(players.size()));
                players.forEach(player -> response.add(fullParse(player, configuration.getFormat())));
            }

        } else {
            String header = configuration.getHeader();
            if (header != null) {
                header = header.replace("{ONLINE}", String.valueOf(players.size()));
            }
            String footer = configuration.getHeader();
            if (footer != null) {
                footer = footer.replace("{ONLINE}", String.valueOf(players.size()));
            }
            String extra = configuration.getExtra();
            response.addAll(Formatter.format(players.stream().map(
                    player -> fullParse(player, configuration.getFormat())
            ).collect(Collectors.toList()),
                    configuration.getInfo().stream().map(
                            info -> fullParse(null, info)
                    ).collect(Collectors.toList()),
                    fullParse(null, header),
                    fullParse(null, footer),
                    extra != null ? fullParse(null, extra) : null,
                    configuration.getMaximumColumns(),
                    configuration.getMaximumRows(),
                    configuration.getJustifyType()
            ));
        }
        return response.stream().map(string -> Bukkit.createProfile(UUID.randomUUID(), string)).collect(Collectors.toList());

    }


    private String fullParse(Player player, String string) {
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
