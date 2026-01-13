package org.saintqd.vineriummarriages;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.saintqd.vineriumlib.VineriumLib;
import org.saintqd.vineriumlib.utils.ResourceUtils;
import org.saintqd.vineriumlib.utils.VinUtils;
import org.saintqd.vineriummarriages.commands.MarryCommandsManager;
import org.saintqd.vineriummarriages.listeners.PlayerListener;
import org.saintqd.vineriummarriages.managers.MarriedPlayersManager;
import org.saintqd.vineriummarriages.placeholders.VinMarriagePlaceholders;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class VineriumMarriages extends JavaPlugin {

    private static VineriumMarriages plugin;
    private MarriedPlayersManager marriedPlayersManager;
    private VinMarriagePlaceholders placeholders = null;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        try {
            ResourceUtils.fetchAllResources(this,getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.marriedPlayersManager = new MarriedPlayersManager();

        loadData();

        MarryCommandsManager.setupCommands(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // Подключаем плейсхолдеры
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholders = new VinMarriagePlaceholders(this);
            placeholders.register();
        } else {
            placeholders = null;
            VinUtils.sendDebugMessage(0,"<yellow>Could not find PlaceholderAPI! Placeholders won't be registered.");
        }

        //Создаем задачу регулярного сохранения данных раз в полчаса
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::saveData, 36000L, 36000L);
    }

    @Override
    public void onDisable() {
        saveData();
        VinUtils.updateJarFile(this,this.getFile());
    }

    public void loadData() {
        reloadConfig();

        String selectedLang = getConfig().getString("Marriages.Language");
        HashMap<Key,String> langLines = VineriumLib.inst().getLangManager().loadLanguageFile(this,
                plugin.getDataFolder().getPath() + File.separator + "lang" + File.separator + selectedLang + ".yml");
        VineriumLib.inst().getLangManager().registerLangLines(langLines);

        long startTime = System.currentTimeMillis();
        long prevTime = startTime;

        marriedPlayersManager.updateParams(this);
        marriedPlayersManager.loadMarriedPlayerNames(this);
        marriedPlayersManager.getTimers().clear();
        long time = System.currentTimeMillis();
        VinUtils.sendDebugMessage(0,"Loaded " + marriedPlayersManager.getMarriedPlayerNames().size() + " married players. ("+(time-prevTime)+" ms)");
        prevTime = System.currentTimeMillis();
    }

    public void saveData() {
        VinUtils.sendDebugMessage(0,"Saving married players data...");
        marriedPlayersManager.saveMarriedPlayerData(this);
        VinUtils.sendDebugMessage(0,"Saved "+marriedPlayersManager.getMarriedPlayerNames().size()+" married players.");
    }

    public static VineriumMarriages inst() {
        return plugin;
    }

    public MarriedPlayersManager getMarriedPlayersManager() {
        return marriedPlayersManager;
    }


}
