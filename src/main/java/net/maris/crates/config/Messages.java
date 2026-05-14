package net.maris.crates.config;

import net.maris.crates.MarisCratesPlugin;
import net.maris.crates.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.List;
import java.util.Map;

public final class Messages {
    private final YamlConfiguration yml;
    public Messages(MarisCratesPlugin plugin) { this.yml = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "message.yml")); }
    public String raw(String path) { return yml.getString(path, ""); }
    public String msg(String path, Map<String,String> vars) { String s = raw(path); if (s == null || s.isEmpty()) return ""; for (var e : vars.entrySet()) s = s.replace(e.getKey(), e.getValue()); return ColorUtil.color(s); }
    public void send(CommandSender sender, String path, Map<String,String> vars) { String s = msg(path, vars); if (!s.isEmpty()) sender.sendMessage(s); }
    public List<String> list(String path, Map<String,String> vars) { return yml.getStringList(path).stream().map(s -> { for (var e : vars.entrySet()) s = s.replace(e.getKey(), e.getValue()); return ColorUtil.color(s); }).toList(); }
}
