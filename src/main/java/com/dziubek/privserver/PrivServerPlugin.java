package com.dziubek.privserver;

import org.bukkit.plugin.java.JavaPlugin;

public class PrivServerPlugin extends JavaPlugin {

    private WhitelistManager whitelist;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        whitelist = new WhitelistManager(this);
        whitelist.load();

        getCommand("bialalista").setExecutor(new WhitelistCommand(this));

        getServer().getPluginManager().registerEvents(new WhitelistListener(this), this);
        getServer().getPluginManager().registerEvents(new MotdListener(this), this);

        getLogger().info("PrivServer wlaczony - biala lista: "
                + (whitelist.isEnabled() ? "WLACZONA" : "wylaczona") + ", graczy na liscie: "
                + whitelist.getWhitelisted().size());
    }

    public WhitelistManager getWhitelist() {
        return whitelist;
    }
}
