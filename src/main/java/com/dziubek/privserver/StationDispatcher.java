package com.dziubek.privserver;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Minecart;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stopniowe odpalenie wagonika ze stacji: prędkość rośnie w kilku etapach (nie skok od razu do
 * pełnej), z odgłosem "klik" na każdym etapie i wybuchem dźwięku+cząsteczek na finalnym starcie -
 * efekt rollercoastera odjeżdżającego ze stacji zamiast szarpnięcia. Zaraz po starcie wagonik
 * jest jeszcze w promieniu naprowadzania własnego slotu (patrz StationStopListener) - bez okresu
 * ochronnego to naprowadzanie ciągnęłoby go z powrotem zamiast pozwolić mu odjechać.
 */
final class StationDispatcher {

    private static final int LAUNCH_STAGES = 4;
    private static final long STAGE_INTERVAL_TICKS = 4L;
    private static final long GRACE_PERIOD_MS = 5000L;

    private static final Map<UUID, Long> dispatchedAt = new HashMap<>();

    private StationDispatcher() {
    }

    /** Czy ten wagonik odjechał ze stacji w ciągu ostatnich GRACE_PERIOD_MS - jeśli tak,
     * StationStopListener nie powinien go jeszcze próbować ściągać z powrotem na slot. */
    static boolean isInGracePeriod(UUID cartId) {
        Long time = dispatchedAt.get(cartId);
        return time != null && System.currentTimeMillis() - time < GRACE_PERIOD_MS;
    }

    static void launch(PrivServerPlugin plugin, Minecart cart, double finalBlocksPerSecond) {
        dispatchedAt.put(cart.getUniqueId(), System.currentTimeMillis());
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
