package ch.framedev.framemine.main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class MineCMD implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("pos1", "pos2", "setup", "addmaterial", "setreset", "setautostart", "start", "info", "gui", "help", "tool");
    private static final List<String> MATERIAL_NAMES = Arrays.stream(Material.values())
            .filter(material -> material != Material.AIR)
            .filter(Utils::isItem)
            .map(Material::name)
            .collect(Collectors.toList());

    private final Main plugin;
    private Location pos1;
    private Location pos2;

    public MineCMD(Main plugin) {
        this.plugin = plugin;
        PluginCommand mineCommand = plugin.getCommand("mine");
        if (mineCommand == null) {
            plugin.getLogger().severe("Command 'mine' is missing from plugin.yml. Disabling FrameMine.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        mineCommand.setExecutor(this);
        mineCommand.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
            player.sendMessage(plugin.getPrefix() + "Usage: /mine <pos1|pos2|setup|addmaterial|setreset|setautostart|start|info|gui|help|tool>");
            return true;
        }

        if(args[0].equalsIgnoreCase("tool")) {
            player.getInventory().addItem(new ItemBuilder(Material.STICK)
                    .addEnchantment(Utils.enchantment("INFINITY", "ARROW_INFINITE"), 1, true).hideEnchantments()
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
            return true;
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
                } else {
                    player.sendMessage("Mine not found.");
                }
                return true;
            }
            player.sendMessage(plugin.getPrefix() + "Usage: /mine info <mineName>");
            return true;
        }

        if(args[0].equalsIgnoreCase("setautostart")) {
            if(args.length == 3) {
                if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) {
                    player.sendMessage(plugin.getPrefix() + "Usage: /mine setautostart <mineName> <true|false>");
                    return true;
                }

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
                return true;
            }
            player.sendMessage(plugin.getPrefix() + "Usage: /mine setautostart <mineName> <true|false>");
            return true;
        }

        if (args[0].equalsIgnoreCase("addmaterial")) {
            if (args.length == 4) {
                Mine mine = Mine.loadMine(args[1]);
                if (mine != null) {
                    try {
                        Material material = Material.matchMaterial(args[2]);
                        if (material == null) {
                            player.sendMessage("Invalid material.");
                            return true;
                        }
                        double chance = Double.parseDouble(args[3]);
                        if (chance <= 0) {
                            player.sendMessage("Chance must be greater than 0.");
                            return true;
                        }
                        mine.addMaterial(material, chance);
                        mine.save();
                        player.sendMessage("Material added to mine.");
                        return true;
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("Invalid material or chance.");
                        return true;
                    }
                }
                player.sendMessage("Mine not found.");
                return true;
            }
            player.sendMessage(plugin.getPrefix() + "Usage: /mine addmaterial <mineName> <material> <chance>");
            return true;
        }

        if (args[0].equalsIgnoreCase("setreset")) {
            if (args.length == 3) {
                Mine mine = Mine.loadMine(args[1]);
                if (mine != null) {
                    try {
                        long interval = Long.parseLong(args[2]);
                        if (interval <= 0) {
                            player.sendMessage("Interval must be greater than 0.");
                            return true;
                        }
                        mine.setReset(interval);
                        mine.save();
                        player.sendMessage("Reset interval set for mine.");
                        return true;
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid interval.");
                        return true;
                    }
                }
                player.sendMessage("Mine not found.");
                return true;
            }
            player.sendMessage(plugin.getPrefix() + "Usage: /mine setreset <mineName> <interval>");
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (args.length == 2) {
                Mine mine = Mine.loadMine(args[1]);
                if (mine != null) {
                    mine.startAutoReset(plugin);
                    player.sendMessage("Auto-reset started for mine.");
                    return true;
                }
                player.sendMessage("Mine not found.");
                return true;
            }
            player.sendMessage(plugin.getPrefix() + "Usage: /mine start <mineName>");
            return true;
        }

        player.sendMessage(plugin.getPrefix() + "Unknown subcommand. Use /mine help.");
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("--- FrameMine commands ---");
        player.sendMessage("/mine <pos1|pos2|setup|addmaterial|setreset|setautostart|start|info|gui|tool> [args]");
        player.sendMessage("tool: left and right click for position to set");
        player.sendMessage("pos1: Sets position 1 for the mine.");
        player.sendMessage("pos2: Sets position 2 for the mine.");
        player.sendMessage("setup: Creates a new mine with given name.");
        player.sendMessage("addmaterial <mineName> <material> <chance>: Adds a new material to the mine.");
        player.sendMessage("setreset <mineName> <interval>: Sets the interval for auto-reset.");
        player.sendMessage("setautostart <mineName> <true|false>: Toggles auto-start for the mine.");
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("addmaterial") || args[0].equalsIgnoreCase("setreset") || args[0].equalsIgnoreCase("start") ||
            args[0].equalsIgnoreCase("setautostart") || args[0].equalsIgnoreCase("info")) {
                ArrayList<String> empty = new ArrayList<>();
                ConfigurationSection mineSection = plugin.getConfig().getConfigurationSection("mine");
                List<String> mineNames = mineSection == null ? Collections.emptyList() : new ArrayList<>(mineSection.getKeys(false));
                for (String mineName : mineNames) {
                    if (mineName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        empty.add(mineName);
                    }
                }
                Collections.sort(empty);
                return empty; // Here you would return the list of available materials if you had a way to list them
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("setautostart")) {
            return StringUtil.copyPartialMatches(args[2], Arrays.asList("true", "false"), new ArrayList<>());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("addmaterial")) {
            ArrayList<String> empty = new ArrayList<>();
            for (String materialName : MATERIAL_NAMES) {
                if (materialName.toLowerCase().startsWith(args[2].toLowerCase())) {
                    empty.add(materialName);
                }
            }
            Collections.sort(empty);
            return empty;  // Here you would return the list of available materials if you had a way to list them
        }

        return Collections.emptyList();
    }
}
