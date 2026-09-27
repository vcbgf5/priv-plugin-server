package com.dziubek.privserver;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

/** Ustawia MOTD tego serwera na tekst z config.yml (domyślnie "Serwer prywatny - ..."). */
public class MotdListener implements Listener {

    private final PrivServerPlugin plugin;

    public MotdListener(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        event.setMotd(plugin.getWhitelist().getMotd());
    }
}
