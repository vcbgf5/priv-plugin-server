package com.dziubek.privserver;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Szyny z wlasnym, recznie ustawionym "mocy" (predkosci docelowej w blokach/sekunde) - patrz
 * PoweredRailListener, ktory co ruch wagonika wymusza te predkosc, bez wzgledu na wanilijny
 * limit predkosci wagonika. Pod kazda taka szyna stawiany jest blok green_terracotta jako
 * wizualny znacznik "to jest boostowana szyna". Zapisywane w config.yml, wiec przetrwa restart.
 */
public class PoweredRailManager {

    private final PrivServerPlugin plugin;
    private final Map<String, Double> speeds = new HashMap<>();

    public PoweredRailManager(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        speeds.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("powered-rails");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            speeds.put(key, section.getDouble(key));
        }
    }

    private void persist() {
        plugin.getConfig().set("powered-rails", null);
        for (Map.Entry<String, Double> entry : speeds.entrySet()) {
            plugin.getConfig().set("powered-rails." + entry.getKey(), entry.getValue());
        }
        plugin.saveConfig();
    }

    public void setPower(Block rail, double blocksPerSecond) {
        speeds.put(key(rail.getLocation()), blocksPerSecond);
        rail.getRelative(BlockFace.DOWN).setType(Material.GREEN_TERRACOTTA);
        persist();
    }

    public void clearPower(Block rail) {
        speeds.remove(key(rail.getLocation()));
        persist();
    }

    /** Zwraca ustawiona predkosc (bloki/sekunde) dla tego bloku, albo null jesli nie ustawiona. */
    public Double getPower(Block block) {
        return speeds.get(key(block.getLocation()));
    }

    private static String key(Location location) {
        World world = location.getWorld();
        return (world != null ? world.getName() : "?") + "," + location.getBlockX() + ","
                + location.getBlockY() + "," + location.getBlockZ();
    }
}
