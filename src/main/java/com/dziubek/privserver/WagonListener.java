package com.dziubek.privserver;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/** Shift+PPM na wagoniku -> zaznacza go do łączenia (patrz WagonManager). */
public class WagonListener implements Listener {

    private final PrivServerPlugin plugin;

    public WagonListener(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        if (!(event.getRightClicked() instanceof Minecart cart)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        plugin.getWagons().select(player, cart);
    }
}
