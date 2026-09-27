package com.dziubek.privserver;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stacja startowa rollercoastera: przycisk/dźwignia powiązana z 1-2 pozycjami ("Miejsce 1"/"Miejsce
 * 2" - podpisane hologramem TextDisplay), na których stoją wagoniki. Kliknięcie odpala
 * StationDispatcher.launch() na wagonikach aktualnie tam stojących. Każdy wagonik przypisany do
 * slotu dostaje widoczną nazwę "1"/"2" i jest śledzony po UUID (przetrwa /stacja wagonik wroc/usun
 * nawet jeśli odjechał kawałek dalej). Konfiguracja i stan zapisywane w config.yml.
 */
public class StationManager {

    private final PrivServerPlugin plugin;
    private final Map<String, Station> stations = new HashMap<>();
    private final Map<UUID, List<Location>> pendingSlots = new HashMap<>();

    record Station(List<Location> slots, double launchSpeed, List<UUID> cartIds) {
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
            if (slots.isEmpty()) {
                continue;
            }
            List<UUID> cartIds = new ArrayList<>();
            List<String> savedIds = entry.getStringList("cart-ids");
            for (int i = 0; i < slots.size(); i++) {
                String raw = i < savedIds.size() ? savedIds.get(i) : "none";
                cartIds.add("none".equals(raw) ? null : UUID.fromString(raw));
            }
            stations.put(buttonKey, new Station(slots, speed, cartIds));
        }
    }

    private void persist() {
        plugin.getConfig().set("stations", null);
        for (Map.Entry<String, Station> entry : stations.entrySet()) {
            String path = "stations." + entry.getKey();
            Station station = entry.getValue();
            plugin.getConfig().set(path + ".speed", station.launchSpeed());
            List<String> slotKeys = new ArrayList<>();
            for (Location loc : station.slots()) {
                slotKeys.add(locationKey(loc));
            }
            plugin.getConfig().set(path + ".slots", slotKeys);
            List<String> cartIdStrings = new ArrayList<>();
            for (UUID id : station.cartIds()) {
                cartIdStrings.add(id == null ? "none" : id.toString());
            }
            plugin.getConfig().set(path + ".cart-ids", cartIdStrings);
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
        List<UUID> cartIds = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            Location slot = slots.get(i);
            spawnLabel(slot, i + 1);
            Minecart existing = findCartAt(slot);
            if (existing != null) {
                tag(existing, i + 1);
                cartIds.add(existing.getUniqueId());
            } else {
                cartIds.add(null);
            }
        }
        stations.put(locationKey(button.getLocation()), new Station(slots, launchSpeed, cartIds));
        persist();
        return true;
    }

    public boolean removeStation(Block button) {
        Station station = stations.remove(locationKey(button.getLocation()));
        if (station == null) {
            return false;
        }
        for (Location slot : station.slots()) {
            removeLabelsAt(slot);
        }
        persist();
        return true;
    }

    public Station getStation(Block button) {
        return stations.get(locationKey(button.getLocation()));
    }

    /** Wagonik faktycznie przypisany do tego slotu (po UUID, nawet jeśli odjechał) - albo null. */
    public Minecart getTrackedCart(Station station, int slotIndex) {
        UUID id = station.cartIds().get(slotIndex);
        if (id == null) {
            return null;
        }
        Entity entity = Bukkit.getEntity(id);
        return entity instanceof Minecart cart && cart.isValid() ? cart : null;
    }

    /** Teleportuje przypisany wagonik z powrotem na slot i zatrzymuje go tam. */
    public boolean returnCart(Station station, int slotIndex) {
        Minecart cart = getTrackedCart(station, slotIndex);
        if (cart == null) {
            return false;
        }
        Location slot = station.slots().get(slotIndex);
        cart.teleport(slot);
        cart.setVelocity(new Vector(0, 0, 0));
        return true;
    }

    /** Usuwa (despawnuje) wagonik przypisany do slotu. */
    public boolean removeCart(Station station, int slotIndex) {
        Minecart cart = getTrackedCart(station, slotIndex);
        if (cart == null) {
            return false;
        }
        cart.remove();
        station.cartIds().set(slotIndex, null);
        persist();
        return true;
    }

    /** Usuwa oba stare wagoniki (jeśli są) i stawia nowe na wyznaczonych miejscach. */
    public void resetCarts(Station station) {
        for (int i = 0; i < station.slots().size(); i++) {
            Minecart old = getTrackedCart(station, i);
            if (old != null) {
                old.remove();
            }
            Location slot = station.slots().get(i);
            Minecart fresh = slot.getWorld().spawn(slot, Minecart.class);
            tag(fresh, i + 1);
            station.cartIds().set(i, fresh.getUniqueId());
        }
        persist();
    }

    private static void tag(Minecart cart, int number) {
        cart.setCustomName(ChatColor.YELLOW + String.valueOf(number));
        cart.setCustomNameVisible(true);
    }

    private static void spawnLabel(Location slot, int number) {
        removeLabelsAt(slot);
        Location labelLoc = slot.clone().add(0, 1.2, 0);
        labelLoc.getWorld().spawn(labelLoc, TextDisplay.class, label -> {
            label.setText(ChatColor.GOLD + "Miejsce " + number);
            label.setBillboard(Display.Billboard.CENTER);
            label.setPersistent(false);
        });
    }

    private static void removeLabelsAt(Location slot) {
        Location labelLoc = slot.clone().add(0, 1.2, 0);
        for (Entity entity : labelLoc.getWorld().getNearbyEntities(labelLoc, 0.6, 0.6, 0.6)) {
            if (entity instanceof TextDisplay) {
                entity.remove();
            }
        }
    }

    /** Numer (1, 2, ...) slotu, jeśli ten blok jest jakimś slotem startowym - albo null. Używane
     * do auto-zatrzymywania wracających wagoników TYLKO na ich WŁASNYM slocie (patrz
     * StationStopListener) - wagonik "1" ma się zatrzymać na slocie 1, nie na slocie 2. */
    public Integer getSlotNumber(Block block) {
        String key = locationKey(block.getLocation());
        for (Station station : stations.values()) {
            List<Location> slots = station.slots();
            for (int i = 0; i < slots.size(); i++) {
                if (locationKey(slots.get(i)).equals(key)) {
                    return i + 1;
                }
            }
        }
        return null;
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
