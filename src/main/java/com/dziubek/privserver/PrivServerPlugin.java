package com.dziubek.privserver;

import org.bukkit.plugin.java.JavaPlugin;

public class PrivServerPlugin extends JavaPlugin {

    private static final long WAGON_TICK_INTERVAL = 1L;

    private WhitelistManager whitelist;
    private WagonManager wagons;
    private PoweredRailManager poweredRails;
    private StationManager stations;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        whitelist = new WhitelistManager(this);
        whitelist.load();
        wagons = new WagonManager();
        poweredRails = new PoweredRailManager(this);
        poweredRails.load();
        stations = new StationManager(this);
        stations.load();

        getCommand("bialalista").setExecutor(new WhitelistCommand(this));
        getCommand("zamknij").setExecutor(new CloseServerCommand(this));
        getCommand("setpower").setExecutor(new PoweredRailCommand(this));
        getCommand("wagon").setExecutor(new WagonCommand(this));
        getCommand("stacja").setExecutor(new StationCommand(this));

        getServer().getPluginManager().registerEvents(new WhitelistListener(this), this);
        getServer().getPluginManager().registerEvents(new MotdListener(this), this);
        getServer().getPluginManager().registerEvents(new PoweredRailListener(this), this);
        getServer().getPluginManager().registerEvents(new StationListener(this), this);
        getServer().getPluginManager().registerEvents(new StationStopListener(this), this);

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

    public PoweredRailManager getPoweredRails() {
        return poweredRails;
    }

    public StationManager getStations() {
        return stations;
    }
}
