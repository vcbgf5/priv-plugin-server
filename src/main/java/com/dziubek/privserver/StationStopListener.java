package com.dziubek.privserver;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

/** Wagonik oznaczony tagiem "N" (patrz StationManager.tag()), który wraca na slot startowy
 * DOKŁADNIE o numerze N (np. po okrążeniu pętli), zatrzymuje się tam automatycznie - wagonik "1"
 * zatrzymuje się tylko na slocie 1, wagonik "2" tylko na slocie 2, nie na cudzym miejscu. */
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
        Integer slotNumber = plugin.getStations().getSlotNumber(cart.getLocation().getBlock());
        if (slotNumber == null) {
            return;
        }
        String name = cart.getCustomName();
        if (name == null || !name.endsWith(String.valueOf(slotNumber))) {
            return;
        }
        cart.setVelocity(ZERO);
        cart.setMaxSpeed(VANILLA_MAX_SPEED);
    }
}
