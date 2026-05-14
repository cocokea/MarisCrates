package net.maris.crates.crate;

import org.bukkit.inventory.ItemStack;
import java.util.List;

public record CrateReward(ItemStack item, boolean giveItem, List<String> commands) {}
