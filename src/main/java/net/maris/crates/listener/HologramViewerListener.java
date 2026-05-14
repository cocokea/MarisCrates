package net.maris.crates.listener;

import net.maris.crates.MarisCratesPlugin;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class HologramViewerListener implements Listener {
    private final MarisCratesPlugin plugin;

    public HologramViewerListener(MarisCratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        plugin.runner().entity(event.getPlayer(), () -> plugin.holograms().refresh(event.getPlayer()));
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        plugin.holograms().remove(event.getPlayer());
    }

    @EventHandler
    public void teleport(PlayerTeleportEvent event) {
        plugin.runner().laterGlobal(() -> plugin.runner().entity(event.getPlayer(), () -> plugin.holograms().refresh(event.getPlayer())), 5L);
    }

    @EventHandler
    public void changedWorld(PlayerChangedWorldEvent event) {
        plugin.runner().laterGlobal(() -> plugin.runner().entity(event.getPlayer(), () -> plugin.holograms().refresh(event.getPlayer())), 5L);
    }

    @EventHandler
    public void move(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ() && from.getWorld().equals(to.getWorld())) return;
        plugin.holograms().updateVisibility(event.getPlayer());
    }
}
