package com.dziubek.privserver;

import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

/** Co tick (na każdym wagoniku, nawet stojącym w miejscu) sprawdza, czy stoi na szynie ze sztywną
 * prędkością (patrz PoweredRailManager) i jeśli tak, wymusza tę prędkość - nawet ponad wanilijny
 * limit. VehicleMoveEvent NIE nadaje się do tego - nie odpala się wcale, dopóki wagonik już się
 * nie porusza, więc nigdy by nie "odpalił" stojącego wagonika z miejsca. */
public class PoweredRailListener implements Listener {

    private final PrivServerPlugin plugin;

    public PoweredRailListener(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUpdate(VehicleUpdateEvent event) {
        if (!(event.getVehicle() instanceof Minecart cart)) {
            return;
        }
        Block block = cart.getLocation().getBlock();
        Double blocksPerSecond = plugin.getPoweredRails().getPower(block);
        if (blocksPerSecond == null) {
            return;
        }

        double blocksPerTick = blocksPerSecond / 20.0;

        Vector direction = cart.getVelocity();
        if (direction.lengthSquared() < 0.0001) {
            direction = RailUtil.fallbackDirection(block.getBlockData());
        }
        direction.normalize().multiply(blocksPerTick);

        // Minecraft sam przycina predkosc wagonika do jego wlasnego maxSpeed (domyslnie 0.4/tick =
        // 8 blokow/s) na koncu kazdego ticku, wiec bez podniesienia tego limitu setVelocity()
        // ponizej i tak zostalby cofniety do wanilijnego maksimum.
        if (cart.getMaxSpeed() < blocksPerTick) {
            cart.setMaxSpeed(blocksPerTick);
        }
        cart.setVelocity(direction);
    }
}
