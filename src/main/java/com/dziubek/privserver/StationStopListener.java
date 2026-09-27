package com.dziubek.privserver;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

/**
 * Naprowadza każdy oznaczony wagonik ("1", "2", ...) na jego WŁASNY przypisany slot (patrz
 * StationManager.getAssignedSlot()) - nie na jakikolwiek inny slot, obok którego akurat
 * przejeżdża. Gdy jest blisko (w promieniu GUIDE_RADIUS, np. wagonik "1" stojący koło slotu 2),
 * jest delikatnie "dociągany" w stronę własnego miejsca; gdy jest już bardzo blisko, zatrzymuje
 * się tam dokładnie. Pomija wagoniki tuż po odpaleniu ze stacji (patrz
 * StationDispatcher.isInGracePeriod()), żeby naprowadzanie nie ciągnęło ich z powrotem zanim
 * zdążą odjechać.
 */
public class StationStopListener implements Listener {

    private static final double GUIDE_RADIUS = 1.5;
    private static final double SNAP_DISTANCE = 0.3;
    private static final double GUIDE_SPEED_PER_TICK = 0.15;
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
        if (StationDispatcher.isInGracePeriod(cart.getUniqueId())) {
            return;
        }
        Location slot = plugin.getStations().getAssignedSlot(cart);
        if (slot == null) {
            return;
        }

        double distance = cart.getLocation().distance(slot);
        if (distance > GUIDE_RADIUS) {
            return;
        }

        if (distance <= SNAP_DISTANCE) {
            cart.teleport(slot);
            cart.setVelocity(ZERO);
            cart.setMaxSpeed(VANILLA_MAX_SPEED);
            return;
        }

        Vector toward = slot.toVector().subtract(cart.getLocation().toVector()).normalize().multiply(GUIDE_SPEED_PER_TICK);
        cart.setVelocity(toward);
    }
}
