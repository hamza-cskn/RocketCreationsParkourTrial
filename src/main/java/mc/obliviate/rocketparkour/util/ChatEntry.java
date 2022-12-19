package mc.obliviate.rocketparkour.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatEntry implements Listener {

    private static final Map<UUID, ChatEntry> entryMap = new HashMap<>();
    private static Plugin plugin;
    public Consumer<AsyncPlayerChatEvent> action;

    public ChatEntry(UUID uuid) {
        entryMap.put(uuid, this);
        Bukkit.getPluginManager().registerEvents(this, ChatEntry.plugin);
    }

    public static void init(Plugin plugin) {
        ChatEntry.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void handleChatEvent(AsyncPlayerChatEvent e) {
        final Player sender = e.getPlayer();
        final ChatEntry chatEntry = entryMap.get(sender.getUniqueId());
        if (chatEntry == null || chatEntry.getAction() == null) return;
        e.setCancelled(true);
        Bukkit.getScheduler().runTask(ChatEntry.plugin, () -> chatEntry.getAction().accept(e));
        unregisterEntryTask(sender.getUniqueId());
    }

    public static void unregisterEntryTask(UUID senderUniqueId) {
        HandlerList.unregisterAll(entryMap.get(senderUniqueId));
        entryMap.remove(senderUniqueId);
    }

    public void onResponse(Consumer<AsyncPlayerChatEvent> e) {
        this.action = e;
    }

    public Consumer<AsyncPlayerChatEvent> getAction() {
        return action;
    }

}
