package ch.framedev.framemine.main;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Main extends JavaPlugin {

    private static final Map<String, BukkitTask> RESET_TASKS = new ConcurrentHashMap<>();
    private static Main instance;
    private MineCMD mineCMD;
    private MineGUI mineGUI;
    private ChatListener chatListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        if (getCommand("mine") == null) {
            getLogger().severe("Command 'mine' is missing from plugin.yml. Disabling FrameMine.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
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
        cancelAllResetTasks();
    }

    public static Main getInstance() {
        return instance;
    }

    public static void registerResetTask(String mineName, BukkitTask task) {
        cancelResetTask(mineName);
        RESET_TASKS.put(mineName, task);
    }

    public static void cancelResetTask(String mineName) {
        BukkitTask task = RESET_TASKS.remove(mineName);
        if (task != null) {
            task.cancel();
        }
    }

    private static void cancelAllResetTasks() {
        RESET_TASKS.values().forEach(BukkitTask::cancel);
        RESET_TASKS.clear();
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
        if (getConfig().getConfigurationSection("mine") != null) {
            for (String mineName : getConfig().getConfigurationSection("mine").getKeys(false)) {
                Mine mine = Mine.loadMine(mineName);
                if (mine != null) {
                    mines.add(mine);
                }
            }
        }
        return mines;
    }

    public String getPrefix() {
        return "§7[§bFrameMine§7] §c» §7";
    }
}
