package org.saintqd.vineriummarriages;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.saintqd.vineriumlib.VineriumLib;
import org.saintqd.vineriumlib.utils.VinUtils;
import org.saintqd.vineriummarriages.commands.MarryCommandsManager;
import org.saintqd.vineriummarriages.listeners.PlayerListener;
import org.saintqd.vineriummarriages.managers.MarriedPlayersManager;
import org.saintqd.vineriummarriages.placeholders.VinMarriagePlaceholders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;

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

        this.marriedPlayersManager = new MarriedPlayersManager();

        setupDefaultConfig();

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
        if (selectedLang != null) {
            File langFile = new File(plugin.getDataFolder().getPath() + File.separator + "lang" + File.separator + selectedLang + ".yml");
            if (!langFile.exists() && langFile.mkdirs()) {
                InputStream langStream = VineriumMarriages.class.getResourceAsStream("/lang/"+selectedLang+".yml");
                if (langStream != null) {
                    try {
                        Files.copy(langStream, Path.of(getDataFolder().getPath() + File.separator + "lang" + File.separator + selectedLang + ".yml"), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            HashMap<String,String> langLines = VineriumLib.inst().getLangManager().loadLanguageFile(this,"lang" + File.separator + selectedLang + ".yml");
            VineriumLib.inst().getLangManager().registerLangLines(this,langLines);
        }

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

    private void setupDefaultConfig() {

        FileConfiguration config = this.getConfig();

        config.addDefault("Marriages.Language","ru_ru");
        config.addDefault("Marriages.AcceptInviteTime",2400L);
        config.addDefault("Marriages.OfferMaxDistance",10.0);
        config.addDefault("Marriages.KissMaxDistance",3);
        config.addDefault("Marriages.KissHealAmount",8);
        config.addDefault("Marriages.KissCooldown",1200L);
        config.addDefault("Marriages.WoohooMaxDistance",5);
        config.addDefault("Marriages.WoohooHungerCost",10);
        config.addDefault("Marriages.WoohooBedNeeded",true);
        config.addDefault("Marriages.WoohooLength",200L);
        config.addDefault("Marriages.WoohooBuffs",List.of("SPEED,0,1200","RESISTANCE,0,2400"));
        config.addDefault("Marriages.WoohooCooldown",6000L);
        config.addDefault("Marriages.WoohooSounds",List.of("entity.villager.trade","entity.villager.celebrate","entity.villager.no"));

        config.options().copyDefaults(true);
        this.saveConfig();
    }

    public MarriedPlayersManager getMarriedPlayersManager() {
        return marriedPlayersManager;
    }


}
