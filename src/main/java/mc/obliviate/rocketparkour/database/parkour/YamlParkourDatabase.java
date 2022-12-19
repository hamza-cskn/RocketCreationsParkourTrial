package mc.obliviate.rocketparkour.database.parkour;

import com.google.common.base.Preconditions;
import mc.obliviate.rocketparkour.RocketParkour;
import mc.obliviate.rocketparkour.config.ConfigurationHandler;
import mc.obliviate.rocketparkour.parkour.AbstractParkour;
import mc.obliviate.rocketparkour.parkour.ParkourCheckpoint;
import mc.obliviate.rocketparkour.parkour.StandartParkour;
import mc.obliviate.rocketparkour.util.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class YamlParkourDatabase implements ParkourDatabase {

    private final YamlConfiguration section;

    public YamlParkourDatabase(YamlConfiguration section) {
        this.section = section;
    }

    @Override
    public void save(AbstractParkour parkour) {
        ConfigurationSection dataSection = this.section.createSection(parkour.getParkourName());
        this.serialize(dataSection, parkour);
        try {
            this.section.save(RocketParkour.getInstance().getDataFolder() + File.separator + ConfigurationHandler.PARKOUR_FILE_NAME);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void serialize(ConfigurationSection dataSection, AbstractParkour parkour) {
        parkour.getCheckpoints().values().forEach(checkpoint -> serialize(dataSection, checkpoint));
        dataSection.set("region", parkour.getWorldGuardRegionName());
    }

    private void serialize(ConfigurationSection section, ParkourCheckpoint checkpoint) {
        ConfigurationSection dataSection = section.createSection(String.valueOf(section.getKeys(false).size()));
        dataSection.set("location", Util.serializeLocation(checkpoint.location()));
    }

    @Override
    public void delete(AbstractParkour parkour) {
        Preconditions.checkNotNull(parkour, "parkour cannot be null");
        this.section.set(parkour.getParkourName(), null);
    }

    @Override
    public List<AbstractParkour> queryAll() {
        return section.getKeys(false).stream().map(key -> query(key).orElseThrow()).collect(Collectors.toList());
    }

    @Override
    public Optional<AbstractParkour> query(String name) {
        ConfigurationSection dataSection = section.getConfigurationSection(name);
        if (dataSection == null) return Optional.empty();
        AbstractParkour parkour = deserializeParkour(dataSection);
        return Optional.of(parkour);
    }

    private AbstractParkour deserializeParkour(ConfigurationSection section) {
        AbstractParkour parkour;
        if (section.getString("parkour-type", "standart").equalsIgnoreCase("standart")) {
            parkour = new StandartParkour(section.getName());
        } else {
            throw new IllegalArgumentException("unknown parkour type specified: " + section.getString("parkour-type"));
        }
        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key))
                parkour.addCheckpoint(deserializeCheckpoint(Objects.requireNonNull(section.getConfigurationSection(key))));
        }
        parkour.setWorldGuardRegionName(section.getString("region"));
        return parkour;
    }

    private ParkourCheckpoint deserializeCheckpoint(ConfigurationSection section) {
        return new ParkourCheckpoint(Util.deserializeLocation(section.getString("location")), AbstractParkour.find(Objects.requireNonNull(section.getParent()).getName()).orElseThrow());
    }

    @Override
    public void connect() {
        queryAll();
    }

    @Override
    public void disconnect() {

    }

}
