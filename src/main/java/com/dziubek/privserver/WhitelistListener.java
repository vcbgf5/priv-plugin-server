package com.dziubek.privserver;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/** Odrzuca graczy spoza własnej białej listy (patrz WhitelistManager) zanim w ogóle wejdą. */
public class WhitelistListener implements Listener {

    private final PrivServerPlugin plugin;

    public WhitelistListener(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        WhitelistManager whitelist = plugin.getWhitelist();
        if (!whitelist.isEnabled()) {
            return;
        }
        if (Bukkit.getOfflinePlayer(event.getUniqueId()).isOp()) {
            return;
        }
        if (whitelist.isWhitelisted(event.getName())) {
            return;
        }
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, whitelist.getKickMessage());
    }
}
