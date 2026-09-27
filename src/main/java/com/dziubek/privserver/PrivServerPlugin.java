package com.dziubek.privserver;

import org.bukkit.plugin.java.JavaPlugin;

public class PrivServerPlugin extends JavaPlugin {

    private static final long WAGON_TICK_INTERVAL = 1L;

    private WhitelistManager whitelist;
    private WagonManager wagons;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        whitelist = new WhitelistManager(this);
        whitelist.load();
        wagons = new WagonManager();

        getCommand("bialalista").setExecutor(new WhitelistCommand(this));
        getCommand("zamknij").setExecutor(new CloseServerCommand(this));

        getServer().getPluginManager().registerEvents(new WhitelistListener(this), this);
        getServer().getPluginManager().registerEvents(new MotdListener(this), this);
        getServer().getPluginManager().registerEvents(new WagonListener(this), this);

        getServer().getScheduler().runTaskTimer(this, wagons::tick, WAGON_TICK_INTERVAL, WAGON_TICK_INTERVAL);

        getLogger().info("PrivServer wlaczony - biala lista: "
                + (whitelist.isEnabled() ? "WLACZONA" : "wylaczona") + ", graczy na liscie: "
                + whitelist.getWhitelisted().size());
    }

    public WhitelistManager getWhitelist() {
        return whitelist;
    }

    public WagonManager getWagons() {
        return wagons;
    }
}
