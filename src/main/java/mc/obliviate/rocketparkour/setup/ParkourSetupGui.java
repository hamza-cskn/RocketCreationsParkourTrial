package mc.obliviate.rocketparkour.setup;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import mc.obliviate.rocketparkour.util.ChatEntry;
import mc.obliviate.rocketparkour.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;

import javax.annotation.Nonnull;

public class ParkourSetupGui extends Gui {

    private final ParkourSetupMode mode;

    public ParkourSetupGui(@Nonnull Player player, @Nonnull ParkourSetupMode mode) {
        super(player, "pakour-setup-gui", "Parkour Setup", 5);
        this.mode = mode;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        addItem(0, new Icon(Material.NAME_TAG).setName("set name").onClick(e -> {
            player.sendMessage("please enter name of the parkour to chat.");
            player.closeInventory();
            new ChatEntry(player.getUniqueId()).onResponse(chat -> {
                mode.setParkourName(chat.getMessage());
                open();
            });
        }));

        addItem(1, new Icon(Material.NAME_TAG).setName("set region").onClick(e -> {
            player.sendMessage("please enter region name of the parkour to chat.");
            player.closeInventory();
            new ChatEntry(player.getUniqueId()).onResponse(chat -> {
                mode.setWorldGuardRegionName(chat.getMessage());
                open();
            });
        }));

        addItem(2, new Icon(Material.LIGHT_WEIGHTED_PRESSURE_PLATE).setName("add checkpoint").onClick(e -> {
            this.mode.getCheckpoints().add(player.getLocation());
            player.closeInventory();
            player.sendMessage("checkpoint saved: " + Util.serializeLocation(player.getLocation()));
        }));

        addItem(3, new Icon(Material.EMERALD_BLOCK).setName("save").onClick(e -> {
            mode.finish();
            player.closeInventory();
        }));

    }
}
