package com.dziubek.privserver;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/** /zamknij - przełącza tryb zamknięcia serwera (poza operatorami nikt nie wejdzie, MOTD się zmienia). */
public class CloseServerCommand implements CommandExecutor {

    private final PrivServerPlugin plugin;

    public CloseServerCommand(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean nowClosed = plugin.getWhitelist().toggleClosed();
        if (nowClosed) {
            sender.sendMessage(ChatColor.RED + "Serwer zamknięty - MOTD zmieniony, nikt poza operatorami nie wejdzie.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Serwer otwarty ponownie.");
        }
        return true;
    }
}
