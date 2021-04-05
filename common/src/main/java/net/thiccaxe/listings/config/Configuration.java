package net.thiccaxe.listings.config;

import net.thiccaxe.listings.ListingsPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.*;

public abstract class Configuration {
    protected final ListingsPlugin plugin;
    protected final File dataFolder;
    private ConfigurationLoader<CommentedConfigurationNode> defaultLoader;
    private ConfigurationNode defaultConfiguration;

    protected File file;
    protected ConfigurationLoader<CommentedConfigurationNode> loader;
    protected ConfigurationNode root;
    protected String getFileName() {return "config.conf";}



    public Configuration(File dataFolder, ListingsPlugin plugin) {
        this.dataFolder = dataFolder;
        this.plugin = plugin;
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

        initConfig();

        save();
    }

    protected void initConfig() {
        //merge defaults
        if (defaultConfiguration != null) {
            root.mergeFrom(defaultConfiguration);
        } else {
            try {
                File defaultConfig = new File(dataFolder, "default_config.conf");
                defaultConfig.createNewFile();
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.conf");
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                OutputStream outputStream = new FileOutputStream(defaultConfig);
                outputStream.write(buffer);
                defaultLoader = HoconConfigurationLoader.builder().file(defaultConfig).build();
                defaultConfiguration = defaultLoader.load();
                root.mergeFrom(defaultConfiguration);
            } catch (Exception e) {
                plugin.log("Failed to load default configuration file!");
                e.printStackTrace();
            }
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
        try {
            loader.save(root);
        } catch (Exception e) {
            plugin.log("Failed to save: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
    public void reload() {
        try {
            root = loader.load();
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

}
