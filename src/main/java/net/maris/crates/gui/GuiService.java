package net.maris.crates.gui;

import net.maris.crates.MarisCratesPlugin;
import net.maris.crates.crate.Crate;
import net.maris.crates.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;

public final class GuiService {
    public static final int[] REWARD_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private final MarisCratesPlugin plugin;
    public GuiService(MarisCratesPlugin plugin) { this.plugin = plugin; }
    public void openEdit(Player p, Crate c) { YamlConfiguration yml = yml(); Inventory inv = Bukkit.createInventory(new CrateGuiHolder(CrateGuiHolder.Type.EDIT, c.name(), -1), yml.getInt("edit.rows",3)*9, ColorUtil.color(yml.getString("edit.title").replace("%crates%", ColorUtil.smallCaps(c.name())))); fillRewards(inv, c, false); p.openInventory(inv); }
    public void openPreview(Player p, Crate c) { YamlConfiguration yml = yml(); Inventory inv = Bukkit.createInventory(new CrateGuiHolder(CrateGuiHolder.Type.PREVIEW, c.name(), -1), yml.getInt("preview.rows",3)*9, ColorUtil.color(yml.getString("preview.title"))); fillRewards(inv, c, true); p.openInventory(inv); }
    public void openConfirm(Player p, Crate c, int idx) { YamlConfiguration yml = yml(); Inventory inv = Bukkit.createInventory(new CrateGuiHolder(CrateGuiHolder.Type.CONFIRM, c.name(), idx), yml.getInt("confirm.rows",3)*9, ColorUtil.color(yml.getString("confirm.title"))); inv.setItem(yml.getInt("confirm.cancel.slot",11), button(yml, "confirm.cancel")); inv.setItem(13, c.rewards().get(idx).item().clone()); inv.setItem(yml.getInt("confirm.confirm.slot",15), button(yml, "confirm.confirm")); p.openInventory(inv); }
    private void fillRewards(Inventory inv, Crate c, boolean clone) { for (int i=0; i<c.rewards().size() && i<REWARD_SLOTS.length; i++) { int slot = REWARD_SLOTS[i]; if (slot >= 0 && slot < inv.getSize()) { ItemStack item = c.rewards().get(i).item(); inv.setItem(slot, clone ? item.clone() : item); } } }
    public static int rewardIndex(int slot) { for (int i=0; i<REWARD_SLOTS.length; i++) if (REWARD_SLOTS[i] == slot) return i; return -1; }
    private ItemStack button(YamlConfiguration yml, String path) { Material mat = Material.matchMaterial(yml.getString(path+".material", "STONE")); ItemStack item = new ItemStack(mat == null ? Material.STONE : mat); ItemMeta meta = item.getItemMeta(); meta.setDisplayName(ColorUtil.color(yml.getString(path+".name", ""))); meta.setLore(yml.getStringList(path+".lore").stream().map(ColorUtil::color).toList()); item.setItemMeta(meta); return item; }
    private YamlConfiguration yml() { return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "guis.yml")); }
}
