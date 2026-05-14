package net.maris.crates.command;

import net.maris.crates.MarisCratesPlugin;
import net.maris.crates.crate.Crate;
import net.maris.crates.gui.GuiService;
import net.maris.crates.util.ColorUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CratesCommand implements CommandExecutor, TabCompleter {
    private final MarisCratesPlugin plugin;
    private final GuiService gui;

    public CratesCommand(MarisCratesPlugin plugin) {
        this.plugin = plugin;
        this.gui = new GuiService(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mariscrates.admin")) {
            plugin.messages().send(sender, "no-permission", Map.of());
            return true;
        }
        if (args.length < 1) {
            plugin.messages().send(sender, "usage", Map.of());
            return true;
        }
        try {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "give" -> giveTake(sender, args, true);
                case "take" -> giveTake(sender, args, false);
                case "create" -> create(sender, args);
                case "edit" -> edit(sender, args);
                case "setloc" -> setloc(sender, args);
                case "setcolor" -> setcolor(sender, args);
                case "reload" -> reload(sender);
                case "keyall" -> {
                    if (args.length >= 3) keyall(args[1], Integer.parseInt(args[2]), false);
                    else plugin.messages().send(sender, "usage", Map.of());
                }
                default -> plugin.messages().send(sender, "usage", Map.of());
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
        return true;
    }

    private void giveTake(CommandSender sender, String[] args, boolean give) {
        if (args.length < 4) {
            plugin.messages().send(sender, "usage", Map.of());
            return;
        }
        Crate crate = plugin.crates().get(args[1]);
        if (crate == null) {
            plugin.messages().send(sender, "crate-not-found", Map.of());
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            plugin.messages().send(sender, "player-not-found", Map.of());
            return;
        }
        int amount = Integer.parseInt(args[3]);
        plugin.storage().add(target.getUniqueId(), crate.name(), give ? amount : -amount);
        plugin.holograms().refresh(crate);
        plugin.messages().send(sender, give ? "key-given" : "key-taken", vars(crate, amount, target.getName()));
    }

    private void create(CommandSender sender, String[] args) throws Exception {
        if (args.length < 2) return;
        Crate crate = plugin.crates().create(args[1]);
        plugin.messages().send(sender, "crate-created", Map.of("%crates%", ColorUtil.smallCaps(crate.name())));
    }

    private void edit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player) || args.length < 2) return;
        Crate crate = plugin.crates().get(args[1]);
        if (crate == null) {
            plugin.messages().send(sender, "crate-not-found", Map.of());
            return;
        }
        gui.openEdit(player, crate);
    }

    private void setloc(CommandSender sender, String[] args) throws Exception {
        if (!(sender instanceof Player player) || args.length < 2) return;
        Crate crate = plugin.crates().get(args[1]);
        if (crate == null) {
            plugin.messages().send(sender, "crate-not-found", Map.of());
            return;
        }
        Block block = player.getTargetBlockExact(8);
        if (block == null) return;
        crate.location(block.getLocation());
        plugin.crates().save(crate);
        plugin.holograms().refresh(crate);
        plugin.messages().send(sender, "loc-set", Map.of("%crates%", ColorUtil.smallCaps(crate.name())));
    }

    private void setcolor(CommandSender sender, String[] args) throws Exception {
        if (args.length < 3) return;
        Crate crate = plugin.crates().get(args[1]);
        if (crate == null) {
            plugin.messages().send(sender, "crate-not-found", Map.of());
            return;
        }
        crate.color(args[2]);
        plugin.crates().save(crate);
        plugin.holograms().refresh(crate);
        plugin.messages().send(sender, "color-set", Map.of("%crates%", ColorUtil.smallCaps(crate.name()), "%color%", args[2]));
    }

    private void reload(CommandSender sender) {
        plugin.reloadPlugin();
        plugin.messages().send(sender, "reloaded", Map.of());
    }

    public void keyall(String crateName, int amount, boolean auto) {
        Crate crate = plugin.crates().get(crateName);
        if (crate == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.storage().add(player.getUniqueId(), crate.name(), amount);
            player.sendTitle(ColorUtil.color(crate.color() + "+" + amount + " " + ColorUtil.smallCaps(crate.name()) + " key"), "", 10, 40, 10);
            if (auto) sendAutoMessage(player, crate, amount);
        }
        plugin.holograms().refresh(crate);
    }

    private void sendAutoMessage(Player player, Crate crate, int amount) {
        for (String line : plugin.messages().list("keyall-player-chat", vars(crate, amount, player.getName()))) {
            if (line.contains("[CLICK TO TELEPORT]")) {
                BaseComponent[] components = TextComponent.fromLegacyText(ColorUtil.color(line));
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.getConfig().getString("warp-command", "warp crates"));
                for (BaseComponent component : components) component.setClickEvent(clickEvent);
                player.spigot().sendMessage(components);
            } else {
                player.sendMessage(line);
            }
        }
    }

    private Map<String, String> vars(Crate crate, int amount, String player) {
        return Map.of(
                "%crates%", ColorUtil.smallCaps(crate.name()),
                "%amount%", String.valueOf(amount),
                "%player%", String.valueOf(player),
                "%crate_color%", crate.color()
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) return List.of("give", "take", "create", "edit", "setloc", "setcolor", "reload", "keyall");
        if (args.length == 2 && !args[0].equalsIgnoreCase("create")) return plugin.crates().all().stream().map(Crate::name).toList();
        return List.of();
    }
}
