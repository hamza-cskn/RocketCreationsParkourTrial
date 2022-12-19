package mc.obliviate.rocketparkour.setup;

import mc.obliviate.rocketparkour.RocketParkour;
import mc.obliviate.rocketparkour.parkour.AbstractParkour;
import mc.obliviate.rocketparkour.parkour.StandartParkour;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

public class ParkourSetupMode implements Listener {

    private static final Map<UUID, ParkourSetupMode> PARKOUR_SETUP_MODE_MAP = new HashMap<>();

    private final Player player;
    private final List<Location> checkpoints = new ArrayList<>();
    private String parkourName;
    private String worldGuardRegionName;

    public ParkourSetupMode(Player player) {
        this.player = player;
    }

    public void enable() {
        player.sendMessage("parkour setup mode enabled.");
        PARKOUR_SETUP_MODE_MAP.put(player.getUniqueId(), this);
    }

    public void finish() {
        PARKOUR_SETUP_MODE_MAP.remove(player.getUniqueId());
        AbstractParkour parkour = new StandartParkour(this.parkourName);
        checkpoints.forEach(parkour::addCheckpoint);
        parkour.setWorldGuardRegionName(worldGuardRegionName);
        RocketParkour.getParkourDatabase().save(parkour);
        player.sendMessage("parkour saved: " + parkourName);
    }

    public static Optional<ParkourSetupMode> findSetupMode(Player player) {
        return Optional.ofNullable(PARKOUR_SETUP_MODE_MAP.get(player.getUniqueId()));
    }

    public static Map<UUID, ParkourSetupMode> getParkourSetupModeMap() {
        return PARKOUR_SETUP_MODE_MAP;
    }

    public List<Location> getCheckpoints() {
        return checkpoints;
    }

    public Player getPlayer() {
        return player;
    }

    public String getParkourName() {
        return parkourName;
    }

    public void setParkourName(String parkourName) {
        this.parkourName = parkourName;
    }

    public String getWorldGuardRegionName() {
        return worldGuardRegionName;
    }

    public void setWorldGuardRegionName(String worldGuardRegionName) {
        this.worldGuardRegionName = worldGuardRegionName;
    }
}
