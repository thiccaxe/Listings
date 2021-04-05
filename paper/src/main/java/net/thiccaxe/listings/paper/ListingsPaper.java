package net.thiccaxe.listings.paper;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.thiccaxe.listings.Formatter;
import net.thiccaxe.listings.JustifyType;
import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.Text;
import net.thiccaxe.listings.paper.config.PaperConfiguration;
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

public class ListingsPaper extends JavaPlugin implements Listener, CommandExecutor, ListingsPlugin {

    private File dataFolder;
    private static Logger logger;
    private PaperConfiguration configuration;

    //private final List<Text> info = new LinkedList<>();
    //private final Text header = new Text("");
    //private final Text footer = new Text("");
    //private JustifyType playerJustify = null;
    //private String playerFormat = null;

    private final HashMap<String, Integer> requests = new HashMap<>();


    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.dataFolder = getDataFolder();
            logger = getLogger();
            configuration = new PaperConfiguration(dataFolder, this);
            configuration.setUpFile();
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
        String addr = event.getClient().getAddress().getAddress().toString();
        requests.put(addr, requests.getOrDefault(addr, 0)+1);
        System.out.println("(" + requests.getOrDefault(addr, 0) + ") " + addr);


        List<PlayerProfile> hover = event.getPlayerSample();
        hover.clear();
        final List<Text> info = new LinkedList<>();
        info.add(new Text("Info Line 1"));
        info.add(new Text("Info Line 2"));
        info.add(new Text("etc"));


        /*
        if (info.isEmpty()) {
            config.getStringList("Info").forEach(infoLine -> info.add(
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
                                    PlaceholderAPI.setPlaceholders(null, Objects.requireNonNull(config.getString("Header", "Amazing Header")))
                            )
                    )
            );
        }
        if (footer.name().isEmpty()) {
            footer.name(
                    LegacyComponentSerializer.legacySection().serialize(
                            MiniMessage.get().parse(
                                    PlaceholderAPI.setPlaceholders(null, Objects.requireNonNull(config.getString("Footer", "... and {X} more ...")))
                            )
                    )
            );
        }
        if (playerJustify == null) {
            playerJustify = JustifyType.getType(config.getString("Justify", "left"));
        }
        if (playerFormat == null) {
            playerFormat = Objects.requireNonNull(config.getString("Player", "%player_name%"));
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

         */
        //Formatter.createColumnGroup(players, info, header.copy(), footer.copy(), 20, 2, playerJustify).forEach(row -> hover.add(Bukkit.createProfile(UUID.randomUUID(), row.name().replace("\0", ""))));
        Formatter.createColumnGroup(
                Collections.emptyList(),
                info,
                new Text(configuration.getConfig().node("header").getString("Paper Header")),
                new Text(configuration.getConfig().node("footer").getString("Paper Footer")),
                15,
                2,
                JustifyType.getType(configuration.getConfig().node("justify").getString("left"))
        )
                .forEach(row -> hover.add(Bukkit.createProfile(UUID.randomUUID(), row.name().replace("\0", ""))));
        //Formatter.format(players, info, header.copy(), footer.copy(), playerJustify).forEach(row -> hover.add(Bukkit.createProfile(UUID.randomUUID(), row)));


    }

    public void log(String s) {
        logger.info(s);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("listings.reload")) {
            //info.clear();
            //header.name("");
            //playerJustify = null;
            //playerFormat = null;
            configuration.reload();
            return true;
        }
        return false;

    }
}
