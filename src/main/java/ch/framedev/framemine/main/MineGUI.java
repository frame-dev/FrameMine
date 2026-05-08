package ch.framedev.framemine.main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MineGUI implements Listener {

    private static final int ITEMS_PER_PAGE = 45;
    private static final List<Material> SELECTABLE_MATERIALS = Arrays.stream(Material.values())
            .filter(material -> material != Material.AIR)
            .filter(Utils::isItem)
            .collect(Collectors.toList());
    private static final double[] CHANCES = {
            0.015, 0.05, 0.10, 0.25, 0.50, 0.75, 1.0, 1.25, 1.5,
            1.75, 2.0, 2.5, 5.0, 7.5, 10.0, 25.0, 30.0, 50.0, 60.0, 75.0, 100.0
    };

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

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, SELECTABLE_MATERIALS.size());

        for (int i = startIndex; i < endIndex; i++) {
            Material material = SELECTABLE_MATERIALS.get(i);
            gui.setItem(i - startIndex, createGuiItem(material, material.name()));
        }

        // Navigation items
        if (page > 0) {
            gui.setItem(45, createGuiItem(Material.ARROW, "Previous Page"));
        }
        if (endIndex < SELECTABLE_MATERIALS.size()) {
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
        gui.setItem(16, createGuiItem(Utils.material("CLOCK", "WATCH"), "Reset Time"));
        if (currentMine != null && currentMine.isAutoStart()) {
            gui.setItem(20, createGuiItem(Material.BLAZE_ROD, "Autostart", true, "§6Enabled!", "§aTo Disable click it."));
        } else {
            gui.setItem(20, createGuiItem(Material.BLAZE_ROD, "Autostart", false, "§cDisabled!", "§aTo Enable click it."));
        }
        gui.setItem(22, createGuiItem(Utils.material("COARSE_DIRT", "DIRT"), "Remove Mine"));
        gui.setItem(24, createGuiItem(Material.BEACON, "Info"));

        gui.setItem(31, createGuiItem(Material.BARRIER, "Back"));
        return gui;
    }

    public Inventory createMaterialsGUI(Mine mine) {
        Inventory gui = Bukkit.createInventory(null, 54, "Materials");
        for (String material : Utils.sortByValue(mine.getMaterials()).keySet()) {
            Material bukkitMaterial = Material.matchMaterial(material);
            if (bukkitMaterial != null) {
                gui.addItem(createGuiItem(bukkitMaterial, material + " - " + mine.getMaterials().get(material)));
            }
        }

        gui.setItem(49, createGuiItem(Material.BARRIER, "Back"));
        return gui;
    }

    public Inventory createResetTimeGUI() {
        Inventory gui = Bukkit.createInventory(null, 54, "Reset Time");

        gui.setItem(10, createGuiItem(Utils.material("CLOCK", "WATCH"), "1 Minute"));
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

        for (int i = 0; i < CHANCES.length; i++) {
            gui.setItem(i + 1, createGuiItem(Material.PAPER, CHANCES[i] + "%"));
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
        gui.setItem(12, createGuiItem(Utils.material("CHAIN_COMMAND_BLOCK", "COMMAND"), "Location 1",
                Utils.locationToPrettyList(currentMine.getPos1())));
        gui.setItem(14, createGuiItem(Utils.material("CHAIN_COMMAND_BLOCK", "COMMAND"), "Location 2",
                Utils.locationToPrettyList(currentMine.getPos2())));
        gui.setItem(16, createGuiItem(Material.CHEST, "Mine Materials", materials));
        gui.setItem(19, createGuiItem(Utils.material("CLOCK", "WATCH"), "Reset Time", false,
                "Reset Time : " + currentMine.getReset()));
        gui.setItem(21, createGuiItem(Utils.material("BLACKSTONE", "STONE"), "Autostart", false,
                "Autostart : " + currentMine.isAutoStart()));

        gui.setItem(49, createGuiItem(Material.BARRIER, "Back"));
        return gui;
    }


    private ItemStack createGuiItem(Material material, String name) {
        if (material == null) {
            material = Material.STONE;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, boolean enchanted, String... lore) {
        if (material == null) {
            material = Material.STONE;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            if (enchanted) {
                if (Utils.enchantment("INFINITY", "ARROW_INFINITE") != null) {
                    meta.addEnchant(Utils.enchantment("INFINITY", "ARROW_INFINITE"), 1, false);
                }
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                new ArrayList<>(meta.getEnchants().keySet()).forEach(meta::removeEnchant);
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        if (material == null) {
            material = Material.STONE;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore == null ? Collections.emptyList() : lore);
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
            return;
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
                        player.openInventory(fromMine ? createMaterialsGUI(mine) : createMineGUI());
                        fromMine = false;
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
            if (!itemName.endsWith("%")) {
                return;
            }
            chance = Double.parseDouble(itemName.replace("%", ""));
            event.getClickedInventory().setItem(0, createGuiItem(Utils.material("CHERRY_SIGN", "OAK_SIGN", "SIGN"), "Current Chance: " + chance));
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
            if (currentMine == null) {
                player.sendMessage("No mine selected.");
                return;
            }
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
        if (currentMine == null) {
            player.sendMessage("No mine selected.");
            return;
        }
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
                fromMine = false;
                player.openInventory(createAddMaterialsGUI(0));
                break;
            case "Start":
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
                Sound levelUp = Utils.sound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
                if (levelUp != null) {
                    player.playSound(player.getLocation(), levelUp, 20, 1);
                }
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
            return;
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
                    selectedMaterial = Material.matchMaterial(materialName);
                    if (selectedMaterial == null) {
                        player.sendMessage("Invalid material.");
                        return;
                    }
                    fromMine = false;
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
