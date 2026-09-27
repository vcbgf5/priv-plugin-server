package com.dziubek.privserver;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Łączenie wagoników w pociąg: gracz patrzy na pierwszy wagonik i woła /wagon - "lokomotywa",
 * potem na drugi i znów /wagon - "przyczepa". Przyczepa co tick dostaje prędkość lokomotywy i
 * jest "podciągana" na stały dystans za nią, więc jadą razem jak połączony skład. Jeśli
 * którykolwiek z pary zniknie (zniszczony/rozładowany), para jest automatycznie usuwana.
 */
public class WagonManager {

    private static final double FOLLOW_DISTANCE = 1.2;
    private static final double SNAP_THRESHOLD_SQUARED = 0.3 * 0.3;

    /** przyczepa (follower) -> lokomotywa (leader) */
    private final Map<UUID, UUID> coupled = new HashMap<>();
    /** gracz -> wagonik wybrany jako pierwszy w tej próbie łączenia */
    private final Map<UUID, UUID> pendingSelection = new HashMap<>();

    public void select(Player player, Minecart clicked) {
        UUID clickedId = clicked.getUniqueId();
        UUID pending = pendingSelection.get(player.getUniqueId());

        if (pending == null) {
            pendingSelection.put(player.getUniqueId(), clickedId);
            player.sendMessage("§eZaznaczono lokomotywę. Spójrz na drugi wagonik i wywołaj /wagon, aby go doczepić.");
            return;
        }

        pendingSelection.remove(player.getUniqueId());
        if (pending.equals(clickedId)) {
            player.sendMessage("§7Anulowano zaznaczenie.");
            return;
        }

        coupled.put(clickedId, pending);
        player.sendMessage("§aWagoniki połączone - jadą teraz razem.");
    }

    /** Co tick: przyczepy dostają prędkość lokomotywy i są korygowane na stały dystans za nią. */
    public void tick() {
        Iterator<Map.Entry<UUID, UUID>> it = coupled.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UUID> entry = it.next();
            Entity followerEntity = Bukkit.getEntity(entry.getKey());
            Entity leaderEntity = Bukkit.getEntity(entry.getValue());
            if (!(followerEntity instanceof Minecart follower) || !follower.isValid()
                    || !(leaderEntity instanceof Minecart leader) || !leader.isValid()) {
                it.remove();
                continue;
            }

            Vector leaderVelocity = leader.getVelocity();
            if (leaderVelocity.lengthSquared() > 0.0001) {
                follower.setVelocity(leaderVelocity);
            }

            Vector direction = leaderVelocity.lengthSquared() > 0.0001
                    ? leaderVelocity.clone().normalize()
                    : leader.getLocation().toVector().subtract(follower.getLocation().toVector());
            if (direction.lengthSquared() < 0.0001) {
                continue;
            }
            direction.normalize();

            Location target = leader.getLocation().clone().subtract(direction.multiply(FOLLOW_DISTANCE));
            if (follower.getLocation().distanceSquared(target) > SNAP_THRESHOLD_SQUARED) {
                target.setYaw(follower.getLocation().getYaw());
                target.setPitch(follower.getLocation().getPitch());
                follower.teleport(target);
            }
        }
    }
}
