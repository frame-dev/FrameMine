package ch.framedev.framemine.main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListener implements Listener {

    private final Main plugin;
    private final Map<Player, Material> waitingForChance;
    private final List<Player> waitingForMineName;
    private final List<Player> waitingForCreate;

    public ChatListener(Main plugin) {
        this.plugin = plugin;
        this.waitingForChance = new HashMap<>();
        this.waitingForMineName = new ArrayList<>();
        this.waitingForCreate = new ArrayList<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setWaitingForMineName(Player player) {
        waitingForMineName.add(player);
    }

    public void setWaitingForCreate(Player player) {
        waitingForCreate.add(player);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (waitingForChance.containsKey(player)) {
            event.setCancelled(true); // Prevent the message from being broadcast

            Material material = waitingForChance.remove(player);
            String message = event.getMessage();

            try {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    double chance = Double.parseDouble(message);
                    Mine mine = plugin.getMineGUI().getCurrentMine();
                    if (mine != null) {
                        mine.addMaterial(material, chance);
                        mine.save();
                        player.sendMessage("Added " + material.name() + " with chance " + chance + " to the mine.");
                    } else {
                        player.sendMessage("Error: No mine selected.");
                    }
                });
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid chance. Please enter a valid number.");
            }
        }

        String mineName;
        if (waitingForCreate.contains(player)) {
            waitingForCreate.remove(player);
            event.setCancelled(true);
            mineName = event.getMessage();
            String finalMineName = mineName;
            Bukkit.getScheduler().runTask(plugin, () -> {
                Mine mine = new Mine(finalMineName, plugin.getMineCMD().getPos1(), plugin.getMineCMD().getPos2());
                mine.fillStone();
                mine.save();
                player.sendMessage("Mine setup and filled with stone.");
            });
        }

        if (waitingForMineName.contains(player)) {
            waitingForMineName.remove(player);
            event.setCancelled(true);
            mineName = event.getMessage();
            plugin.getMineGUI().setCurrentMine(Mine.loadMine(mineName));
        }
    }

}
