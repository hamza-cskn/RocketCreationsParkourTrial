package mc.obliviate.rocketparkour;

import mc.obliviate.inventory.InventoryAPI;
import mc.obliviate.rocketparkour.command.RocketParkourCMD;
import mc.obliviate.rocketparkour.config.ConfigurationHandler;
import mc.obliviate.rocketparkour.database.parkour.ParkourDatabase;
import mc.obliviate.rocketparkour.database.parkour.YamlParkourDatabase;
import mc.obliviate.rocketparkour.database.repository.StatisticRepository;
import mc.obliviate.rocketparkour.util.ChatEntry;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class RocketParkour extends JavaPlugin {

    private static ParkourDatabase parkourDatabase;
    private StatisticRepository statisticRepository;

    public static RocketParkour getInstance() {
        return JavaPlugin.getPlugin(RocketParkour.class);
    }

    @Override
    public void onEnable() {
        new ConfigurationHandler(this).init();
        ChatEntry.init(this);
        new InventoryAPI(this).init();
        connectDatabases();
        Objects.requireNonNull(getCommand("rocketparkour")).setExecutor(new RocketParkourCMD());
    }

    public void connectDatabases() {
        (RocketParkour.parkourDatabase = new YamlParkourDatabase(ConfigurationHandler.getParkours())).connect();
        this.statisticRepository = new StatisticRepository();
    }

    public static ParkourDatabase getParkourDatabase() {
        return parkourDatabase;
    }

    public StatisticRepository getStatisticRepository() {
        return statisticRepository;
    }
}
