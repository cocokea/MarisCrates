package net.maris.crates.gui;

import net.maris.crates.MarisCratesPlugin;
import net.maris.crates.crate.Crate;
import net.maris.crates.crate.CrateReward;
import net.maris.crates.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class GuiListener implements Listener {
    private final MarisCratesPlugin plugin; private final GuiService gui; private final Set<UUID> backOnClose = new HashSet<>(); private final Set<UUID> confirming = new HashSet<>(); private final Map<UUID, Long> lastGuiClick = new HashMap<>();
    public GuiListener(MarisCratesPlugin plugin) { this.plugin = plugin; this.gui = new GuiService(plugin); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false) public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p) || !(e.getInventory().getHolder() instanceof CrateGuiHolder h)) return;
        if (h.type == CrateGuiHolder.Type.EDIT) return; e.setCancelled(true); p.updateInventory(); if (isPacketSpam(p)) return; Crate c = plugin.crates().get(h.crate); if (c == null) return;
        if (h.type == CrateGuiHolder.Type.PREVIEW) { int idx = GuiService.rewardIndex(e.getRawSlot()); if (idx < 0 || idx >= c.rewards().size()) return; if (plugin.storage().get(p.getUniqueId(), c.name()) <= 0) { p.closeInventory(); p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(plugin.messages().msg("no-key-actionbar", Map.of()))); p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1); return; } gui.openConfirm(p,c,idx); return; }
        if (h.type == CrateGuiHolder.Type.CONFIRM) { if (e.getRawSlot()==11) { backOnClose.add(p.getUniqueId()); p.closeInventory(); plugin.runner().laterGlobal(() -> gui.openPreview(p,c),1); return; } if (e.getRawSlot()==15) confirm(p,c,h.rewardIndex); }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false) public void drag(InventoryDragEvent e) { if (e.getInventory().getHolder() instanceof CrateGuiHolder h && h.type != CrateGuiHolder.Type.EDIT) { e.setCancelled(true); if (e.getWhoClicked() instanceof Player p) p.updateInventory(); } }
    private void confirm(Player p, Crate c, int idx) { UUID uuid = p.getUniqueId(); if (idx < 0 || idx >= c.rewards().size() || !confirming.add(uuid)) return; boolean delayedUnlock = false; try { if (!plugin.storage().tryTake(uuid, c.name(), 1)) { p.closeInventory(); p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(plugin.messages().msg("no-key-actionbar", Map.of()))); p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1); return; } plugin.holograms().refresh(c); CrateReward r = c.rewards().get(idx); if (r.giveItem()) p.getInventory().addItem(r.item().clone()); else plugin.runner().global(() -> { for (String cmd : r.commands()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()).replace("%crates%", c.name())); }); p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1); delayedUnlock = true; plugin.runner().laterGlobal(() -> { confirming.remove(uuid); gui.openPreview(p,c); },2); } finally { if (!delayedUnlock) confirming.remove(uuid); } }
    @EventHandler public void close(InventoryCloseEvent e) { if (!(e.getPlayer() instanceof Player p) || !(e.getInventory().getHolder() instanceof CrateGuiHolder h)) return; lastGuiClick.remove(p.getUniqueId()); Crate c = plugin.crates().get(h.crate); if (c == null) return; if (h.type == CrateGuiHolder.Type.EDIT) { List<ItemStack> items = new ArrayList<>(); for (int slot : GuiService.REWARD_SLOTS) { ItemStack item = e.getInventory().getItem(slot); if (item != null) items.add(item); } try { plugin.crates().saveRewards(c, items); } catch(Exception ex) { ex.printStackTrace(); } plugin.messages().send(p,"crate-edited", Map.of("%crates%", c.name())); } else if (h.type == CrateGuiHolder.Type.CONFIRM && !backOnClose.remove(p.getUniqueId())) plugin.runner().laterGlobal(() -> gui.openPreview(p,c),1); }
    private boolean isPacketSpam(Player p) { long now = System.currentTimeMillis(); Long last = lastGuiClick.put(p.getUniqueId(), now); return last != null && now - last < 75; }
}
