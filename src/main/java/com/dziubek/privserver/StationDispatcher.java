package com.dziubek.privserver;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Minecart;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Stopniowe odpalenie wagonika ze stacji: prędkość rośnie w kilku etapach (nie skok od razu do
 * pełnej), z odgłosem "klik" na każdym etapie i wybuchem dźwięku+cząsteczek na finalnym starcie -
 * efekt rollercoastera odjeżdżającego ze stacji zamiast szarpnięcia.
 */
final class StationDispatcher {

    private static final int LAUNCH_STAGES = 4;
    private static final long STAGE_INTERVAL_TICKS = 4L;

    private StationDispatcher() {
    }

    static void launch(PrivServerPlugin plugin, Minecart cart, double finalBlocksPerSecond) {
        double finalBlocksPerTick = finalBlocksPerSecond / 20.0;
        if (cart.getMaxSpeed() < finalBlocksPerTick) {
            cart.setMaxSpeed(finalBlocksPerTick);
        }

        Vector velocity = cart.getVelocity();
        Vector direction = velocity.lengthSquared() < 0.0001
                ? RailUtil.fallbackDirection(cart.getLocation().getBlock().getBlockData())
                : velocity;
        Vector finalDirection = direction.normalize();

        new BukkitRunnable() {
            int stage = 0;

            @Override
            public void run() {
                if (!cart.isValid()) {
                    cancel();
                    return;
                }
                stage++;
                double stageSpeed = finalBlocksPerTick * stage / LAUNCH_STAGES;
                cart.setVelocity(finalDirection.clone().multiply(stageSpeed));

                Location loc = cart.getLocation();
                if (stage < LAUNCH_STAGES) {
                    loc.getWorld().playSound(loc, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f + stage * 0.15f);
                } else {
                    loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
                    loc.getWorld().spawnParticle(Particle.CLOUD, loc, 15, 0.3, 0.3, 0.3, 0.05);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, STAGE_INTERVAL_TICKS);
    }
}
