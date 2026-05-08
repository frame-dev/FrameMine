package ch.framedev.framemine.main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.*;

public class Mine {

    private final Main plugin = Main.getInstance();
    private final String mineName;
    private final Location pos1;
    private final Location pos2;
    private boolean autoStart;
    private final Map<String, Double> materials;
    private final Random random;
    private long reset;

    public Mine(String mineName, Location pos1, Location pos2) {
        this.mineName = mineName;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.materials = new HashMap<>();
        this.random = new Random();
        this.reset = Main.getInstance() == null ? 5 : Math.max(1, Main.getInstance().getConfig().getLong("settings.default-reset-minutes", 5));
    }

    public String getMineName() {
        return mineName;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setReset(long reset) {
        this.reset = reset;
    }

    public long getReset() {
        return reset;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public Map<String, Double> getMaterials() {
        return materials;
    }

    public void removeMaterial(Material material) {
        if (material == null) return;
        materials.remove(material.name());
    }

    public void addMaterial(Material material, double chance) {
        if (material == null || chance <= 0) return;
        materials.put(material.name(), chance);
    }

    public Mine addMaterials(Map<String, Double> newMaterials) {
        if (newMaterials == null) return this;
        materials.putAll(newMaterials);
        return this;
    }

    public void save() {
        if (plugin == null || mineName == null || pos1 == null || pos2 == null) return;
        plugin.getConfig().set("mine." + mineName + ".name", mineName);
        plugin.getConfig().set("mine." + mineName + ".pos1", Utils.locationToString(pos1));
        plugin.getConfig().set("mine." + mineName + ".pos2", Utils.locationToString(pos2));
        plugin.getConfig().set("mine." + mineName + ".materials", null); // Clear previous materials
        for (Map.Entry<String, Double> entry : materials.entrySet()) {
            plugin.getConfig().set("mine." + mineName + ".materials." + entry.getKey(), entry.getValue());
        }
        plugin.getConfig().set("mine." + mineName + ".autostart", autoStart);
        plugin.getConfig().set("mine." + mineName + ".reset", reset);
        plugin.saveConfig();
    }

    public void fill() {
        Result result = getResult();
        if (result == null) return;

        for (int x = result.minX; x <= result.maxX; x++) {
            for (int y = result.minY; y <= result.maxY; y++) {
                for (int z = result.minZ; z <= result.maxZ; z++) {
                    result.world.getBlockAt(x, y, z).setType(getRandomMaterial());
                }
            }
        }
        // Teleport players within the mine to the top
        teleportPlayerToTheTop(result.world, result.minX, result.minY, result.minZ, result.maxX, result.maxY, result.maxZ);

        if (plugin.getConfig().getBoolean("settings.log-reset-messages", true)) {
            plugin.getLogger().info("Mine: " + mineName + " has been reset!");
        }
    }

    private boolean isInside(Location loc, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public void fillStone() {
        Result result = getResult();
        if (result == null) return;

        for (int x = result.minX; x <= result.maxX; x++) {
            for (int y = result.minY; y <= result.maxY; y++) {
                for (int z = result.minZ; z <= result.maxZ; z++) {
                    result.world.getBlockAt(x, y, z).setType(Material.STONE);
                }
            }
        }

        teleportPlayerToTheTop(result.world, result.minX, result.minY, result.minZ, result.maxX, result.maxY, result.maxZ);
    }

    private Result getResult() {
        if (pos1 == null || pos2 == null) return null;
        World world = getWorld();
        if (world == null) return null;
        int[] data = getData();
        if (data == null) return null;
        return new Result(world, data[0], data[1], data[2], data[3], data[4], data[5]);
    }

    private Material getRandomMaterial() {
        double total = materials.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) {
            return Material.STONE;
        }
        double randomValue = random.nextDouble() * total;
        double cumulative = 0.0;
        for (Map.Entry<String, Double> entry : materials.entrySet()) {
            cumulative += entry.getValue();
            if (randomValue <= cumulative) {
                Material material = Material.matchMaterial(entry.getKey());
                return material != null ? material : Material.STONE;
            }
        }
        return Material.STONE;  // Fallback
    }

    public void teleportPlayerToTheTop(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (Player player : world.getPlayers()) {
            Location playerLocation = player.getLocation();
            if (isInside(playerLocation, minX, maxX, minY, maxY, minZ, maxZ)) {
                Location topLocation = new Location(world, (minX + maxX) / 2.0, maxY + 1, (minZ + maxZ) / 2.0);
                player.teleport(topLocation);
            }
        }
    }

    public void startAutoReset(JavaPlugin plugin) {
        long resetFromConfig = Math.max(1, plugin.getConfig().getLong("mine." + mineName + ".reset", reset));
        long resetPeriodTicks = 60L * 20L * resetFromConfig;
        Main.registerResetTask(mineName, new BukkitRunnable() {
            @Override
            public void run() {
                fill();
                long resetTime = System.currentTimeMillis() + (resetPeriodTicks * 50L);
                if (plugin.getConfig().getBoolean("settings.log-next-reset", true)) {
                    plugin.getLogger().info("Next reset in " + new SimpleDateFormat("HH:mm:ss | dd.MM.yyyy").format(new Date(resetTime)));
                }
            }
        }.runTaskTimer(plugin, 0, resetPeriodTicks));
    }

    public World getWorld() {
        if (pos1 == null) return null;
        return pos1.getWorld();
    }

    public int[] getData() {
        if (pos1 == null || pos2 == null) return null;
        World world = getWorld();
        if (world == null) return null;
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        return new int[]{minX,minY,minZ,maxX,maxY,maxZ};
    }

    public static Mine loadMine(String mineName) {
        Main plugin = Main.getInstance();
        String name = plugin.getConfig().getString("mine." + mineName + ".name");
        if (name == null) return null;

        String pos1String = plugin.getConfig().getString("mine." + mineName + ".pos1");
        String pos2String = plugin.getConfig().getString("mine." + mineName + ".pos2");
        if (pos1String == null || pos2String == null) {
            plugin.getLogger().warning("Mine " + mineName + " is missing one or both positions.");
            return null;
        }

        Location pos1 = Utils.stringToLocation(pos1String);
        Location pos2 = Utils.stringToLocation(pos2String);
        if (pos1 == null || pos2 == null) {
            plugin.getLogger().warning("Mine " + mineName + " has invalid position data.");
            return null;
        }

        Map<String, Double> materials = new HashMap<>();
        ConfigurationSection materialSection = plugin.getConfig().getConfigurationSection("mine." + mineName + ".materials");
        if (materialSection != null) {
            for (String materialName : materialSection.getKeys(false)) {
                if (Material.matchMaterial(materialName) != null) {
                    materials.put(materialName, materialSection.getDouble(materialName));
                } else {
                    plugin.getLogger().warning("Mine " + mineName + " contains unknown material " + materialName + ".");
                }
            }
        }

        Mine mine = new Mine(name, pos1, pos2).addMaterials(materials);
        mine.setAutoStart(plugin.getConfig().getBoolean("mine." + mineName + ".autostart"));
        mine.reset = Math.max(1, plugin.getConfig().getLong("mine." + mineName + ".reset", mine.reset));
        return mine;
    }

    public void removeMine() {
        Main.cancelResetTask(mineName);
        plugin.getConfig().set("mine." + mineName, null);
        plugin.saveConfig();
    }

    private static class Result {
        public final World world;
        public final int minX;
        public final int minY;
        public final int minZ;
        public final int maxX;
        public final int maxY;
        public final int maxZ;

        public Result(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.world = world;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
    }
}
