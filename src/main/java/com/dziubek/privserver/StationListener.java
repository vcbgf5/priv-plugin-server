package com.dziubek.privserver;

import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/** Kliknięcie (albo najście na płytę) przycisku stacji odpala StationDispatcher na wagonikach
 * aktualnie stojących na jej slotach startowych - patrz StationManager. */
public class StationListener implements Listener {

    private final PrivServerPlugin plugin;

    public StationListener(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || !StationCommand.isTrigger(block.getType())) {
            return;
        }

        StationManager.Station station = plugin.getStations().getStation(block);
        if (station == null) {
            return;
        }

        for (var slot : station.slots()) {
            Minecart cart = StationManager.findCartAt(slot);
            if (cart != null) {
                StationDispatcher.launch(plugin, cart, station.launchSpeed());
            }
        }
    }
}
