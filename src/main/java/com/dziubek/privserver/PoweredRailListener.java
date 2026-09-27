package com.dziubek.privserver;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

/** Na każdym ruchu wagonika sprawdza, czy stoi na szynie ze sztywną prędkością (patrz
 * PoweredRailManager) i jeśli tak, wymusza tę prędkość - nawet ponad wanilijny limit. */
public class PoweredRailListener implements Listener {

    private final PrivServerPlugin plugin;

    public PoweredRailListener(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart cart)) {
            return;
        }
        Block block = event.getTo().getBlock();
        Double blocksPerSecond = plugin.getPoweredRails().getPower(block);
        if (blocksPerSecond == null) {
            return;
        }

        Vector direction = cart.getVelocity();
        if (direction.lengthSquared() < 0.0001) {
            direction = fallbackDirection(block.getBlockData());
        }
        direction.normalize().multiply(blocksPerSecond / 20.0);
        cart.setVelocity(direction);
    }

    private static Vector fallbackDirection(BlockData data) {
        if (!(data instanceof Rail rail)) {
            return new Vector(1, 0, 0);
        }
        return switch (rail.getShape()) {
            case NORTH_SOUTH -> new Vector(0, 0, 1);
            case EAST_WEST -> new Vector(1, 0, 0);
            case ASCENDING_EAST -> new Vector(1, 0.5, 0);
            case ASCENDING_WEST -> new Vector(-1, 0.5, 0);
            case ASCENDING_NORTH -> new Vector(0, 0.5, -1);
            case ASCENDING_SOUTH -> new Vector(0, 0.5, 1);
            case SOUTH_EAST -> new Vector(1, 0, 1);
            case SOUTH_WEST -> new Vector(-1, 0, 1);
            case NORTH_WEST -> new Vector(-1, 0, -1);
            case NORTH_EAST -> new Vector(1, 0, -1);
        };
    }
}
