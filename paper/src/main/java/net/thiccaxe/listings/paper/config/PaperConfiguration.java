package net.thiccaxe.listings.paper.config;

import net.thiccaxe.listings.ListingsPlugin;
import net.thiccaxe.listings.config.Configuration;
import net.thiccaxe.listings.paper.ListingsPaper;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;


public class PaperConfiguration extends Configuration {
    public PaperConfiguration(File dataFolder, ListingsPlugin plugin) {
        super(dataFolder, plugin);
    }

}

/*
public class PaperConfiguration extends YamlConfiguration {


    public PaperConfiguration(File dataFolder) {
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
        } catch (Exception e) {
            ListingsPaper.log("Failed to create plugin directory: " + dataFolder.getAbsolutePath());
            e.printStackTrace();
        }
    }

    protected File file;
    protected String getFileName() {
        return "config.yml";
    }
    public void setupFile(File dataFolder) {
        file = new File(dataFolder, getFileName());
        createIfNotExists();

        reload();

        initConfig();

        save();
    }


    public void save() {
        try {
            this.save(file);
        } catch (IOException | YAMLException e) {
            ListingsPaper.log("Failed to save file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
    public void reload() {
        try {
            this.load(file);
            loadConfiguration(file);

        } catch (Exception e) {
            ListingsPaper.log("Failed to load file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }
    private void createIfNotExists() {
        if (file.exists()) {
            return;
        }

        try {
            file.createNewFile();
            ListingsPaper.log("Created " + file.getAbsolutePath());
        } catch (Exception e) {
            ListingsPaper.log("Failed to create file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    protected void initConfig() {
        this.options().copyDefaults(true);

        this.options().header("~~ Listings Configuration ~~ #");

        this.addDefault("Info", new String[] {
                "Info Line 1",
                "Info Line 2",
                "etc."
        });
        this.addDefault("Player", "%player_name%");
        this.addDefault("Header", "Amazing Header");
        this.addDefault("Footer", "... and {X} more ...");
        this.addDefault("Justify", true);
        this.addDefault("JustifyInformation", "# Note: true = justify left, false = justify right");
    }

}


 */