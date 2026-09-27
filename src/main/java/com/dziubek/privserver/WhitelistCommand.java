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
                    ChatUtil.sendUsage(sender, "/bialalista dodaj <gracz>", "dodaje gracza do białej listy");
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
                    ChatUtil.sendUsage(sender, "/bialalista usun <gracz>", "usuwa gracza z białej listy");
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
        ChatUtil.sendHelpLine(sender, "/bialalista dodaj <gracz>", "dodaje gracza do białej listy");
        ChatUtil.sendHelpLine(sender, "/bialalista usun <gracz>", "usuwa gracza z białej listy");
        ChatUtil.sendHelpLine(sender, "/bialalista lista", "pokazuje białą listę");
        ChatUtil.sendHelpLine(sender, "/bialalista wlacz", "włącza białą listę (serwer prywatny)");
        ChatUtil.sendHelpLine(sender, "/bialalista wylacz", "wyłącza białą listę (serwer otwarty)");
    }
}
