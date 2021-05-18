package net.thiccaxe.listings.config;

import net.thiccaxe.listings.JustifyType;
import net.thiccaxe.listings.ListingsPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class Configuration {
    protected final ListingsPlugin plugin;
    protected final File dataFolder;
    private ConfigurationLoader<CommentedConfigurationNode> defaultLoader;
    private ConfigurationNode defaultConfiguration;


    protected File file;
    protected ConfigurationLoader<CommentedConfigurationNode> loader;
    protected ConfigurationNode root;
    protected String getFileName() {return "config.conf";}


    private Integer maximumColumns;
    private Integer maximumRows;
    private String header = null;
    private String footer = null;
    private String extra = null;
    private boolean vanish = true;
    private @NotNull String format = "";
    private int cache = 0;
    private @NotNull List<String> info = Collections.emptyList();
    private JustifyType justifyType = JustifyType.LEFT;
    private ServerType serverType = ServerType.PROXY;
    private String server = null;
    private @NotNull String errorMessage = "<bold><red>!</bold> Missing Permissions! </red><white>\"<yellow>listings.reload</yellow>\"</white>";
    private @NotNull String reloadedMessage = "<bold><green>!</bold> Reloaded!</green>";







    public Configuration(File dataFolder, ListingsPlugin plugin, ServerType serverType) {
        this.dataFolder = dataFolder;
        this.plugin = plugin;
        this.serverType = serverType;
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
        } catch (Exception e) {
            this.plugin.log("Failed to create plugin directory: " + dataFolder.getAbsolutePath());
            e.printStackTrace();
        }
    }
    public void setUpFile() {
        file = new File(dataFolder, getFileName());
        createIfNotExists();
        loader = HoconConfigurationLoader.builder().file(file).build();

        reload();

    }

    protected void initConfig() {
        //merge defaults
        System.out.println("Init Config...");
        try {
            File defaultConfig = new File(dataFolder, "default_config.conf");
            if (!defaultConfig.exists()) {
                defaultConfig.createNewFile();
            }
            FileOutputStream outputStream  = new FileOutputStream(defaultConfig,false);
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.conf");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            outputStream.write(buffer);
            outputStream.close();
            if (defaultLoader == null) {
                defaultLoader = HoconConfigurationLoader.builder().file(defaultConfig).build();
            }
            defaultConfiguration = defaultLoader.load();
            if (loader == null) {
                loader = HoconConfigurationLoader.builder().file(file).build();
            }
            root = loader.load();
            root.mergeFrom(defaultConfiguration);
        } catch (Exception e) {
            plugin.log("Failed to load default configuration file!");
            e.printStackTrace();
        }

    }

    protected void createIfNotExists() {
        if (file.exists()) {
            return;
        }

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            plugin.log("Created " + file.getAbsolutePath());
        } catch (Exception e) {
            plugin.log("Failed to create file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
    public void save() {
        System.out.println("Saving...");
        try {
            loader.save(root);
        } catch (Exception e) {
            plugin.log("Failed to save: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
    public void reload() {
        try {
            System.out.println("Reloading...");
            initConfig();
            updateOptions();
            save();
        } catch (Exception e) {
            plugin.log("Failed to load: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
    public ConfigurationNode getRoot() {
        return root;
    }
    public ConfigurationNode getConfig() {
        return root.node("configuration");
    }



    protected void updateOptions() {
        try {
            if (root != null) {
                ConfigurationNode config = root.node("configuration");
                maximumColumns = config.node("maximumColumns").getInt(3);
                maximumRows = config.node("maximumRows").getInt(15);
                vanish = config.node("vanish").getBoolean(true);
                ConfigurationNode headerNode = config.node("header");
                header = headerNode.node("header").getString("");
                if (!headerNode.node("enabled").getBoolean(false)) {
                    header = null;
                }
                ConfigurationNode footerNode = config.node("footer");
                footer = footerNode.node("footer").getString("");
                if (!footerNode.node("enabled").getBoolean(false)) {
                    footer = null;
                }
                ConfigurationNode extraNode = config.node("extra");
                extra = extraNode.node("extra").getString("");
                if (!extraNode.node("enabled").getBoolean(false)) {
                    extra = null;
                }
                format = config.node("format").getString("");
                justifyType = JustifyType.getType(config.node("justify").getString("left"));
                cache = config.node("cache").getInt(0);
                serverType = ServerType.getServerType(config.node("type").getString("proxy"));
                server = config.node("server").getString("");
                if (server.equals("")) server = null;

                info = config.node("info").getList(String.class, Collections.emptyList());
                errorMessage = config.node("messages").node("error").getString("<bold><red>!</bold> Missing Permissions! </red><white>\"<yellow>listings.reload</yellow>\"</white>");
                reloadedMessage = config.node("messages").node("reloaded").getString("<bold><green>!</bold> Reloaded!</green>");


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public @NotNull Integer getMaximumColumns() {
        return maximumColumns;
    }

    public @NotNull Integer getMaximumRows() {
        return maximumRows;
    }

    public boolean vanishEnabled() {
        return vanish;
    }

    public @NotNull Integer getCacheTimeout() {
        return cache;
    }

    public @Nullable String getHeader() {
        return header;
    }

    public @Nullable String getFooter() {
        return footer;
    }

    public @Nullable String getExtra() {
        return extra;
    }

    public @NotNull String getFormat() {
        return format;
    }

    public @NotNull JustifyType getJustifyType() {
        return justifyType;
    }

    public @Nullable String getServer() {
        return server;
    }

    public @NotNull ServerType getServerType() {
        return serverType;
    }

    public @NotNull List<String> getInfo() {
        return info;
    }

    public @NotNull String getErrorMessage() {
        return errorMessage;
    }

    public @NotNull String getReloadedMessage() {
        return reloadedMessage;
    }
}
