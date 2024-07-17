package ch.framedev.framemine.main;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PositionListener implements Listener {

    private final Main plugin;

    public PositionListener(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || item.getItemMeta() == null) return;

        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§aMine Positioning Tool")) {
            event.setCancelled(true);
            if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) return;

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                plugin.getMineCMD().setPos1(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage("Position 1 set.");
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                plugin.getMineCMD().setPos2(event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage("Position 2 set.");
            }
        }
    }
}
