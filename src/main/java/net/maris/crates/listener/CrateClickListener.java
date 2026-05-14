package net.maris.crates.listener;

import net.maris.crates.MarisCratesPlugin;
import net.maris.crates.crate.Crate;
import net.maris.crates.gui.GuiService;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public final class CrateClickListener implements Listener {
    private final MarisCratesPlugin plugin; private final GuiService gui;
    public CrateClickListener(MarisCratesPlugin plugin){this.plugin=plugin;this.gui=new GuiService(plugin);}    
    @EventHandler public void interact(PlayerInteractEvent e){ if(e.getClickedBlock()==null)return; Location l=e.getClickedBlock().getLocation(); for(Crate c:plugin.crates().all()){ if(c.location()==null || c.location().getWorld()!=l.getWorld()) continue; if(c.location().getBlockX()==l.getBlockX()&&c.location().getBlockY()==l.getBlockY()&&c.location().getBlockZ()==l.getBlockZ()){ e.setCancelled(true); gui.openPreview(e.getPlayer(),c); return; } } }
}
