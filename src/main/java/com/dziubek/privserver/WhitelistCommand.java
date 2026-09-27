package com.dziubek.privserver;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/** /bialalista dodaj|usun|lista|wlacz|wylacz - zarządzanie własną białą listą serwera. */
public class WhitelistCommand implements CommandExecutor {

    private final PrivServerPlugin plugin;

    public WhitelistCommand(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        WhitelistManager whitelist = plugin.getWhitelist();
        switch (args[0].toLowerCase()) {
            case "dodaj" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Użycie: /bialalista dodaj <gracz>");
                    return true;
                }
                if (whitelist.add(args[1])) {
                    sender.sendMessage(ChatColor.GREEN + "Dodano '" + args[1] + "' do białej listy.");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "'" + args[1] + "' już jest na białej liście.");
                }
            }
            case "usun" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Użycie: /bialalista usun <gracz>");
                    return true;
                }
                if (whitelist.remove(args[1])) {
                    sender.sendMessage(ChatColor.GREEN + "Usunięto '" + args[1] + "' z białej listy.");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "'" + args[1] + "' nie było na białej liście.");
                }
            }
            case "lista" -> {
                if (whitelist.getWhitelisted().isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "Biała lista jest pusta.");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "Biała lista (" + whitelist.getWhitelisted().size() + "): "
                            + ChatColor.WHITE + String.join(", ", whitelist.getWhitelisted()));
                }
            }
            case "wlacz" -> {
                whitelist.setEnabled(true);
                sender.sendMessage(ChatColor.GREEN + "Biała lista włączona - serwer jest teraz prywatny.");
            }
            case "wylacz" -> {
                whitelist.setEnabled(false);
                sender.sendMessage(ChatColor.YELLOW + "Biała lista wyłączona - serwer jest teraz otwarty dla wszystkich.");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "/bialalista dodaj <gracz> " + ChatColor.GRAY + "- dodaje gracza do białej listy");
        sender.sendMessage(ChatColor.GOLD + "/bialalista usun <gracz> " + ChatColor.GRAY + "- usuwa gracza z białej listy");
        sender.sendMessage(ChatColor.GOLD + "/bialalista lista " + ChatColor.GRAY + "- pokazuje białą listę");
        sender.sendMessage(ChatColor.GOLD + "/bialalista wlacz " + ChatColor.GRAY + "- włącza białą listę (serwer prywatny)");
        sender.sendMessage(ChatColor.GOLD + "/bialalista wylacz " + ChatColor.GRAY + "- wyłącza białą listę (serwer otwarty)");
    }
}
