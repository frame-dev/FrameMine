package ch.framedev.framemine.main;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class Main extends JavaPlugin {

    public static HashMap<String, BukkitTask>  tasks = new HashMap<>();
    private static Main instance;
    private MineCMD mineCMD;
    private MineGUI mineGUI;
    private ChatListener chatListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        mineCMD = new MineCMD(this);
        chatListener = new ChatListener(this);
        mineGUI = new MineGUI(this);
        new PositionListener(this);

        List<Mine> mines = getMineList();
        for (Mine mine : mines) {
            if (mine.isAutoStart())
                mine.startAutoReset(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getInstance() {
        return instance;
    }

    public MineCMD getMineCMD() {
        return mineCMD;
    }

    public MineGUI getMineGUI() {
        return mineGUI;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public List<Mine> getMineList() {
        List<Mine> mines = new ArrayList<>();
        if (getConfig().getConfigurationSection("mine") != null)
            for (String mineName : Objects.requireNonNull(getConfig().getConfigurationSection("mine")).getKeys(false)) {
                if (Mine.loadMine(mineName) != null)
                    mines.add(Mine.loadMine(mineName));
            }
        return mines;
    }

    public String getPrefix() {
        return "§7[§bFrameMine§7] §c» §7";
    }
}
