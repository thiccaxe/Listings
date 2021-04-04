package net.thiccaxe.listings.paper;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.thiccaxe.listings.Formatter;
import net.thiccaxe.listings.Text;
import net.thiccaxe.listings.paper.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class ListingsPaper extends JavaPlugin implements Listener, CommandExecutor {

    private File dataFolder;
    private static Logger logger;
    private Configuration configuration;

    private final List<Text> info = new LinkedList<>();
    private final Text header = new Text("");
    private final Text footer = new Text("");
    private Boolean playerJustify = null;
    private String playerFormat = null;


    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.dataFolder = getDataFolder();
            logger = getLogger();
            configuration = new Configuration(dataFolder);
            configuration.setupFile(this.dataFolder);
            Objects.requireNonNull(getCommand("listings")).setExecutor(this);

            getServer().getPluginManager().registerEvents(this, this);
        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            getLogger().info("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

    }


    @EventHandler
    public void onServerListPing(PaperServerListPingEvent event) {
        List<PlayerProfile> hover = event.getPlayerSample();
        hover.clear();
        if (info.isEmpty()) {
            configuration.getStringList("Info").forEach(infoLine -> info.add(
                    new Text(
                            LegacyComponentSerializer.legacySection().serialize(
                                    MiniMessage.get().parse(
                                            PlaceholderAPI.setPlaceholders(null, infoLine)
                                    )
                            )
                    )
            ));
        }
        if (header.name().isEmpty()) {
            header.name(
                    LegacyComponentSerializer.legacySection().serialize(
                            MiniMessage.get().parse(
                                    PlaceholderAPI.setPlaceholders(null, Objects.requireNonNull(configuration.getString("Header", "Amazing Header")))
                            )
                    )
            );
        }
        if (footer.name().isEmpty()) {
            footer.name(
                    LegacyComponentSerializer.legacySection().serialize(
                            MiniMessage.get().parse(
                                    PlaceholderAPI.setPlaceholders(null, Objects.requireNonNull(configuration.getString("Footer", "... and {X} more ...")))
                            )
                    )
            );
        }
        if (playerJustify == null) {
            playerJustify = configuration.getBoolean("Justify", true);
        }
        if (playerFormat == null) {
            playerFormat = Objects.requireNonNull(configuration.getString("Player", "%player_name%"));
        }
        List<Text> players = new LinkedList<>();
        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                    if (!player.hasPermission("listings.hidden")) {
                        players.add(
                                new Text(
                                        LegacyComponentSerializer.legacySection().serialize(
                                                MiniMessage.get().parse(
                                                        PlaceholderAPI.setPlaceholders(player, playerFormat)
                                                )
                                        )
                                )
                        );
                    }
                }
        );


        Formatter.format(players, info, header.copy(), footer.copy(), playerJustify).forEach(row -> hover.add(Bukkit.createProfile(UUID.randomUUID(), row)));


    }

    public static void log(String s) {
        logger.info(s);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("listings.reload")) {
            info.clear();
            header.name("");
            playerJustify = null;
            playerFormat = null;
            configuration.reload();
            return true;
        }
        return false;

    }
}
