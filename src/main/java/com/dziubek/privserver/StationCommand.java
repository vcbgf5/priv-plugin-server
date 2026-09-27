package com.dziubek.privserver;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

/**
 * /stacja dodaj - patrzysz na wagonik, zapamiętuje go jako slot startowy (max 2 na stację).
 * /stacja przypisz <bloki/s> - patrzysz na przycisk/dźwignię, wiąże zapamiętane sloty z nim.
 * /stacja usun - patrzysz na przycisk stacji, usuwa ją.
 */
public class StationCommand implements CommandExecutor {

    private static final double LOOK_RANGE = 10.0;

    private final PrivServerPlugin plugin;

    public StationCommand(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Tej komendy może użyć tylko gracz.");
            return true;
        }
        if (args.length < 1) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "dodaj" -> handleAdd(player);
            case "przypisz" -> handleAssign(player, args);
            case "usun" -> handleRemove(player);
            default -> sendHelp(player);
        }
        return true;
    }

    private void handleAdd(Player player) {
        Entity hit = rayTraceEntity(player);
        if (!(hit instanceof Minecart cart)) {
            player.sendMessage(ChatColor.RED + "Musisz patrzeć na wagonik.");
            return;
        }
        if (!plugin.getStations().addPendingSlot(player, cart)) {
            player.sendMessage(ChatColor.RED + "Stacja może mieć maksymalnie 2 wagoniki - użyj /stacja przypisz.");
            return;
        }
        int count = plugin.getStations().pendingCount(player);
        player.sendMessage(ChatColor.GREEN + "Zapamiętano wagonik (" + count + "/2). "
                + (count < 2 ? "Zaznacz kolejny albo od razu /stacja przypisz <bloki/s>."
                : "Teraz /stacja przypisz <bloki/s>, patrząc na przycisk."));
    }

    private void handleAssign(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Użycie: /stacja przypisz <bloki/sekunde>");
            return;
        }
        double speed;
        try {
            speed = Double.parseDouble(args[1].replace(',', '.'));
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "To nie jest liczba: '" + args[1] + "'.");
            return;
        }
        Block target = player.getTargetBlockExact((int) LOOK_RANGE);
        if (target == null || !isTrigger(target.getType())) {
            player.sendMessage(ChatColor.RED + "Musisz patrzeć na przycisk albo dźwignię.");
            return;
        }
        if (!plugin.getStations().createStation(player, target, speed)) {
            player.sendMessage(ChatColor.RED + "Najpierw zaznacz przynajmniej jeden wagonik: /stacja dodaj.");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Stacja gotowa - ten przycisk wystrzeli wagoniki z prędkością "
                + speed + " bloków/s.");
    }

    private void handleRemove(Player player) {
        Block target = player.getTargetBlockExact((int) LOOK_RANGE);
        if (target == null || !isTrigger(target.getType())) {
            player.sendMessage(ChatColor.RED + "Musisz patrzeć na przycisk stacji.");
            return;
        }
        if (plugin.getStations().removeStation(target)) {
            player.sendMessage(ChatColor.YELLOW + "Usunięto stację.");
        } else {
            player.sendMessage(ChatColor.RED + "Ten przycisk nie jest stacją.");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "/stacja dodaj " + ChatColor.GRAY + "- patrz na wagonik, zapamiętaj go (max 2)");
        player.sendMessage(ChatColor.GOLD + "/stacja przypisz <bloki/s> " + ChatColor.GRAY + "- patrz na przycisk, przypisz zapamiętane wagoniki");
        player.sendMessage(ChatColor.GOLD + "/stacja usun " + ChatColor.GRAY + "- patrz na przycisk stacji, usuń ją");
    }

    private static Entity rayTraceEntity(Player player) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(), player.getEyeLocation().getDirection(), LOOK_RANGE,
                entity -> entity instanceof Minecart);
        return result != null ? result.getHitEntity() : null;
    }

    static boolean isTrigger(Material material) {
        String name = material.name();
        return material == Material.LEVER || name.endsWith("_BUTTON") || name.endsWith("_PRESSURE_PLATE");
    }
}
