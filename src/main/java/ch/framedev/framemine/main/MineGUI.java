package ch.framedev.framemine.main;

/*
 * ch.framedev.framemine.main
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 14.07.2024 20:21
 */

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MineGUI implements Listener {

    private final Main plugin;
    private Mine currentMine;
    private Material selectedMaterial;
    private boolean fromMine;


    public MineGUI(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void setCurrentMine(Mine currentMine) {
        this.currentMine = currentMine;
    }

    public Mine getCurrentMine() {
        return currentMine;
    }

    public Inventory createMineSetupGUI() {
        Inventory gui = Bukkit.createInventory(null, 4 * 9, "Mine Setup");

        // 0 1 2 3 4 5 6 7 8
        // 9 10 11 12 13 14 15 16 17
        // 18 19 20 21 22 23 24 25 26
        // 27 28 29 30 31 32 33 34 35
        // 36 37 38 39 40 41 42 43 44

        // Add items to the GUI
        gui.setItem(10, createGuiItem(Material.DIAMOND_PICKAXE, "Set Position 1"));
        gui.setItem(12, createGuiItem(Material.IRON_PICKAXE, "Set Position 2"));
        gui.setItem(14, createGuiItem(Material.CHEST, "Setup Mine"));
        // gui.setItem(16, createGuiItem(Material.CHEST, "Add Materials"));

        gui.setItem(16, createGuiItem(Material.GOLD_ORE, "Mine Selection"));

        return gui;
    }

    public Inventory createAddMaterialsGUI(int page) {
        Inventory gui = Bukkit.createInventory(null, 54, "Add Materials - Page " + (page + 1));

        // Get materials for this page
        List<Material> materials = new ArrayList<>(List.of(Material.values()));
        // 5 rows for items, 1 row for navigation
        final int ITEMS_PER_PAGE = 45;
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, materials.size());

        for (int i = startIndex; i < endIndex; i++) {
            gui.setItem(i - startIndex, createGuiItem(materials.get(i), materials.get(i).name()));
        }

        // Navigation items
        if (page > 0) {
            gui.setItem(45, createGuiItem(Material.ARROW, "Previous Page"));
        }
        if (endIndex < materials.size()) {
            gui.setItem(53, createGuiItem(Material.ARROW, "Next Page"));
        }

        gui.setItem(49, createGuiItem(Material.BARRIER, "Back"));

        return gui;
    }

    public Inventory createSelectionMineGUI() {
        Inventory gui = Bukkit.createInventory(null, 54, "Mine Selection");
        List<Mine> mines = plugin.getMineList();
        for (Mine mine : mines) {
            gui.addItem(createGuiItem(Material.CHEST, mine.getMineName()));
        }
        gui.setItem(49, createGuiItem(Material.BARRIER, "Back"));
        return gui;
    }

    public Inventory createMineGUI() {
        Inventory gui = Bukkit.createInventory(null, 4 * 9, "Mine");
        // 0 1 2 3 4 5 6 7 8
        // 9 10 11 12 13 14 15 16 17
        // 18 19 20 21 22 23 24 25 26
        // 27 28 29 30 31 32 33 34 35
        gui.setItem(10, createGuiItem(Material.CHEST, "Materials"));
        gui.setItem(12, createGuiItem(Material.STONE, "Add Materials"));
        gui.setItem(14, createGuiItem(Material.DIAMOND_ORE, "Start"));
        gui.setItem(16, createGuiItem(Material.CLOCK, "Reset Time"));
        if (currentMine != null && currentMine.isAutoStart()) {
            gui.setItem(20, createGuiItem(Material.BLAZE_ROD, "Autostart", true, "§6Enabled!", "§aTo Disable click it."));
        } else {
            gui.setItem(20, createGuiItem(Material.BLAZE_ROD, "Autostart", false, "§cDisabled!", "§aTo Enable click it."));
        }
        gui.setItem(22, createGuiItem(Material.COARSE_DIRT, "Remove Mine"));
        gui.setItem(24, createGuiItem(Material.BEACON, "Info"));

        gui.setItem(31, createGuiItem(Material.BARRIER, "Back"));
        return gui;
    }

    public Inventory createMaterialsGUI(Mine mine) {
        Inventory gui = Bukkit.createInventory(null, 54, "Materials");
        for (String material : Utils.sortByValue(mine.getMaterials()).keySet())
            gui.addItem(createGuiItem(Material.getMaterial(material), material + " - " + mine.getMaterials().get(material)));

        gui.setItem(49, createGuiItem(Material.BARRIER, "Back"));
        return gui;
    }

    public Inventory createResetTimeGUI() {
        Inventory gui = Bukkit.createInventory(null, 54, "Reset Time");

        gui.setItem(10, createGuiItem(Material.CLOCK, "1 Minute"));
        gui.setItem(12, createGuiItem(Material.DIAMOND_BLOCK, "5 Minutes"));
        gui.setItem(14, createGuiItem(Material.DIAMOND_BLOCK, "10 Minutes"));
        gui.setItem(16, createGuiItem(Material.DIAMOND_BLOCK, "15 Minutes"));
        gui.setItem(19, createGuiItem(Material.DIAMOND_BLOCK, "20 Minutes"));
        gui.setItem(21, createGuiItem(Material.DIAMOND_BLOCK, "25 Minutes"));
        gui.setItem(23, createGuiItem(Material.DIAMOND_BLOCK, "30 Minutes"));
        gui.setItem(25, createGuiItem(Material.DIAMOND_BLOCK, "60 Minutes"));
        gui.setItem(28, createGuiItem(Material.DIAMOND_BLOCK, "120 Minutes"));
        gui.setItem(30, createGuiItem(Material.DIAMOND_BLOCK, "240 Minutes"));

        gui.setItem(49, createGuiItem(Material.BARRIER, "Back"));
        return gui;
    }

    public Inventory createChanceSelectionGUI() {
        Inventory gui = Bukkit.createInventory(null, 4 * 9, "Select Chance");

        // Predefined chance values
        double[] chances = {0.015, 0.05, 0.10, 0.25, 0.50, 0.75, 1.0, 1.25, 1.5,
                1.75, 2.0, 2.5, 5.0, 7.5, 10.0, 25.0, 30.0, 50.0, 60.0, 75.0, 100.0};

        // Populate the GUI with chance items
        for (int i = 1; i < chances.length; i++) {
            gui.setItem(i, createGuiItem(Material.PAPER, chances[i - 1] + "%"));
        }

        gui.setItem(gui.getSize() - 4, createGuiItem(Material.DIAMOND, "Set Chance"));
        // Add Back button
        gui.setItem(gui.getSize() - 1, createGuiItem(Material.BARRIER, "Back"));

        return gui;
    }

    public Inventory createInfoGUI() {
        Inventory gui = Bukkit.createInventory(null, 54, "Info Inventory");
        if (currentMine == null) return gui;

        gui.setItem(10, createGuiItem(Material.CHEST, "Mine Name", false, currentMine.getMineName()));
        List<String> materials = new ArrayList<>();
        for (Map.Entry<String, Double> entry : currentMine.getMaterials().entrySet()) {
            materials.add("§6" + entry.getKey() + "§c:§b" + entry.getValue() + "%" + "\n");
        }
        gui.setItem(12, createGuiItem(Material.CHAIN_COMMAND_BLOCK, "Location 1",
                Utils.locationToPrettyList(currentMine.getPos1())));
        gui.setItem(14, createGuiItem(Material.CHAIN_COMMAND_BLOCK, "Location 2",
                Utils.locationToPrettyList(currentMine.getPos2())));
        gui.setItem(16, createGuiItem(Material.CHEST, "Mine Materials", materials));
        gui.setItem(19, createGuiItem(Material.CLOCK, "Reset Time", false,
                "Reset Time : " + currentMine.getReset()));
        gui.setItem(21, createGuiItem(Material.BLACKSTONE, "Autostart", false,
                "Autostart : " + currentMine.isAutoStart()));

        gui.setItem(49, createGuiItem(Material.BARRIER, "Back"));
        return gui;
    }


    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, boolean enchanted, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(List.of(lore));
            if (enchanted) {
                meta.addEnchant(Enchantment.INFINITY, 1, false);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                meta.removeEnchantments();
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Mine Setup")) {
            handleMineSetupClick(event);
        } else if (event.getView().getTitle().startsWith("Add Materials")) {
            handleAddMaterialsClick(event);
        } else if (event.getView().getTitle().equalsIgnoreCase("Mine Selection")) {
            handleSelectMineEvent(event);
        } else if (event.getView().getTitle().equalsIgnoreCase("Mine")) {
            handleMineClick(event);
        } else if (event.getView().getTitle().equalsIgnoreCase("Materials")) {
            handleMaterialsClick(event);
        } else if (event.getView().getTitle().equalsIgnoreCase("Reset Time")) {
            handleResetTimeEvent(event);
        } else if (event.getView().getTitle().equalsIgnoreCase("Select Chance")) {
            handleChanceSelectionEvent(event);
        } else if (event.getView().getTitle().equalsIgnoreCase("Info Inventory")) {
            handleInfoInventoryClick(event);
        }
    }

    private void handleInfoInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent the player from taking the items

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null || event.getClickedInventory() == null)
            return;
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (itemName.equalsIgnoreCase("Back")) {
            player.openInventory(createMineGUI());
        }
    }

    private void handleMaterialsClick(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent the player from taking the items

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null || event.getClickedInventory() == null)
            return;
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        Material material = event.getCurrentItem().getType();

        if (itemName.equalsIgnoreCase("Back")) {
            player.openInventory(createMineGUI());
        }
        if (event.getClick() == ClickType.SHIFT_RIGHT) {
            Mine mine = currentMine;
            if (mine != null) {
                mine.removeMaterial(material);
                mine.save();
                player.sendMessage("Removed " + material.name() + " from the mine.");
                currentMine = mine;
                player.openInventory(createMaterialsGUI(mine));
            } else {
                player.sendMessage("No mine selected.");
            }
            return;
        }

        if (event.getClick() == ClickType.SHIFT_LEFT) {
            Mine mine = currentMine;
            if (mine != null) {
                double currentChance = mine.getMaterials().getOrDefault(material.name(), 0.0);
                mine.addMaterial(material, currentChance / 2); // Assuming you want to double the chance
                mine.save();
                player.sendMessage("Half the chance of " + material.name() + " in the mine.");
                player.openInventory(createMaterialsGUI(mine));
                currentMine = mine;
            } else {
                player.sendMessage("No mine selected.");
            }
        }
        if (event.getClick() == ClickType.MIDDLE) {
            selectedMaterial = material;
            fromMine = true;
            player.openInventory(createChanceSelectionGUI());
        }

    }

    private void handleChanceSelectionEvent(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent the player from taking the items

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null || event.getClickedInventory() == null)
            return;
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        ItemStack chanceItem = event.getClickedInventory().getItem(0);
        if(chanceItem != null && chanceItem.getItemMeta() == null) return;
        ItemMeta chanceItemMeta = chanceItem != null ? chanceItem.getItemMeta() : null;
        double chance;
        if (itemName.equalsIgnoreCase("Back")) {
            if (!fromMine)
                player.openInventory(createAddMaterialsGUI(0));
            else {
                player.openInventory(createMineGUI());
                fromMine = false;
            }
        } else if (itemName.equalsIgnoreCase("Set Chance")) {
            try {
                Material material = selectedMaterial;
                if (material != null) {
                    Mine mine = getCurrentMine();
                    if (mine != null) {
                        if (event.getClickedInventory().getItem(0) == null) return;
                        double newChance = 0;
                        if (chanceItemMeta != null) {
                            newChance = Double.parseDouble(chanceItemMeta.getDisplayName()
                                    .replace("Current Chance: ", "")
                                    .replace("%", ""));
                        }
                        mine.addMaterial(material, newChance);
                        mine.save();
                        player.sendMessage("Set chance for " + material.name() + " to " + newChance + "%");
                    } else {
                        player.sendMessage("No mine selected.");
                    }
                } else {
                    player.sendMessage("No material selected.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid Number");
            }
        } else {
            chance = Double.parseDouble(itemName.replace("%", ""));
            event.getClickedInventory().setItem(0, createGuiItem(Material.CHERRY_SIGN, "Current Chance: " + chance));
            System.out.println(chance);
        }
    }

    private void handleResetTimeEvent(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent the player from taking the items

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (itemName.equalsIgnoreCase("Back")) {
            player.openInventory(createMineGUI());
        } else {
            String[] time = itemName.split(" ");
            long resetTime = Long.parseLong(time[0]);
            currentMine.setReset(resetTime);
            currentMine.save();
            player.sendMessage("Reset time set to " + resetTime + " Minutes.");
        }
    }

    private void handleMineClick(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent the player from taking the items

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        ItemStack item = event.getCurrentItem();
        Inventory inventory = event.getClickedInventory();
        if (item == null || inventory == null) return;
        Mine mine = Mine.loadMine(currentMine.getMineName());
        if (mine == null) {
            player.sendMessage("Mine not found!");
            return;
        }
        switch (itemName) {
            case "Back":
                player.openInventory(createSelectionMineGUI());
                break;
            case "Materials":
                player.openInventory(createMaterialsGUI(mine));
                break;
            case "Add Materials":
                player.openInventory(createAddMaterialsGUI(0));
                break;
            case "Start":
                if (Main.tasks.containsKey(mine.getMineName())) {
                    Main.tasks.get(mine.getMineName()).cancel();
                    Main.tasks.remove(mine.getMineName());
                }
                mine.startAutoReset(plugin);
                break;
            case "Reset Time":
                player.openInventory(createResetTimeGUI());
                break;
            case "Autostart":
                // Ensure the item has metadata and enchants before proceeding
                if (item.hasItemMeta()) {
                    if (mine.isAutoStart()) {
                        // If auto-start is enabled, disable it and update the inventory item
                        mine.setAutoStart(false);
                        inventory.setItem(event.getSlot(),
                                createGuiItem(Material.BLAZE_ROD, "Autostart", false, "§cDisabled!",
                                        "§aTo Enable click it."));
                        player.sendMessage("§6Auto-start has been disabled.");
                    } else {
                        // If auto-start is disabled, enable it and update the inventory item
                        mine.setAutoStart(true);
                        inventory.setItem(event.getSlot(),
                                createGuiItem(Material.BLAZE_ROD, "Autostart", true, "§6Enabled!",
                                        "§aTo Disable click it."));
                        player.sendMessage("§6Auto-start has been enabled.");
                    }

                    // Save the mine state
                    mine.save();

                    // Update current mine reference
                    currentMine = mine;

                    // Ensure the player's inventory is updated
                    //noinspection UnstableApiUsage
                    player.updateInventory();
                } else {
                    player.sendMessage("§cError: Item does not have metadata.");
                }
                break;
            case "Info":
                player.openInventory(createInfoGUI());
                break;
            case "Remove Mine":
                mine.removeMine();
                player.sendMessage(plugin.getPrefix() + "Mine has been removed!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 20, 1);
                player.openInventory(createSelectionMineGUI());
                break;
        }
    }

    private void handleMineSetupClick(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent the player from taking the items

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

        switch (itemName) {
            case "Set Position 1":
                plugin.getMineCMD().setPos1(player.getLocation());
                player.sendMessage("Position 1 set.");
                break;
            case "Set Position 2":
                plugin.getMineCMD().setPos2(player.getLocation());
                player.sendMessage("Position 2 set.");
                break;
            case "Setup Mine":
                if (plugin.getMineCMD().getPos1() != null && plugin.getMineCMD().getPos2() != null) {
                    event.getWhoClicked().sendMessage("Enter the Mine Name in chat:");
                    plugin.getChatListener().setWaitingForCreate(player);
                } else {
                    player.sendMessage("You need to set both positions.");
                }
                break;
            case "Add Materials":
                player.sendMessage("Enter the Mine Name in chat:");
                plugin.getChatListener().setWaitingForMineName(player);
                break;
            case "Mine Selection":
                player.openInventory(createSelectionMineGUI());
        }
    }

    private void handleSelectMineEvent(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent the player from taking the items

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        Player player = (Player) event.getWhoClicked();
        String mineName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (mineName.equalsIgnoreCase("Back")) {
            player.openInventory(createMineSetupGUI());
        }

        Mine mine = Mine.loadMine(mineName);
        if (mine != null) {
            plugin.getMineGUI().setCurrentMine(mine);
            player.sendMessage(mineName + " has been successfully selected!");
            player.openInventory(createMineGUI());
        }
    }

    private void handleAddMaterialsClick(InventoryClickEvent event) {
        event.setCancelled(true); // Prevent the player from taking the items

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String materialName = event.getCurrentItem().getItemMeta().getDisplayName();

        int page = getPageFromTitle(title);

        switch (materialName) {
            case "Back":
                player.openInventory(createMineGUI());
                break;
            case "Previous Page":
                player.openInventory(createAddMaterialsGUI(page - 1));
                break;
            case "Next Page":
                player.openInventory(createAddMaterialsGUI(page + 1));
                break;
            default:
                try {
                    selectedMaterial = Material.valueOf(materialName);
                    player.openInventory(createChanceSelectionGUI());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Invalid material.");
                }
        }
    }

    private int getPageFromTitle(String title) {
        String[] parts = title.split(" ");
        try {
            return Integer.parseInt(parts[parts.length - 1]) - 1;
        } catch (NumberFormatException e) {
            return 0; // Default to page 0 if parsing fails
        }
    }
}
