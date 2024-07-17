package ch.framedev.framemine.main;

/*
 * ch.framedev.framemine.main
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 14.07.2024 19:41
 */

import ch.framedev.spigotutils.SpigotAPI;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    public static String locationToString(Location location) {
        return new SpigotAPI().locationToString(location);
    }

    public static Location stringToLocation(String locationString) {
        return new SpigotAPI().stringToLocation(locationString);
    }

    @SuppressWarnings("unused")
    public static String locationToPrettyString(Location location) {
        if(location.getWorld() == null) return null;
        String locationString = "";
        locationString += "World : " + location.getWorld().getName() + "\n";
        locationString += "X : " + location.getBlockX() + "\n";
        locationString += "Y : " + location.getBlockY() + "\n";
        locationString += "Z : " + location.getBlockZ() + "\n";
        locationString += "Yaw : " + location.getYaw() + "\n";
        locationString += "Pitch : " + location.getPitch();
        return locationString;
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
        return materials.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        // Step 1: Convert Map to List of Map entries
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());

        // Step 2: Sort the list with a custom comparator
        list.sort(Map.Entry.comparingByValue());

        // Step 3: Create a new LinkedHashMap and put sorted entries into it
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}

