package com.dziubek.privserver;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /setpower <bloki/s> - patrzysz na szyne (zwykla albo powered rail) i ustawiasz jej wlasna,
 * sztywna predkosc docelowa - kazdy wagonik przejezdzajacy przez ten blok bedzie jechal z tą
 * predkoscia, nawet ponad wanilijny limit. /setpower 0 usuwa efekt z szyny, na ktora patrzysz.
 */
public class PoweredRailCommand implements CommandExecutor {

    private static final int LOOK_RANGE = 10;

    private final PrivServerPlugin plugin;

    public PoweredRailCommand(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Tej komendy może użyć tylko gracz.");
            return true;
        }
        if (args.length < 1) {
            ChatUtil.sendUsage(sender, "/setpower <bloki/sekunde>", "wymusza predkosc na szynie, na ktora patrzysz (0 = wylacz)");
            return true;
        }

        double blocksPerSecond;
        try {
            blocksPerSecond = Double.parseDouble(args[0].replace(',', '.'));
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "To nie jest liczba: '" + args[0] + "'.");
            return true;
        }

        Block target = player.getTargetBlockExact(LOOK_RANGE);
        if (target == null || !isRail(target.getType())) {
            sender.sendMessage(ChatColor.RED + "Musisz patrzeć na szynę (w promieniu " + LOOK_RANGE + " bloków).");
            return true;
        }

        if (blocksPerSecond <= 0) {
            plugin.getPoweredRails().clearPower(target);
            sender.sendMessage(ChatColor.YELLOW + "Usunięto wymuszoną prędkość z tej szyny.");
            return true;
        }

        plugin.getPoweredRails().setPower(target, blocksPerSecond);
        sender.sendMessage(ChatColor.GREEN + "Ta szyna teraz zawsze rozpędza wagonik do " + blocksPerSecond + " bloków/s.");
        return true;
    }

    private static boolean isRail(Material material) {
        return material == Material.RAIL || material == Material.POWERED_RAIL
                || material == Material.DETECTOR_RAIL || material == Material.ACTIVATOR_RAIL;
    }
}
