package net.maris.crates.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public final class TaskRunner {
    private final Plugin plugin;
    private final boolean folia;
    public TaskRunner(Plugin plugin) { this.plugin = plugin; this.folia = hasFolia(); }
    private boolean hasFolia() { try { Class.forName("io.papermc.paper.threadedregions.RegionizedServer"); return true; } catch (ClassNotFoundException e) { return false; } }
    public void async(Runnable r) {
        if (!folia) { Bukkit.getScheduler().runTaskAsynchronously(plugin, r); return; }
        try {
            Object scheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Method runNow = scheduler.getClass().getMethod("runNow", Plugin.class, java.util.function.Consumer.class);
            runNow.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> r.run());
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
    public void global(Runnable r) {
        if (!folia) { Bukkit.getScheduler().runTask(plugin, r); return; }
        try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method run = scheduler.getClass().getMethod("run", Plugin.class, java.util.function.Consumer.class);
            run.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> r.run());
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
    public void laterGlobal(Runnable r, long ticks) {
        if (!folia) { Bukkit.getScheduler().runTaskLater(plugin, r, ticks); return; }
        try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method runDelayed = scheduler.getClass().getMethod("runDelayed", Plugin.class, java.util.function.Consumer.class, long.class);
            runDelayed.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> r.run(), ticks);
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
    public void at(Location loc, Runnable r) {
        if (!folia) { Bukkit.getScheduler().runTask(plugin, r); return; }
        try {
            Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            Method run = scheduler.getClass().getMethod("run", Plugin.class, org.bukkit.Location.class, java.util.function.Consumer.class);
            run.invoke(scheduler, plugin, loc, (java.util.function.Consumer<Object>) task -> r.run());
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
    public void entity(Entity entity, Runnable r) {
        if (!folia) { Bukkit.getScheduler().runTask(plugin, r); return; }
        try {
            Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            Method run = scheduler.getClass().getMethod("run", Plugin.class, java.util.function.Consumer.class, Runnable.class);
            run.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> r.run(), null);
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
    public void repeatAsync(Runnable r, long initialTicks, long periodTicks) {
        if (!folia) { Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, r, initialTicks, periodTicks); return; }
        try {
            Object scheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Method runAtFixedRate = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, java.util.function.Consumer.class, long.class, long.class, TimeUnit.class);
            runAtFixedRate.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> r.run(), initialTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS);
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
}
