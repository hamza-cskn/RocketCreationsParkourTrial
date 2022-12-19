package mc.obliviate.rocketparkour.game;

import com.sk89q.worldedit.util.collection.DoubleArrayList;
import mc.obliviate.rocketparkour.RocketParkour;
import mc.obliviate.rocketparkour.parkour.AbstractParkour;
import mc.obliviate.rocketparkour.parkour.ParkourCheckpoint;
import mc.obliviate.rocketparkour.statistics.ParkourStatistic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class ParkourGame {

    private static final Map<UUID, ParkourGame> PARKOUR_GAME_MAP = new HashMap<>();
    private final AbstractParkour parkour;
    private final Player player;
    private final long startDate;
    private final List<ParkourCheckpoint> completedCheckpoints = new ArrayList<>();

    public ParkourGame(AbstractParkour parkour, Player player) {
        this.parkour = parkour;
        this.player = player;
        this.startDate = System.currentTimeMillis();
        PARKOUR_GAME_MAP.put(player.getUniqueId(), this);
    }

    public static Optional<ParkourGame> findParkourGame(Player player) {
        return Optional.ofNullable(PARKOUR_GAME_MAP.get(player.getUniqueId()));
    }

    public static Map<UUID, ParkourGame> getParkourGameMap() {
        return PARKOUR_GAME_MAP;
    }

    /**
     * Completes a checkpoint of the parkour.
     *
     * @param checkpoint the checkpoint to complete
     */
    public void complete(ParkourCheckpoint checkpoint) {
        complete(checkpoint, game -> {
        });
    }

    /**
     * Completes a checkpoint of the parkour.
     *
     * @param checkpoint the checkpoint to complete
     * @param consumer   if checkpoint did not complete before, consumer will run.
     */
    public void complete(ParkourCheckpoint checkpoint, Consumer<ParkourGame> consumer) {
        if (completedCheckpoints.contains(checkpoint)) return;
        completedCheckpoints.add(checkpoint);
        if (completedCheckpoints.size() == parkour.getCheckpoints().size()) complete();
        consumer.accept(this);
    }

    /**
     * Completes all checkpoints of the parkour.
     */
    public void complete() {
        PARKOUR_GAME_MAP.remove(player.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(RocketParkour.getInstance(), () -> {
            RocketParkour.getInstance().getStatisticRepository().save(new ParkourStatistic(player.getUniqueId(), parkour.getParkourName(), getDuration()));
            var completedParkours = RocketParkour.getInstance().getStatisticRepository().query(player.getUniqueId(), parkour);
            completedParkours.remove(0);
            completedParkours.forEach(RocketParkour.getInstance().getStatisticRepository()::delete);
        });
    }

    public boolean isFinished() {
        return PARKOUR_GAME_MAP.get(player.getUniqueId()).equals(this);
    }

    public List<ParkourCheckpoint> getCompletedCheckpoints() {
        return completedCheckpoints;
    }

    public Duration getDuration() {
        return Duration.ofMillis(System.currentTimeMillis() - startDate);
    }

    public long getStartDate() {
        return startDate;
    }

    public Player getPlayer() {
        return player;
    }
}
