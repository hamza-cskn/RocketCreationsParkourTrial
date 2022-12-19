package mc.obliviate.rocketparkour.parkour;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import mc.obliviate.rocketparkour.RocketParkour;
import mc.obliviate.rocketparkour.game.ParkourGame;
import mc.obliviate.rocketparkour.scoreboard.InternalScoreboard;
import mc.obliviate.rocketparkour.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public abstract class AbstractParkour {

    private static final Map<String, AbstractParkour> PARKOUR_MAP = new HashMap<>();
    private final Map<Location, ParkourCheckpoint> checkpoints = new LinkedHashMap<>();
    private final String parkourName;
    private String worldGuardRegionName;

    protected AbstractParkour(String parkourName) {
        this.parkourName = parkourName;
        PARKOUR_MAP.put(this.parkourName, this);
        registerListener();
    }

    public World getWorld() {
        return checkpoints.keySet().iterator().next().getWorld();
    }

    public static Optional<AbstractParkour> find(String name) {
        return Optional.ofNullable(PARKOUR_MAP.get(name));
    }

    private void registerListener() {
        AbstractParkour parkour = this;
        Bukkit.getPluginManager().registerEvents(new Listener() {

            private final Set<UUID> playersInRegion = new HashSet<>();

            private void resetScoreboard(Player player) {
                new InternalScoreboard(player.getUniqueId()).setTitle(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Parkour ").setLines(
                        "",
                        "Parkour: " + ChatColor.GREEN + parkourName,
                        "",
                        ChatColor.RED + "You did not started to",
                        ChatColor.RED + " the parkour yet.").show();
            }

            @EventHandler
            public void onPlayerEnterRegion(final PlayerMoveEvent e) {
                if (e.getTo() == null || e.getFrom().distance(e.getTo()) == 0D) return;
                final var player = e.getPlayer();
                final var from = BukkitAdapter.adapt(e.getTo());
                final var to = BukkitAdapter.adapt(e.getFrom());
                final var container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                final var query = container.createQuery();
                final var oldRegions = query.getApplicableRegions(from);
                final var newRegions = query.getApplicableRegions(to);
                for (final ProtectedRegion oldRegion : oldRegions.getRegions()) {
                    if (!oldRegion.getId().equalsIgnoreCase(worldGuardRegionName)) continue;
                    if (newRegions.getRegions().contains(oldRegion)) return;
                    playersInRegion.add(player.getUniqueId());
                    resetScoreboard(player);
                    checkForCheckpoints(player);
                    return;

                }
            }

            public void checkForCheckpoints(Player player) {
                var game = ParkourGame.findParkourGame(player);
                findCheckpointAt(player.getLocation()).ifPresent(checkpoint -> {
                    if (checkpoint.equals(getFirstCheckpoint()) && (game.isEmpty() || game.get().getCompletedCheckpoints().size() > 1)) {
                        new ParkourGame(parkour, player).complete(checkpoint,
                                parkourGame -> {
                                    InternalScoreboard.getScoreboard(player.getUniqueId()).ifPresent(sb -> {
                                        sb.setUpdateInterval(20);
                                        sb.update(internalScoreboard -> sb.setLines(
                                                "",
                                                "Parkour: " + ChatColor.GREEN + parkourName,
                                                "",
                                                "Time:",
                                                " " + ChatColor.LIGHT_PURPLE + parkourGame.getDuration().toMinutesPart() + "minutes " +
                                                        parkourGame.getDuration().toSecondsPart() + " seconds"));
                                    });
                                    player.sendMessage(ChatColor.YELLOW + "You have started the parkour: " + ChatColor.GOLD + parkourName);
                                });
                    } else {
                        game.ifPresent(parkourGame -> parkourGame.complete(checkpoint, parkourGame1 -> {
                            player.sendMessage(ChatColor.YELLOW + "You have reached " + (parkourGame.getCompletedCheckpoints().size() - 1) + ". checkpoint in " + ChatColor.GOLD + parkourGame1.getDuration().toSeconds() + "s");
                            if (parkourGame1.isFinished()) {
                                player.sendMessage(ChatColor.YELLOW + "You have finished the parkour: " + parkourName);
                                resetScoreboard(player);
                            }
                        }));
                    }
                });
                if (!playersInRegion.contains(player.getUniqueId())) return;
                Bukkit.getScheduler().runTaskLater(RocketParkour.getInstance(), () -> checkForCheckpoints(player), 1);
            }

            @EventHandler
            public void onPlayerLeaveRegion(final PlayerMoveEvent e) {
                if (e.getTo() == null || e.getFrom().distance(e.getTo()) == 0D) return;
                final var player = e.getPlayer();
                final var from = BukkitAdapter.adapt(e.getFrom());
                final var to = BukkitAdapter.adapt(e.getTo());
                final var query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
                final var originalSet = query.getApplicableRegions(from);
                final var newSet = query.getApplicableRegions(to);
                for (final ProtectedRegion oldRegion : originalSet.getRegions()) {
                    if (!oldRegion.getId().equalsIgnoreCase(worldGuardRegionName)) continue;
                    if (newSet.getRegions().contains(oldRegion)) return;

                    playersInRegion.remove(player.getUniqueId());
                    InternalScoreboard.deleteIfPresent(player.getUniqueId());
                    ParkourGame.findParkourGame(player).ifPresent(game -> ParkourGame.getParkourGameMap().remove(player.getUniqueId()));
                    return;
                }
            }
        }, RocketParkour.getInstance());
    }

    public void addCheckpoint(ParkourCheckpoint checkpoint) {
        checkpoints.put(Util.normalizeLocation(checkpoint.location()), checkpoint);
    }

    public void addCheckpoint(Location loc) {
        addCheckpoint(new ParkourCheckpoint(loc, this));
    }

    public Optional<ParkourCheckpoint> findCheckpointAt(Location location) {
        return Optional.ofNullable(checkpoints.get(Util.normalizeLocation(location)));
    }

    public ParkourCheckpoint getFirstCheckpoint() {
        return checkpoints.values().iterator().next();
    }

    public ParkourCheckpoint getLastCheckpoint() {
        var iterator = checkpoints.values().iterator();
        while (iterator.hasNext()) {
            var val = iterator.next();
            if (!iterator.hasNext()) return val;
        }
        return null;
    }

    public Map<Location, ParkourCheckpoint> getCheckpoints() {
        return checkpoints;
    }

    public String getParkourName() {
        return parkourName;
    }

    public String getWorldGuardRegionName() {
        return worldGuardRegionName;
    }

    public void setWorldGuardRegionName(String worldGuardRegionName) {
        this.worldGuardRegionName = worldGuardRegionName;
    }
}
