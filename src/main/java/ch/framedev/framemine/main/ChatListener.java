package ch.framedev.framemine.main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Material> waitingForChance;
    private final Set<UUID> waitingForMineName;
    private final Set<UUID> waitingForCreate;

    public ChatListener(Main plugin) {
        this.plugin = plugin;
        this.waitingForChance = new ConcurrentHashMap<>();
        this.waitingForMineName = ConcurrentHashMap.newKeySet();
        this.waitingForCreate = ConcurrentHashMap.newKeySet();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setWaitingForMineName(Player player) {
        waitingForMineName.add(player.getUniqueId());
    }

    public void setWaitingForCreate(Player player) {
        waitingForCreate.add(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (waitingForChance.containsKey(playerId)) {
            event.setCancelled(true); // Prevent the message from being broadcast

            Material material = waitingForChance.remove(playerId);
            String message = event.getMessage();

            try {
                double chance = Double.parseDouble(message);
                if (chance <= 0) {
                    plugin.sendMessage(player, "chance-greater-than-zero");
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Mine mine = plugin.getMineGUI().getCurrentMine();
                    if (mine != null) {
                        mine.addMaterial(material, chance);
                        mine.save();
                        plugin.sendMessage(player, "material-added",
                                "mine", mine.getMineName(),
                                "material", material.name(),
                                "chance", String.valueOf(chance));
                    } else {
                        plugin.sendMessage(player, "no-mine-selected");
                    }
                });
            } catch (NumberFormatException e) {
                plugin.sendMessage(player, "invalid-chance");
            }
            return;
        }

        String mineName;
        if (waitingForCreate.contains(playerId)) {
            waitingForCreate.remove(playerId);
            event.setCancelled(true);
            mineName = event.getMessage();
            String finalMineName = mineName;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (plugin.getMineCMD().getPos1() == null || plugin.getMineCMD().getPos2() == null) {
                    plugin.sendMessage(player, "missing-positions");
                    return;
                }
                Mine mine = new Mine(finalMineName, plugin.getMineCMD().getPos1(), plugin.getMineCMD().getPos2());
                mine.fillStone();
                mine.save();
                plugin.sendMessage(player, "mine-created", "mine", finalMineName);
            });
            return;
        }

        if (waitingForMineName.contains(playerId)) {
            waitingForMineName.remove(playerId);
            event.setCancelled(true);
            mineName = event.getMessage();
            String finalMineName = mineName;
            Bukkit.getScheduler().runTask(plugin, () -> {
                Mine mine = Mine.loadMine(finalMineName);
                plugin.getMineGUI().setCurrentMine(mine);
                if (mine == null) {
                    plugin.sendMessage(player, "mine-not-found");
                } else {
                    plugin.sendMessage(player, "mine-selected", "mine", finalMineName);
                }
            });
        }
    }

}
