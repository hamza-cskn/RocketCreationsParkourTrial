package mc.obliviate.rocketparkour.util;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class Util {

    public static Location normalizeLocation(Location loc) {
        return loc.getBlock().getLocation();
    }

    public static Location deserializeLocation(String serializedLocation) {
        if (serializedLocation == null) return null;
        String[] data = serializedLocation.split(" ");
        World world = Bukkit.getWorld(data[0]);
        Preconditions.checkNotNull(world, "world could not find. serialized location: " + serializedLocation);
        return new Location(world, Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
    }

    public static String serializeLocation(Location loc) {
        if (loc == null) return null;
        return Objects.requireNonNull(loc.getWorld()).getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
    }

}
