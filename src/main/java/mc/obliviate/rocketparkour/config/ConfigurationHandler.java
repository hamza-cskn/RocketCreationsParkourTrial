package mc.obliviate.rocketparkour.config;

import mc.obliviate.rocketparkour.RocketParkour;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigurationHandler {

    private static final String CONFIG_FILE_NAME = "config.yml";
    private static YamlConfiguration config;
    public static final String PARKOUR_FILE_NAME = "parkour.yml";
    private static YamlConfiguration parkours;

    private final RocketParkour plugin;

    public ConfigurationHandler(RocketParkour plugin) {
        this.plugin = plugin;
    }

    public void init() {
        loadConfigFile(new File(plugin.getDataFolder().getPath() + File.separator + CONFIG_FILE_NAME));
        loadParkourFile(new File(plugin.getDataFolder().getPath() + File.separator + PARKOUR_FILE_NAME));
    }

    private void loadConfigFile(File configFile) {
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.getKeys(false).isEmpty()) {
            plugin.saveResource(CONFIG_FILE_NAME, true);
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    private void loadParkourFile(File file) {
        parkours = YamlConfiguration.loadConfiguration(file);
    }

    public static YamlConfiguration getParkours() {
        return parkours;
    }

    public static YamlConfiguration getConfig() {
        return config;
    }
}

