package net.maris.crates.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.maris.crates.MarisCratesPlugin;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Locale;

public final class MarisPlaceholders extends PlaceholderExpansion {
    private final MarisCratesPlugin plugin;

    public MarisPlaceholders(MarisCratesPlugin plugin) {
        this.plugin = plugin;
    }

    public String getIdentifier() {
        return "mariscrates";
    }

    public String getAuthor() {
        return "Maris";
    }

    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public boolean persist() {
        return true;
    }

    public String onPlaceholderRequest(Player p, String params) {
        if (params.equalsIgnoreCase("time_keyall")) return time();
        if (p == null) return "0";
        return NumberFormat.getInstance(Locale.US).format(plugin.storage().get(p.getUniqueId(), params.toLowerCase(Locale.ROOT)));
    }

    private String time() {
        long totalSeconds = Math.max(0, (plugin.nextAutoKeyallMillis() - System.currentTimeMillis() + 999) / 1000);
        long d = totalSeconds / 86400;
        long h = (totalSeconds % 86400) / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(String.format("%02dd ", d));
        if (h > 0 || sb.length() > 0) sb.append(String.format("%02dh ", h));
        if (m > 0 || sb.length() > 0) sb.append(String.format("%02dm ", m));
        sb.append(String.format("%02ds", s));
        return sb.toString().trim();
    }
}
