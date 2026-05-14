package net.maris.crates;

import net.maris.crates.command.CratesCommand;
import net.maris.crates.config.Messages;
import net.maris.crates.crate.CrateManager;
import net.maris.crates.data.KeyStorage;
import net.maris.crates.gui.GuiListener;
import net.maris.crates.hologram.HologramService;
import net.maris.crates.hook.MarisPlaceholders;
import net.maris.crates.listener.CrateClickListener;
import net.maris.crates.listener.HologramViewerListener;
import net.maris.crates.scheduler.TaskRunner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MarisCratesPlugin extends JavaPlugin {
    private CrateManager crates; private KeyStorage storage; private Messages messages; private TaskRunner runner; private HologramService holograms; private long nextAutoKeyallMillis;
    @Override public void onEnable() {
        saveDefaultConfig(); saveResourceIfMissing("message.yml"); saveResourceIfMissing("guis.yml");
        runner = new TaskRunner(this); messages = new Messages(this); crates = new CrateManager(this); crates.loadAll(); holograms = new HologramService(this); storage = new KeyStorage(this);
        try { storage.init(); } catch (Exception e) { getLogger().severe("Cannot initialize storage: " + e.getMessage()); Bukkit.getPluginManager().disablePlugin(this); return; }
        var cmd = new CratesCommand(this); getCommand("crates").setExecutor(cmd); getCommand("crates").setTabCompleter(cmd);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this); Bukkit.getPluginManager().registerEvents(new CrateClickListener(this), this); Bukkit.getPluginManager().registerEvents(new HologramViewerListener(this), this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) new MarisPlaceholders(this).register();
        holograms.refreshAll();
        startAutoKeyall();
    }
    private void saveResourceIfMissing(String path) {
        if (!new File(getDataFolder(), path).exists()) saveResource(path, false);
    }
    @Override public void onDisable() { if (holograms != null) holograms.removeAll(); if (storage != null) storage.close(); }
    public void reloadPlugin() {
        reloadConfig();
        messages = new Messages(this);
        crates.loadAll();
        holograms.refreshAll();
    }
    private void startAutoKeyall() {
        if (!getConfig().getBoolean("auto-keyall.enabled", true)) return;
        long periodTicks = Math.max(1, getConfig().getLong("auto-keyall.interval-minutes", 60)) * 60L * 20L;
        nextAutoKeyallMillis = System.currentTimeMillis() + periodTicks * 50L;
        runner.repeatAsync(() -> runner.global(() -> {
            reloadConfig();
            new CratesCommand(this).keyall(getConfig().getString("auto-keyall.crate", "common"), getConfig().getInt("auto-keyall.amount", 1), true);
            nextAutoKeyallMillis = System.currentTimeMillis() + periodTicks * 50L;
        }), periodTicks, periodTicks);
    }
    public CrateManager crates() { return crates; } public KeyStorage storage() { return storage; } public Messages messages() { return messages; } public TaskRunner runner() { return runner; } public HologramService holograms() { return holograms; } public long nextAutoKeyallMillis() { return nextAutoKeyallMillis; }

}
