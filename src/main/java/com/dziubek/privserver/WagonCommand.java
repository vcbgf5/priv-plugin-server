package com.dziubek.privserver;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

/** /wagon - patrzysz na wagonik i wołasz komendę, żeby go zaznaczyć do łączenia (patrz WagonManager). */
public class WagonCommand implements CommandExecutor {

    private static final double LOOK_RANGE = 10.0;

    private final PrivServerPlugin plugin;

    public WagonCommand(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Tej komendy może użyć tylko gracz.");
            return true;
        }

        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(), player.getEyeLocation().getDirection(), LOOK_RANGE,
                entity -> entity instanceof Minecart);
        Entity hit = result != null ? result.getHitEntity() : null;
        if (!(hit instanceof Minecart cart)) {
            player.sendMessage(ChatColor.RED + "Musisz patrzeć na wagonik (w promieniu " + (int) LOOK_RANGE + " bloków).");
            return true;
        }

        plugin.getWagons().select(player, cart);
        return true;
    }
}
