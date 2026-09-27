package com.dziubek.privserver;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stacja startowa rollercoastera: przycisk/dźwignia powiązana z 1-2 pozycjami, na których stoją
 * wagoniki. Kliknięcie odpala TransportDispatcher.launch() na wagonikach, które akurat tam stoją
 * (więc można je podmieniać między przejazdami bez przepinania stacji). Konfiguracja (dwuetapowa:
 * /stacja dodaj patrząc na wagonik, potem /stacja przypisz patrząc na przycisk) i wyniki
 * zapisywane w config.yml, przetrwają restart.
 */
public class StationManager {

    private final PrivServerPlugin plugin;
    private final Map<String, Station> stations = new HashMap<>();
    private final Map<UUID, List<Location>> pendingSlots = new HashMap<>();

    record Station(List<Location> slots, double launchSpeed) {
    }

    public StationManager(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        stations.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("stations");
        if (section == null) {
            return;
        }
        for (String buttonKey : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(buttonKey);
            if (entry == null) {
                continue;
            }
            double speed = entry.getDouble("speed");
            List<Location> slots = new ArrayList<>();
            for (String slotKey : entry.getStringList("slots")) {
                Location loc = parseLocation(slotKey);
                if (loc != null) {
                    slots.add(loc);
                }
            }
            if (!slots.isEmpty()) {
                stations.put(buttonKey, new Station(slots, speed));
            }
        }
    }

    private void persist() {
        plugin.getConfig().set("stations", null);
        for (Map.Entry<String, Station> entry : stations.entrySet()) {
            String path = "stations." + entry.getKey();
            plugin.getConfig().set(path + ".speed", entry.getValue().launchSpeed());
            List<String> slotKeys = new ArrayList<>();
            for (Location loc : entry.getValue().slots()) {
                slotKeys.add(locationKey(loc));
            }
            plugin.getConfig().set(path + ".slots", slotKeys);
        }
        plugin.saveConfig();
    }

    /** Zapamiętuje pozycję wagonika, na który gracz patrzy, jako kolejny slot startowy (max 2). */
    public boolean addPendingSlot(Player player, Minecart cart) {
        List<Location> slots = pendingSlots.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        if (slots.size() >= 2) {
            return false;
        }
        slots.add(cart.getLocation());
        return true;
    }

    public int pendingCount(Player player) {
        return pendingSlots.getOrDefault(player.getUniqueId(), List.of()).size();
    }

    /** Wiąże zapamiętane sloty gracza z przyciskiem, na który patrzy - zwraca false jeśli nic nie zapamiętał. */
    public boolean createStation(Player player, Block button, double launchSpeed) {
        List<Location> slots = pendingSlots.remove(player.getUniqueId());
        if (slots == null || slots.isEmpty()) {
            return false;
        }
        stations.put(locationKey(button.getLocation()), new Station(slots, launchSpeed));
        persist();
        return true;
    }

    public boolean removeStation(Block button) {
        boolean removed = stations.remove(locationKey(button.getLocation())) != null;
        if (removed) {
            persist();
        }
        return removed;
    }

    public Station getStation(Block button) {
        return stations.get(locationKey(button.getLocation()));
    }

    private static String locationKey(Location location) {
        World world = location.getWorld();
        return (world != null ? world.getName() : "?") + "," + location.getBlockX() + ","
                + location.getBlockY() + "," + location.getBlockZ();
    }

    private static Location parseLocation(String key) {
        String[] parts = key.split(",");
        if (parts.length != 4) {
            return null;
        }
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        try {
            return new Location(world, Integer.parseInt(parts[1]) + 0.5, Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]) + 0.5);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Znajduje wagonik aktualnie stojący na danym slocie startowym (jeśli jest). */
    static Minecart findCartAt(Location slot) {
        for (Entity entity : slot.getWorld().getNearbyEntities(slot, 0.6, 0.6, 0.6)) {
            if (entity instanceof Minecart cart) {
                return cart;
            }
        }
        return null;
    }
}
