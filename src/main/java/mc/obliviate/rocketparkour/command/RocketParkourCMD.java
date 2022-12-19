package mc.obliviate.rocketparkour.command;

import mc.obliviate.rocketparkour.setup.ParkourSetupGui;
import mc.obliviate.rocketparkour.setup.ParkourSetupMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class RocketParkourCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        Optional<ParkourSetupMode> setupMode = ParkourSetupMode.findSetupMode(player);
        if (setupMode.isEmpty()) {
            new ParkourSetupMode(player).enable();
        } else {
            new ParkourSetupGui(player, setupMode.get()).open();
        }
        return false;
    }
}
