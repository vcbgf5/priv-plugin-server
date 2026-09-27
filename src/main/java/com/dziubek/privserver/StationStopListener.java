package com.dziubek.privserver;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

/** Wagonik wracający na slot startowy jakiejkolwiek stacji (np. po okrążeniu pętli) zatrzymuje
 * się tam automatycznie - zamiast przejeżdżać dalej albo zderzać się z kolejnym wagonikiem. */
public class StationStopListener implements Listener {

    private static final Vector ZERO = new Vector(0, 0, 0);
    private static final double VANILLA_MAX_SPEED = 0.4;

    private final PrivServerPlugin plugin;

    public StationStopListener(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUpdate(VehicleUpdateEvent event) {
        if (!(event.getVehicle() instanceof Minecart cart)) {
            return;
        }
        if (cart.getVelocity().lengthSquared() < 0.0001) {
            return;
        }
        if (!plugin.getStations().isSlotLocation(cart.getLocation().getBlock())) {
            return;
        }
        cart.setVelocity(ZERO);
        cart.setMaxSpeed(VANILLA_MAX_SPEED);
    }
}
