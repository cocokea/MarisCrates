package net.maris.crates.hologram;

import net.maris.crates.MarisCratesPlugin;
import net.maris.crates.crate.Crate;
import net.maris.crates.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class HologramService {
    private final MarisCratesPlugin plugin;
    private final Map<String, Map<UUID, UUID>> displays = new HashMap<>();
    private final Map<UUID, Location> displayLocations = new HashMap<>();

    public HologramService(MarisCratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void refreshAll() {
        removeAll();
        if (!plugin.getConfig().getBoolean("hologram.enabled", true)) return;
        for (Crate crate : plugin.crates().all()) refresh(crate);
    }

    public void refresh(Crate crate) {
        remove(crate);
        if (!plugin.getConfig().getBoolean("hologram.enabled", true) || crate.location() == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (shouldShow(crate, player)) show(crate, player);
        }
    }

    public void refresh(Player player) {
        remove(player);
        if (!plugin.getConfig().getBoolean("hologram.enabled", true)) return;
        for (Crate crate : plugin.crates().all()) {
            if (crate.location() == null) continue;
            if (shouldShow(crate, player)) show(crate, player);
        }
    }

    public void updateVisibility(Player player) {
        if (!plugin.getConfig().getBoolean("hologram.enabled", true)) {
            remove(player);
            return;
        }
        for (Crate crate : plugin.crates().all()) {
            if (crate.location() == null) continue;
            boolean visible = hasDisplay(crate, player);
            boolean shouldShow = shouldShow(crate, player);
            if (shouldShow && !visible) show(crate, player);
            else if (!shouldShow && visible) remove(crate, player);
        }
    }

    public void remove(Crate crate) {
        Map<UUID, UUID> viewers = displays.remove(crate.name());
        if (viewers == null) return;
        for (UUID displayId : viewers.values()) removeDisplay(displayId);
    }

    public void remove(Player player) {
        for (Map<UUID, UUID> viewers : displays.values()) {
            UUID displayId = viewers.remove(player.getUniqueId());
            if (displayId != null) removeDisplay(displayId);
        }
    }

    private void remove(Crate crate, Player player) {
        Map<UUID, UUID> viewers = displays.get(crate.name());
        if (viewers == null) return;
        UUID displayId = viewers.remove(player.getUniqueId());
        if (displayId != null) removeDisplay(displayId);
        if (viewers.isEmpty()) displays.remove(crate.name());
    }

    public void removeAll() {
        for (Map<UUID, UUID> viewers : displays.values()) {
            for (UUID displayId : viewers.values()) removeDisplay(displayId);
        }
        displays.clear();
        displayLocations.clear();
    }

    private boolean hasDisplay(Crate crate, Player player) {
        Map<UUID, UUID> viewers = displays.get(crate.name());
        return viewers != null && viewers.containsKey(player.getUniqueId());
    }

    private boolean shouldShow(Crate crate, Player player) {
        if (crate == null || crate.location() == null || crate.location().getWorld() == null) return false;
        if (player == null || !player.isOnline() || player.getWorld() == null) return false;
        Location loc = hologramLocation(crate);
        if (loc.getWorld() == null || !loc.getWorld().equals(player.getWorld())) return false;
        double maxDistance = plugin.getConfig().getDouble("hologram.view-distance", 30.0D);
        return player.getLocation().distanceSquared(loc) <= maxDistance * maxDistance;
    }

    private void show(Crate crate, Player player) {
        if (!shouldShow(crate, player) || hasDisplay(crate, player)) return;
        Location loc = hologramLocation(crate);
        plugin.runner().at(loc, () -> {
            if (!shouldShow(crate, player) || hasDisplay(crate, player)) return;
            TextDisplay display = (TextDisplay) loc.getWorld().spawnEntity(loc, EntityType.TEXT_DISPLAY);
            display.setPersistent(false);
            display.setVisibleByDefault(false);
            display.setBillboard(Display.Billboard.CENTER);
            display.setSeeThrough(true);
            display.setShadowed(plugin.getConfig().getBoolean("hologram.text-shadow", true));
            display.setDefaultBackground(false);
            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            display.setText(ColorUtil.color(text(crate, player)));
            displays.computeIfAbsent(crate.name(), k -> new HashMap<>()).put(player.getUniqueId(), display.getUniqueId());
            displayLocations.put(display.getUniqueId(), loc);
            plugin.runner().entity(player, () -> {
                if (shouldShow(crate, player)) player.showEntity(plugin, display);
                else remove(crate, player);
            });
        });
    }

    private void removeDisplay(UUID displayId) {
        Location loc = displayLocations.remove(displayId);

        // During server shutdown Canvas/Paper may already have cleared the regionized
        // world data. Removing entities at that point can throw an internal NPE.
        // The server is stopping anyway, so only skip the physical remove in that case.
        if (isServerStopping()) return;

        if (!plugin.isEnabled()) {
            removeEntity(displayId);
            return;
        }
        if (loc != null) plugin.runner().at(loc, () -> removeEntity(displayId));
        else removeEntity(displayId);
    }

    private boolean isServerStopping() {
        try {
            return (boolean) Bukkit.class.getMethod("isStopping").invoke(null);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private void removeEntity(UUID uuid) {
        Entity entity = plugin.getServer().getEntity(uuid);
        if (entity != null) entity.remove();
    }

    private Location hologramLocation(Crate crate) {
        return crate.location().clone().add(0.5, 1.0 + plugin.getConfig().getDouble("hologram.y-offset", 0.3), 0.5);
    }

    private String text(Crate crate, Player player) {
        List<String> lines = plugin.getConfig().getStringList("hologram.lines");
        if (lines.isEmpty()) lines = List.of("%crate_color%%crates% ᴄʀᴀᴛᴇs", "%crate_color%%key% &fkeys", "&fClick to view");
        String crateName = ColorUtil.smallCaps(crate.name());
        String keys = NumberFormat.getInstance(Locale.US).format(plugin.storage().get(player.getUniqueId(), crate.name()));
        return String.join("\n", lines)
                .replace("%crates%", crateName)
                .replace("%key%", keys)
                .replace("%crate_color%", crate.color());
    }
}
