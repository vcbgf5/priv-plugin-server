package com.dziubek.privserver;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Wczytuje/zapisuje wlasna biala liste serwera z config.yml - patrz komentarz w pliku. */
public class WhitelistManager {

    private final PrivServerPlugin plugin;
    private boolean enabled = true;
    private final Set<String> whitelisted = new LinkedHashSet<>();
    private String kickMessage = "";
    private String motd = "";
    private boolean closed = false;
    private String closedMotd = "";
    private String closedKickMessage = "";

    public WhitelistManager(PrivServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        enabled = config.getBoolean("enabled", true);

        whitelisted.clear();
        for (String name : config.getStringList("whitelisted")) {
            whitelisted.add(name.toLowerCase(Locale.ROOT));
        }

        kickMessage = colorize(config.getStringList("kick-message"));
        motd = colorize(config.getStringList("motd"));
        closedMotd = colorize(config.getStringList("closed-motd"));
        closedKickMessage = colorize(config.getStringList("closed-kick-message"));
    }

    private static String colorize(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(ChatColor.translateAlternateColorCodes('&', lines.get(i)));
        }
        return sb.toString();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("enabled", enabled);
        plugin.saveConfig();
    }

    public boolean isWhitelisted(String playerName) {
        return whitelisted.contains(playerName.toLowerCase(Locale.ROOT));
    }

    public boolean add(String playerName) {
        boolean added = whitelisted.add(playerName.toLowerCase(Locale.ROOT));
        if (added) {
            persist();
        }
        return added;
    }

    public boolean remove(String playerName) {
        boolean removed = whitelisted.remove(playerName.toLowerCase(Locale.ROOT));
        if (removed) {
            persist();
        }
        return removed;
    }

    public Set<String> getWhitelisted() {
        return whitelisted;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public String getMotd() {
        return closed ? closedMotd : motd;
    }

    public boolean isClosed() {
        return closed;
    }

    /** Przełącza tryb "zamknięty" (poza operatorami nikt nie wejdzie, MOTD pokazuje info o zamknięciu)
     * - stan tylko w pamięci, restart serwera zawsze otwiera go z powrotem. */
    public boolean toggleClosed() {
        closed = !closed;
        return closed;
    }

    public String getClosedKickMessage() {
        return closedKickMessage;
    }

    private void persist() {
        plugin.getConfig().set("whitelisted", List.copyOf(whitelisted));
        plugin.saveConfig();
    }
}
