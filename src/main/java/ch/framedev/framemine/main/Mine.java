package ch.framedev.framemine.main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

public class Mine {

    private final Main plugin = Main.getInstance();
    private final String mineName;
    private final Location pos1;
    private final Location pos2;
    private boolean autoStart;
    private Map<String, Double> materials;
    private final Random random;
    private long reset = 5;

    public Mine(@NotNull String mineName, Location pos1, Location pos2) {
        this.mineName = mineName;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.materials = new HashMap<>();
        this.random = new Random();
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
        materials.remove(material.name());
    }

    public void addMaterial(Material material, double chance) {
        materials.put(material.name(), chance);
    }

    public Mine addMaterials(Map<String, Double> newMaterials) {
        materials.putAll(newMaterials);
        return this;
    }

    public void save() {
        // Save mine data to a file or database
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

        plugin.getLogger().info("Mine: " + mineName + " has been reset!");
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

    private @Nullable Result getResult() {
        World world = getWorld();
        if (world == null) return null;
        int minX = getData()[0];
        int minY = getData()[1];
        int minZ = getData()[2];
        int maxX = getData()[3];
        int maxY = getData()[4];
        int maxZ = getData()[5];
        return new Result(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    private Material getRandomMaterial() {
        double total = materials.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = random.nextDouble() * total;
        double cumulative = 0.0;
        for (Map.Entry<String, Double> entry : materials.entrySet()) {
            cumulative += entry.getValue();
            if (randomValue <= cumulative) {
                return Material.valueOf(entry.getKey());
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
        long resetFromConfig = plugin.getConfig().getLong("mine." + mineName + ".reset");
        Main.tasks.put(mineName, new BukkitRunnable() {
            @Override
            public void run() {
                fill();
                long start = System.currentTimeMillis();
                long resetIntervalMillis = 60 * 20 * resetFromConfig * 50L; // Convert ticks to milliseconds (1 tick = 50 ms)
                long resetTime = start + resetIntervalMillis;
                plugin.getLogger().info("Next reset in " + new SimpleDateFormat("HH:mm:ss | dd.MM.yyyy").format(new Date(resetTime)));
            }
        }.runTaskTimer(plugin, 0, 60 * 20 * resetFromConfig));
    }

    public World getWorld() {
        return pos1.getWorld();
    }

    public int[] getData() {
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
        Location pos1 = Utils.stringToLocation(Objects.requireNonNull(plugin.getConfig().getString("mine." + mineName + ".pos1")));
        Location pos2 = Utils.stringToLocation(Objects.requireNonNull(plugin.getConfig().getString("mine." + mineName + ".pos2")));
        Map<String, Double> materials = new HashMap<>();
        if (plugin.getConfig().getConfigurationSection("mine." + mineName + ".materials") != null)
            for (String materialName : Objects.requireNonNull(plugin.getConfig().getConfigurationSection("mine." + mineName + ".materials")).getKeys(false)) {
                materials.put(materialName, plugin.getConfig().getDouble("mine." + mineName + ".materials." + materialName));
            }
        Mine mine = new Mine(name, pos1, pos2).addMaterials(materials);
        if (mine.materials == null)
            mine.materials = new HashMap<>();
        mine.setAutoStart(plugin.getConfig().getBoolean("mine." + mineName + ".autostart"));
        mine.reset = plugin.getConfig().getLong("mine." + mineName + ".reset");
        return mine;
    }

    public void removeMine() {
        Main.tasks.remove(this.mineName);
        // Save mine data to a file or database
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
