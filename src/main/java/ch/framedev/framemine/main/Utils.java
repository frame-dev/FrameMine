package ch.framedev.framemine.main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Utils {

    private Utils() {
    }

    public static Material material(String... names) {
        for (String name : names) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                return material;
            }
        }
        return Material.STONE;
    }

    public static Enchantment enchantment(String... names) {
        for (String name : names) {
            Enchantment enchantment = Enchantment.getByName(name);
            if (enchantment != null) {
                return enchantment;
            }
        }
        return null;
    }

    public static Sound sound(String... names) {
        for (String name : names) {
            try {
                return Sound.valueOf(name);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public static boolean isItem(Material material) {
        try {
            Method isItemMethod = Material.class.getMethod("isItem");
            return (Boolean) isItemMethod.invoke(material);
        } catch (NoSuchMethodException ignored) {
            return material != Material.AIR;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return material != Material.AIR;
        }
    }

    public static String locationToString(Location location) {
        if(location.getWorld() == null) return null;
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    public static Location stringToLocation(String locationString) {
        String[] parts = locationString.split("[,;]");
        if (parts.length != 6) return null;

        try {
            String worldName = parts[0].trim();
            if (Bukkit.getWorld(worldName) == null) {
                return null;
            }

            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            float yaw = Float.parseFloat(parts[4].trim());
            float pitch = Float.parseFloat(parts[5].trim());

            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String locationToPrettyString(Location location) {
        if(location.getWorld() == null) return null;
        return String.join("\n", locationToPrettyList(location));
    }

    public static List<String> locationToPrettyList(Location location) {
        if(location.getWorld() == null) return null;
        List<String> locationList = new ArrayList<>();
        locationList.add("World : " + location.getWorld().getName());
        locationList.add("X : " + location.getBlockX());
        locationList.add("Y : " + location.getBlockY());
        locationList.add("Z : " + location.getBlockZ());
        locationList.add("Yaw : " + location.getYaw());
        locationList.add("Pitch : " + location.getPitch());
        return locationList;
    }

    public static String formatMaterialsMap(Map<String, Double> materials) {
        if (materials == null || materials.isEmpty()) {
            return "None";
        }
        return materials.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
