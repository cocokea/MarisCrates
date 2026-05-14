package net.maris.crates.crate;

import net.maris.crates.MarisCratesPlugin;
import net.maris.crates.util.ItemCodec;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class CrateManager {
    private final MarisCratesPlugin plugin;
    private final Map<String, Crate> crates = new HashMap<>();
    private final File folder;
    public CrateManager(MarisCratesPlugin plugin) { this.plugin = plugin; this.folder = new File(plugin.getDataFolder(), "crates"); }
    public void loadAll() {
        crates.clear(); if (!folder.exists()) folder.mkdirs();
        File[] files = folder.listFiles((d, n) -> n.endsWith(".yml")); if (files == null) return;
        for (File file : files) load(file);
    }
    private void load(File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        String name = yml.getString("name", file.getName().replace(".yml", "")).toLowerCase(Locale.ROOT);
        Crate crate = new Crate(name, yml.getString("color", "&f"), readLocation(yml.getConfigurationSection("location")));
        for (Map<?, ?> map : yml.getMapList("items")) {
            Object encoded = map.get("item"); if (!(encoded instanceof String s)) continue;
            ItemStack item = ItemCodec.decode(s);
            boolean giveItem = !map.containsKey("give-item") || Boolean.parseBoolean(String.valueOf(map.get("give-item")));
            List<String> commands = map.get("commands") instanceof List<?> list ? new ArrayList<>(list.stream().map(String::valueOf).toList()) : new ArrayList<>();
            crate.rewards().add(new CrateReward(item, giveItem, commands));
        }
        crates.put(name, crate);
    }
    public Collection<Crate> all() { return crates.values(); }
    public Crate get(String name) { return name == null ? null : crates.get(name.toLowerCase(Locale.ROOT)); }
    public Crate create(String name) throws IOException {
        String id = name.toLowerCase(Locale.ROOT); Crate crate = new Crate(id, "&f", null); crates.put(id, crate); save(crate); return crate;
    }
    public void save(Crate crate) throws IOException {
        File file = new File(folder, crate.name() + ".yml"); YamlConfiguration yml = new YamlConfiguration();
        yml.set("name", crate.name()); yml.set("color", crate.color()); writeLocation(yml, crate.location());
        List<Map<String,Object>> list = new ArrayList<>();
        for (CrateReward reward : crate.rewards()) { Map<String,Object> map = new LinkedHashMap<>(); map.put("item", ItemCodec.encode(reward.item())); map.put("give-item", reward.giveItem()); map.put("commands", new ArrayList<>(reward.commands())); list.add(map); }
        yml.set("items", list); yml.save(file);
    }
    public void saveRewards(Crate crate, List<ItemStack> items) throws IOException {
        crate.rewards().clear(); for (ItemStack item : items) crate.rewards().add(new CrateReward(item.clone(), true, new ArrayList<>())); save(crate);
    }
    private Location readLocation(ConfigurationSection s) {
        if (s == null) return null; World world = Bukkit.getWorld(s.getString("world", "")); if (world == null) return null;
        double x = s.getDouble("x"); double y = s.getDouble("y"); double z = s.getDouble("z");
        if (isLegacyHologramLocation(x) && isLegacyHologramLocation(z)) {
            x -= 0.5; y -= 1.0 + plugin.getConfig().getDouble("hologram.y-offset", 0.3); z -= 0.5;
        }
        return new Location(world, x, y, z, (float)s.getDouble("yaw"), (float)s.getDouble("pitch"));
    }
    private boolean isLegacyHologramLocation(double value) { return Math.abs((value - Math.floor(value)) - 0.5) < 0.0001; }
    private void writeLocation(YamlConfiguration yml, Location l) {
        if (l == null) { yml.set("location", null); return; }
        yml.set("location.world", l.getWorld().getName()); yml.set("location.x", l.getX()); yml.set("location.y", l.getY()); yml.set("location.z", l.getZ()); yml.set("location.yaw", l.getYaw()); yml.set("location.pitch", l.getPitch());
    }
}
