package ch.framedev.framemine.main;

import ch.framedev.spigotutils.SpigotAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MineCMD implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private Location pos1;
    private Location pos2;

    public MineCMD(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("mine")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("mine")).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPrefix() + "This command can only be executed by players.");
            return true;
        }
        Player player = (Player) sender;
        if(!player.hasPermission("framemine.admin")) {
            player.sendMessage(plugin.getPrefix() + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getPrefix() + "Usage: /mine <pos1|pos2|setup|addmaterial|setreset|start|info|gui|help|tool>");
            return false;
        }

        if(args[0].equalsIgnoreCase("tool")) {
            player.getInventory().addItem(new SpigotAPI.ItemBuilder(Material.STICK)
                    .addEnchantment(Enchantment.INFINITY, 1, true).hideEnchantments()
                    .setDisplayName("§aMine Positioning Tool").build());
            player.sendMessage(plugin.getPrefix() + "§aUse Left and Right Click to set the Positions!");
            return true;
        }

        if(args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("pos1")) {
            pos1 = player.getLocation();
            player.sendMessage("Position 1 set.");
            return true;
        }

        if (args[0].equalsIgnoreCase("pos2")) {
            pos2 = player.getLocation();
            player.sendMessage("Position 2 set.");
            return true;
        }

        if(args[0].equalsIgnoreCase("gui")) {
            player.openInventory(plugin.getMineGUI().createMineSetupGUI());
            player.sendMessage("GUI opened.");
            return true;
        }

        if (args[0].equalsIgnoreCase("setup")) {
            if (pos1 != null && pos2 != null && args.length > 1) {
                Mine mine = new Mine(args[1], pos1, pos2);
                mine.fillStone();
                mine.save();
                player.sendMessage(plugin.getPrefix() + "Mine setup and filled with stone.");
                return true;
            }
            player.sendMessage(plugin.getPrefix() + "You need to set both positions and provide a name.");
            return false;
        }

        if(args[0].equalsIgnoreCase("info")) {
            if(args.length == 2) {
                String mineName = args[1];
                Mine mine = Mine.loadMine(mineName);
                if(mine != null) {
                    player.sendMessage("Name: " + mine.getMineName());
                    player.sendMessage("Position 1: " + Utils.locationToString(mine.getPos1()));
                    player.sendMessage("Position 2: " + Utils.locationToString(mine.getPos2()));
                    player.sendMessage("Materials: " + Utils.formatMaterialsMap(mine.getMaterials()));
                    player.sendMessage("AutoStart: " + mine.isAutoStart());
                    player.sendMessage("Reset: " + mine.getReset() + " Minutes");
                }
            }
        }

        if(args[0].equalsIgnoreCase("setautostart")) {
            if(args.length == 3) {
                try {
                    boolean autoStart = Boolean.parseBoolean(args[2]);
                    Mine mine = Mine.loadMine(args[1]);
                    if(mine != null) {
                        mine.setAutoStart(autoStart);
                        mine.save();
                        player.sendMessage("AutoStart set to " + autoStart + ".");
                        return true;
                    } else {
                        player.sendMessage("Mine not found.");
                    }
                } catch (Exception ignored) {
                    player.sendMessage(plugin.getPrefix() + "Wrong arguments for setautostart command (true/false)");
                }
            }
        }

        if (args[0].equalsIgnoreCase("addmaterial")) {
            if (args.length == 4) {
                Mine mine = Mine.loadMine(args[1]);
                if (mine != null) {
                    try {
                        Material material = Material.valueOf(args[2].toUpperCase());
                        double chance = Double.parseDouble(args[3]);
                        mine.addMaterial(material, chance);
                        mine.save();
                        player.sendMessage("Material added to mine.");
                        return true;
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("Invalid material or chance.");
                        return false;
                    }
                }
                player.sendMessage("Mine not found.");
                return false;
            }
            player.sendMessage(plugin.getPrefix() + "Usage: /mine addmaterial <mineName> <material> <chance>");
            return false;
        }

        if (args[0].equalsIgnoreCase("setreset")) {
            if (args.length == 3) {
                Mine mine = Mine.loadMine(args[1]);
                if (mine != null) {
                    try {
                        long interval = Long.parseLong(args[2]);
                        mine.setReset(interval);
                        mine.save();
                        player.sendMessage("Reset interval set for mine.");
                        return true;
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid interval.");
                        return false;
                    }
                }
                player.sendMessage("Mine not found.");
                return false;
            }
            player.sendMessage(plugin.getPrefix() + "Usage: /mine setreset <mineName> <interval>");
            return false;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (args.length == 2) {
                Mine mine = Mine.loadMine(args[1]);
                if (mine != null) {
                    if (Main.tasks.containsKey(mine.getMineName())) {
                        Main.tasks.get(mine.getMineName()).cancel();
                        Main.tasks.remove(mine.getMineName());
                    }
                    mine.startAutoReset(plugin);
                    player.sendMessage("Auto-reset started for mine.");
                    return true;
                }
                player.sendMessage("Mine not found.");
                return false;
            }
            player.sendMessage(plugin.getPrefix() + "Usage: /mine start <mineName>");
            return false;
        }

        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage("--- FrameMine commands ---");
        player.sendMessage("/mine <pos1|pos2|setup|addmaterial|setreset|start|info|gui> [args]");
        player.sendMessage("tool: left and right click for position to set");
        player.sendMessage("pos1: Sets position 1 for the mine.");
        player.sendMessage("pos2: Sets position 2 for the mine.");
        player.sendMessage("setup: Creates a new mine with given name.");
        player.sendMessage("addmaterial <mineName> <material> <chance>: Adds a new material to the mine.");
        player.sendMessage("setreset <mineName> <interval>: Sets the interval for auto-reset.");
        player.sendMessage("start <mineName>: Starts the auto-reset for the given mine.");
        player.sendMessage("info <mineName>: Shows information about the given mine.");
        player.sendMessage("gui: Opens the GUI for creating and managing mines.");
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location getPos2() {
        return pos2;
    }

    private static final List<String> SUBCOMMANDS = Arrays.asList("pos1", "pos2", "setup", "addmaterial", "setreset", "start", "info", "gui", "tool");

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("addmaterial") || args[0].equalsIgnoreCase("setreset") || args[0].equalsIgnoreCase("start") ||
            args[0].equalsIgnoreCase("info")) {
                ArrayList<String> empty = new ArrayList<>();
                List<String> mineNames = new ArrayList<>();
                if(plugin.getConfig().getConfigurationSection("mine") != null)
                    mineNames.addAll(Objects.requireNonNull(plugin.getConfig().getConfigurationSection("mine")).getKeys(false));
                for (String mineName : mineNames) {
                    if (mineName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        empty.add(mineName);
                    }
                }
                Collections.sort(empty);
                return empty; // Here you would return the list of available materials if you had a way to list them
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("addmaterial")) {
            ArrayList<String> empty = new ArrayList<>();
            Material[] materials = Material.values();
            for (Material material : materials) {
                if (material.name().toLowerCase().startsWith(args[2].toLowerCase())) {
                    empty.add(material.name());
                }
            }
            Collections.sort(empty);
            return empty;  // Here you would return the list of available materials if you had a way to list them
        }

        return Collections.emptyList();
    }
}