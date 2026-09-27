package com.dziubek.privserver;

import net.kyori.adventure.text.format.NamedTextColor;
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
 * /stacja wagonik wroc <1|2> - patrzysz na przycisk, teleportuje przypisany wagonik z powrotem na miejsce i zatrzymuje go.
 * /stacja wagonik usun <1|2> - patrzysz na przycisk, usuwa (despawnuje) przypisany wagonik.
 * /stacja wagonik reset - patrzysz na przycisk, usuwa oba stare wagoniki i stawia nowe na wyznaczonych miejscach.
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
            case "wagonik" -> handleWagonik(player, args);
            default -> sendHelp(player);
        }
        return true;
    }

    private void handleWagonik(Player player, String[] args) {
        if (args.length < 2) {
            sendWagonikUsage(player);
            return;
        }

        Block target = player.getTargetBlockExact((int) LOOK_RANGE);
        if (target == null || !isTrigger(target.getType())) {
            player.sendMessage(ChatColor.RED + "Musisz patrzeć na przycisk stacji.");
            return;
        }
        StationManager.Station station = plugin.getStations().getStation(target);
        if (station == null) {
            player.sendMessage(ChatColor.RED + "Ten przycisk nie jest stacją.");
            return;
        }

        String sub = args[1].toLowerCase();
        if (sub.equals("reset")) {
            plugin.getStations().resetCarts(station);
            player.sendMessage(ChatColor.GREEN + "Zresetowano wagoniki tej stacji.");
            return;
        }

        if (args.length < 3) {
            ChatUtil.sendUsage(player, "/stacja wagonik " + sub + " <1|2>", "podaj numer wagonika (1 albo 2)");
            return;
        }
        int slotIndex = parseSlotIndex(args[2], station.slots().size());
        if (slotIndex < 0) {
            player.sendMessage(ChatColor.RED + "Nieprawidłowy numer - ta stacja ma " + station.slots().size() + " miejsc.");
            return;
        }

        switch (sub) {
            case "wroc" -> {
                if (plugin.getStations().returnCart(station, slotIndex)) {
                    player.sendMessage(ChatColor.GREEN + "Wagonik " + args[2] + " wrócił na miejsce.");
                } else {
                    player.sendMessage(ChatColor.RED + "Nie ma przypisanego wagonika " + args[2] + " (może już nie istnieje).");
                }
            }
            case "usun" -> {
                if (plugin.getStations().removeCart(station, slotIndex)) {
                    player.sendMessage(ChatColor.YELLOW + "Usunięto wagonik " + args[2] + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "Nie ma przypisanego wagonika " + args[2] + ".");
                }
            }
            default -> sendWagonikUsage(player);
        }
    }

    private void sendWagonikUsage(Player player) {
        ChatUtil.sendHelpLine(player, "/stacja wagonik wroc <1|2>", "teleportuje wagonik z powrotem na miejsce");
        ChatUtil.sendHelpLine(player, "/stacja wagonik usun <1|2>", "usuwa przypisany wagonik");
        ChatUtil.sendHelpLine(player, "/stacja wagonik reset", "usuwa oba stare, stawia nowe na miejscach");
    }

    private static int parseSlotIndex(String raw, int slotCount) {
        try {
            int number = Integer.parseInt(raw);
            return number >= 1 && number <= slotCount ? number - 1 : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void handleAdd(Player player) {
        Entity hit = rayTraceEntity(player);
        if (!(hit instanceof Minecart cart)) {
            player.sendMessage(ChatColor.RED + "Musisz patrzeć na wagonik.");
            return;
        }
        if (!plugin.getStations().addPendingSlot(player, cart)) {
            player.sendMessage(ChatUtil.sentenceWithCommand("Stacja może mieć maksymalnie 2 wagoniki - użyj ",
                    "/stacja przypisz <bloki/s>", "patrz na przycisk, przypisz zapamiętane wagoniki", ".",
                    NamedTextColor.RED));
            return;
        }
        int count = plugin.getStations().pendingCount(player);
        String suffix = count < 2 ? "" : ", patrząc na przycisk.";
        String prefix = "Zapamiętano wagonik (" + count + "/2). "
                + (count < 2 ? "Zaznacz kolejny albo od razu " : "Teraz ");
        player.sendMessage(ChatUtil.sentenceWithCommand(prefix, "/stacja przypisz <bloki/s>",
                "patrz na przycisk, przypisz zapamiętane wagoniki", suffix, NamedTextColor.GREEN));
    }

    private void handleAssign(Player player, String[] args) {
        if (args.length < 2) {
            ChatUtil.sendUsage(player, "/stacja przypisz <bloki/sekunde>", "patrz na przycisk, przypisz zapamiętane wagoniki");
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
            player.sendMessage(ChatUtil.sentenceWithCommand("Najpierw zaznacz przynajmniej jeden wagonik: ",
                    "/stacja dodaj", "patrz na wagonik, zapamiętaj go (max 2)", ".", NamedTextColor.RED));
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
        ChatUtil.sendHelpLine(player, "/stacja dodaj", "patrz na wagonik, zapamiętaj go (max 2)");
        ChatUtil.sendHelpLine(player, "/stacja przypisz <bloki/s>", "patrz na przycisk, przypisz zapamiętane wagoniki");
        ChatUtil.sendHelpLine(player, "/stacja usun", "patrz na przycisk stacji, usuń ją");
        ChatUtil.sendHelpLine(player, "/stacja wagonik wroc <1|2>", "teleportuje wagonik z powrotem na miejsce");
        ChatUtil.sendHelpLine(player, "/stacja wagonik usun <1|2>", "usuwa przypisany wagonik");
        ChatUtil.sendHelpLine(player, "/stacja wagonik reset", "usuwa oba stare, stawia nowe na miejscach");
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
