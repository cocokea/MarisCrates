package net.maris.crates.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class CrateGuiHolder implements InventoryHolder {
    public enum Type { EDIT, PREVIEW, CONFIRM }
    public final Type type; public final String crate; public final int rewardIndex;
    public CrateGuiHolder(Type type, String crate, int rewardIndex) { this.type = type; this.crate = crate; this.rewardIndex = rewardIndex; }
    public Inventory getInventory() { return null; }
}
