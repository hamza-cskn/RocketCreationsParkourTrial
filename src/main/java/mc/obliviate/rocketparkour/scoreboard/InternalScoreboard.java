package mc.obliviate.rocketparkour.scoreboard;

import com.google.common.base.Preconditions;
import mc.obliviate.rocketparkour.RocketParkour;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * HScoreboard class for creating a
 * scoreboard object for player
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class InternalScoreboard {

    private static final int MAX_PLAYER_NAME_LENGTH = 32;
    private static final Map<UUID, InternalScoreboard> SCOREBOARD_MAP = new HashMap<>();
    private final UUID uid;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private boolean terminated = false;

    private String title = "";
    private int updateInterval = 0;

    /**
     * Creates new Instance of this class.
     *
     * @param uid UID of player
     */
    public InternalScoreboard(UUID uid) {
        this.uid = Preconditions.checkNotNull(uid, "uuid cannot be null");
        this.scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective("board", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(this.title);
        InternalScoreboard.deleteIfPresent(uid);
        SCOREBOARD_MAP.put(uid, this);
    }

    public static void deleteIfPresent(UUID uuid) {
        InternalScoreboard.getScoreboard(uuid).ifPresent(InternalScoreboard::delete);
    }

    public static Optional<InternalScoreboard> getScoreboard(UUID uuid) {
        return Optional.ofNullable(SCOREBOARD_MAP.get(uuid));
    }

    public static Map<UUID, InternalScoreboard> getScoreboardMap() {
        return SCOREBOARD_MAP;
    }

    /**
     * Checks if scoreboard still exist for player
     *
     * @return if scoreboard still exist for player, return true
     */
    public boolean isExist() {
        return this.equals(SCOREBOARD_MAP.get(uid));
    }

    /**
     * Gets UUID of player.
     *
     * @return UUID of player.
     */
    public UUID getUID() {
        return this.uid;
    }

    /**
     * Gets player.
     *
     * @return Player.
     */
    public Optional<Player> getPlayerSafe() {
        Player player = Bukkit.getPlayer(this.uid);
        return Optional.ofNullable(player);
    }

    /**
     * Gets player.
     *
     * @return Player.
     */
    public Player getPlayer() {
        return this.getPlayerSafe().orElseThrow(() -> new NullPointerException("there is no player with this uid(" + this.uid + ")"));
    }

    /**
     * Gets title of scoreboard.
     *
     * @return Title of scoreboard.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets title of scoreboard.
     *
     * @param title Title.
     * @return Instance of this class.
     */
    public InternalScoreboard setTitle(String title) {
        this.title = Preconditions.checkNotNull(title, "title cannot be null");
        this.objective.setDisplayName(this.title);
        return this;
    }

    /**
     * Gets update interval of scoreboard.
     *
     * @return Update interval of scoreboard.
     */
    public int getUpdateInterval() {
        return this.updateInterval;
    }

    /**
     * Sets update interval of scoreboard.
     *
     * @param updateInterval Update interval.
     * @return Instance of this class.
     */
    public InternalScoreboard setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
        return this;
    }

    /**
     * Gets text of line.
     *
     * @param line Line.
     * @return Text of line.
     */
    public String getLine(int line) {
        return this.getTeam(line).getPrefix();
    }

    /**
     * Sets line of scoreboard to text.
     *
     * @param line Line number.
     * @param text Text.
     * @return Instance of this class.
     */
    public InternalScoreboard setLine(int line, String text) {
        Preconditions.checkNotNull(text, "text cannot be null");
        String first = text.substring(0, Math.min(MAX_PLAYER_NAME_LENGTH, text.length()));
        String second = null;
        if (first.endsWith("&")) {
            first = first.substring(0, first.length() - 1);
            second = "&";
        }
        this.getTeam(line).setPrefix(ChatColor.translateAlternateColorCodes('&', first));
        if (text.length() > MAX_PLAYER_NAME_LENGTH) {
            String lineText = (second == null ? InternalScoreboard.getLastRawColors(first) : second) + text.substring(MAX_PLAYER_NAME_LENGTH, Math.min(MAX_PLAYER_NAME_LENGTH * 2 - 2, text.length()));
            this.getTeam(line).setSuffix(ChatColor.translateAlternateColorCodes('&', lineText));
        }
        return this;
    }

    //Copied and modified by the ChatColor class.
    public static String getLastRawColors(String input) {
        String result = "";
        int length = input.length();
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == '&' && index < length - 1) {
                char colorChar = input.charAt(index + 1);
                result = "&" + colorChar;
            }
        }
        return result;
    }


    /**
     * Sets lines of scoreboard to lines.
     *
     * @param lines List of lines.
     * @return Instance of this class.
     */
    public InternalScoreboard setLines(List<String> lines) {
        Preconditions.checkNotNull(lines, "lines cannot be null");
        for (int i = 1; i <= 16; i++)
            if (lines.size() >= i) this.setLine(i, lines.get(i - 1));
            else this.removeLine(i);
        return this;
    }

    /**
     * Sets lines of scoreboard to lines.
     *
     * @param lines List of lines.
     * @return Instance of this class.
     */
    public InternalScoreboard setLines(String... lines) {
        return this.setLines(Arrays.asList(lines));
    }

    /**
     * Removes line from scoreboard.
     *
     * @param line Line number.
     * @return Instance of this class.
     */
    public InternalScoreboard removeLine(int line) {
        Team currentTeam = this.scoreboard.getTeam("line_" + line);
        if (currentTeam == null) {
            return this;
        }

        this.scoreboard.resetScores(currentTeam.getEntries().iterator().next());
        currentTeam.unregister();
        return this;
    }

    /**
     * When the time is up, scoreboard will
     * remove automatically.
     *
     * @param time     Time.
     * @param timeUnit Time unit (TimeUnit.SECONDS, TimeUnit.HOURS, etc.)
     * @return Instance of this class.
     */
    public InternalScoreboard expire(int time, TimeUnit timeUnit) {
        Preconditions.checkNotNull(timeUnit, "time unit cannot be null");
        Bukkit.getScheduler().runTaskLater(RocketParkour.getInstance(), this::delete, timeUnit.toMillis(time) / 50);
        return this;
    }

    /**
     * When the time is up, scoreboard will
     * remove automatically.
     *
     * @param duration Duration.
     * @return Instance of this class.
     */
    public InternalScoreboard expire(Duration duration) {
        Preconditions.checkNotNull(duration, "duration cannot be null!");
        Bukkit.getScheduler().runTaskLater(RocketParkour.getInstance(), this::delete, duration.toMillis() / 50);
        return this;
    }

    /**
     * When the time is up, scoreboard will
     * remove automatically.
     *
     * @param ticks Ticks.
     * @return Instance of this class.
     */
    public InternalScoreboard expire(int ticks) {
        Bukkit.getScheduler().runTaskLater(RocketParkour.getInstance(), this::delete, ticks);
        return this;
    }

    /**
     * Shows the scoreboard to player.
     */
    public InternalScoreboard show() {
        this.getPlayerSafe().ifPresent(player -> {
            if (player.getScoreboard().equals(this.scoreboard))
                return;

            player.setScoreboard(this.scoreboard);
        });
        return this;
    }

    /**
     * Once every updateInterval ticks,
     * it will trigger.
     *
     * @param consumer Callback.
     * @return Instance of this class.
     */
    public InternalScoreboard update(Consumer<InternalScoreboard> consumer) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");

        if (this.updateInterval > 0 && !this.terminated) {
            consumer.accept(this);
            Bukkit.getScheduler().runTaskLater(RocketParkour.getInstance(), () -> this.update(consumer), this.updateInterval);
        }
        return this;
    }

    /**
     * Deletes scoreboard.
     *
     * @return Instance of this class.
     */
    public InternalScoreboard delete() {
        this.getPlayerSafe().ifPresent(player -> {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            this.terminated = true;
            InternalScoreboard.SCOREBOARD_MAP.remove(this.uid);
            //HScoreboardHandler.getContent().remove(this.uid);
        });
        return this;
    }


    /**
     * Gets or creates team object.
     *
     * @param line Line.
     * @return Team.
     */
    private Team getTeam(int line) {
        Team currentTeam = this.scoreboard.getTeam("line_" + line);
        if (currentTeam != null) {
            return currentTeam;
        }

        Team newTeam = this.scoreboard.registerNewTeam("line_" + line);
        newTeam.setAllowFriendlyFire(true);
        newTeam.setCanSeeFriendlyInvisibles(false);
        if (newTeam.getEntries().size() > 0) {
            newTeam.removeEntry(newTeam.getEntries().iterator().next());
        }

        String teamEntry = line >= 10 ? "§" + new String[]{"a", "b", "c", "d", "e", "f"}[line - 10] : "§" + line;
        newTeam.addEntry(teamEntry);

        this.objective.getScore(teamEntry).setScore(16 - line);
        return newTeam;
    }
}
