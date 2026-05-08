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

    private static final List<String> SUBCOMMANDS = Arrays.asList("pos1", "pos2", "setup", "addmaterial", "setreset", "setautostart", "start", "info", "gui", "help", "tool", "reload");
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
            plugin.sendMessage(sender, "only-players");
            return true;
        }
        Player player = (Player) sender;
        if(!player.hasPermission("framemine.admin")) {
            plugin.sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            plugin.sendMessage(player, "usage-main");
            return true;
        }

        if(args[0].equalsIgnoreCase("tool")) {
            player.getInventory().addItem(new ItemBuilder(Material.STICK)
                    .addEnchantment(Utils.enchantment("INFINITY", "ARROW_INFINITE"), 1, true).hideEnchantments()
                    .setDisplayName(plugin.getToolName()).build());
            plugin.sendMessage(player, "tool-given");
            return true;
        }

        if(args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        if(args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveConfig();
            plugin.sendMessage(player, "config-reloaded");
            return true;
        }

        if (args[0].equalsIgnoreCase("pos1")) {
            pos1 = player.getLocation();
            plugin.sendMessage(player, "position-1-set");
            return true;
        }

        if (args[0].equalsIgnoreCase("pos2")) {
            pos2 = player.getLocation();
            plugin.sendMessage(player, "position-2-set");
            return true;
        }

        if(args[0].equalsIgnoreCase("gui")) {
            player.openInventory(plugin.getMineGUI().createMineSetupGUI());
            plugin.sendMessage(player, "gui-opened");
            return true;
        }

        if (args[0].equalsIgnoreCase("setup")) {
            if (pos1 != null && pos2 != null && args.length > 1) {
                Mine mine = new Mine(args[1], pos1, pos2);
                mine.fillStone();
                mine.save();
                plugin.sendMessage(player, "mine-created", "mine", args[1]);
                return true;
            }
            plugin.sendMessage(player, "missing-positions-or-name");
            return true;
        }

        if(args[0].equalsIgnoreCase("info")) {
            if(args.length == 2) {
                String mineName = args[1];
                Mine mine = Mine.loadMine(mineName);
                if(mine != null) {
                    plugin.sendMessage(player, "mine-info",
                            "mine", mine.getMineName(),
                            "pos1", Utils.locationToString(mine.getPos1()),
                            "pos2", Utils.locationToString(mine.getPos2()),
                            "materials", Utils.formatMaterialsMap(mine.getMaterials()),
                            "autostart", String.valueOf(mine.isAutoStart()),
                            "reset", String.valueOf(mine.getReset()));
                } else {
                    plugin.sendMessage(player, "mine-not-found");
                }
                return true;
            }
            plugin.sendMessage(player, "usage-info");
            return true;
        }

        if(args[0].equalsIgnoreCase("setautostart")) {
            if(args.length == 3) {
                if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) {
                    plugin.sendMessage(player, "usage-setautostart");
                    return true;
                }

                boolean autoStart = Boolean.parseBoolean(args[2]);
                    Mine mine = Mine.loadMine(args[1]);
                if(mine != null) {
                    mine.setAutoStart(autoStart);
                    mine.save();
                    plugin.sendMessage(player, "autostart-set", "mine", args[1], "autostart", String.valueOf(autoStart));
                    return true;
                } else {
                    plugin.sendMessage(player, "mine-not-found");
                }
                return true;
            }
            plugin.sendMessage(player, "usage-setautostart");
            return true;
        }

        if (args[0].equalsIgnoreCase("addmaterial")) {
            if (args.length == 4) {
                Mine mine = Mine.loadMine(args[1]);
                if (mine != null) {
                    try {
                        Material material = Material.matchMaterial(args[2]);
                        if (material == null) {
                            plugin.sendMessage(player, "invalid-material");
                            return true;
                        }
                        double chance = Double.parseDouble(args[3]);
                        if (chance <= 0) {
                            plugin.sendMessage(player, "chance-greater-than-zero");
                            return true;
                        }
                        mine.addMaterial(material, chance);
                        mine.save();
                        plugin.sendMessage(player, "material-added", "mine", args[1], "material", material.name(), "chance", String.valueOf(chance));
                        return true;
                    } catch (IllegalArgumentException e) {
                        plugin.sendMessage(player, "invalid-material-or-chance");
                        return true;
                    }
                }
                plugin.sendMessage(player, "mine-not-found");
                return true;
            }
            plugin.sendMessage(player, "usage-addmaterial");
            return true;
        }

        if (args[0].equalsIgnoreCase("setreset")) {
            if (args.length == 3) {
                Mine mine = Mine.loadMine(args[1]);
                if (mine != null) {
                    try {
                        long interval = Long.parseLong(args[2]);
                        if (interval <= 0) {
                            plugin.sendMessage(player, "interval-greater-than-zero");
                            return true;
                        }
                        mine.setReset(interval);
                        mine.save();
                        plugin.sendMessage(player, "reset-set", "mine", args[1], "reset", String.valueOf(interval));
                        return true;
                    } catch (NumberFormatException e) {
                        plugin.sendMessage(player, "invalid-interval");
                        return true;
                    }
                }
                plugin.sendMessage(player, "mine-not-found");
                return true;
            }
            plugin.sendMessage(player, "usage-setreset");
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (args.length == 2) {
                Mine mine = Mine.loadMine(args[1]);
                if (mine != null) {
                    mine.startAutoReset(plugin);
                    plugin.sendMessage(player, "auto-reset-started", "mine", args[1]);
                    return true;
                }
                plugin.sendMessage(player, "mine-not-found");
                return true;
            }
            plugin.sendMessage(player, "usage-start");
            return true;
        }

        plugin.sendMessage(player, "unknown-subcommand");
        return true;
    }

    private void sendHelp(Player player) {
        plugin.sendMessage(player, "help");
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
