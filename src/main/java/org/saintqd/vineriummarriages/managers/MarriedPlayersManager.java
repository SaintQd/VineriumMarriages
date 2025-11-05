package org.saintqd.vineriummarriages.managers;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import kotlin.Pair;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.saintqd.vineriumlib.utils.VinUtils;
import org.saintqd.vineriummarriages.VineriumMarriages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MarriedPlayersManager {

    private HashMap<String,String> marriedPlayerNames;
    private long acceptInviteTime = 300;
    private double offerMaxDistance = 10;
    private double kissMaxDistance = 3;
    private double kissHealAmount = 8;
    private long kissCooldown = 1200;
    private double woohooMaxDistance = 3;
    private int woohooHungerCost = 3;
    private boolean woohooBedNeeded = true;
    private long woohooLength = 200L;
    private long woohooCooldown = 6000L;
    private List<PotionEffect> woohooBuffs = new ArrayList<>();
    private List<String> woohooSounds = new ArrayList<>();
    private double woohooSoundMinPitch = 0.8;
    private double woohooSoundMaxPitch = 1.2;
    private HashMap<String,HashMap<Player, Pair<String,Long>>> timers;

    public HashMap<String, String> getMarriedPlayerNames() {
        return marriedPlayerNames;
    }

    public void updateParams(VineriumMarriages plugin) {
        this.timers = new HashMap<>();
        acceptInviteTime = plugin.getConfig().getLong("Marriages.AcceptInviteTime",300);
        offerMaxDistance = plugin.getConfig().getDouble("Marriages.OfferMaxDistance",10);
        kissMaxDistance = plugin.getConfig().getDouble("Marriages.KissMaxDistance",3);
        kissCooldown = plugin.getConfig().getLong("Marriages.KissCooldown",1200);
        kissHealAmount = plugin.getConfig().getDouble("Marriages.KissHealAmount",8);
        woohooMaxDistance = plugin.getConfig().getDouble("Marriages.WoohooMaxDistance",5);
        woohooHungerCost = plugin.getConfig().getInt("Marriages.WoohooHungerCost",10);
        woohooBedNeeded = plugin.getConfig().getBoolean("Marriages.WoohooBedNeeded",true);
        woohooLength = plugin.getConfig().getLong("Marriages.WoohooLength",200L);
        woohooCooldown = plugin.getConfig().getLong("Marriages.WoohooCooldown",6000L);
        woohooBuffs = new ArrayList<>();
        for (String buffInfo : plugin.getConfig().getStringList("Marriages.WoohooBuffs")) {
            String[] buffData = buffInfo.split(",");
            PotionEffectType effectType = RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).get(Key.key(buffData[0].toLowerCase()));
            int level = Integer.parseInt(buffData[1]);
            int duration = Integer.parseInt(buffData[2]);
            PotionEffect potionEffect = new PotionEffect(effectType,duration,level);
            woohooBuffs.add(potionEffect);
        }
        woohooSounds = new ArrayList<>();
        woohooSounds.addAll(plugin.getConfig().getStringList("Marriages.WoohooSounds"));
        woohooSoundMinPitch = plugin.getConfig().getDouble("Marriages.WoohooSoundMinPitch",0.8);
        woohooSoundMaxPitch = plugin.getConfig().getDouble("Marriages.WoohooSoundMaxPitch",1.2);
    }

    public void loadMarriedPlayerNames(VineriumMarriages plugin) {
        this.marriedPlayerNames = new HashMap<>();
        File marriedPlayerNamesFile = new File(plugin.getDataFolder().getPath() + File.separator + "MarriedPlayerNames.yml");
        File parent = marriedPlayerNamesFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            VinUtils.sendDebugMessage(0,"<red>Couldn't create married players file!");
            return;
        }
        YamlConfiguration marriedPlayersYaml = YamlConfiguration.loadConfiguration(marriedPlayerNamesFile);
        ConfigurationSection marriedPlayersConfig = marriedPlayersYaml.getConfigurationSection("MarriedPlayers");
        if (marriedPlayersConfig == null)
            return;
        for (String playerName : marriedPlayersConfig.getKeys(false)) {
            marriedPlayerNames.put(playerName,marriedPlayersConfig.getString(playerName));
        }
    }

    public void saveMarriedPlayerData(VineriumMarriages plugin) {
        File marriedPlayerNamesFile = new File(plugin.getDataFolder().getPath() + File.separator + "MarriedPlayerNames.yml");
        YamlConfiguration marriedPlayersYaml = YamlConfiguration.loadConfiguration(marriedPlayerNamesFile);
        marriedPlayersYaml.set("MarriedPlayers",null);
        try {
            if (!marriedPlayerNamesFile.exists() && !marriedPlayerNamesFile.createNewFile())
                VinUtils.sendDebugMessage(0,"<red>Couldn't save married players file to "+ marriedPlayerNamesFile +"!");
            for (String playerName : marriedPlayerNames.keySet()) {
                marriedPlayersYaml.set("MarriedPlayers."+playerName,marriedPlayerNames.get(playerName));
            }
            marriedPlayersYaml.save(marriedPlayerNamesFile);
        } catch (IOException e) {
            VinUtils.sendDebugMessage(0,"<red>Couldn't save married players file to "+ marriedPlayerNamesFile +"!");
        }
    }

    public long getAcceptInviteTime() {
        return acceptInviteTime;
    }

    public double getOfferMaxDistance() {
        return offerMaxDistance;
    }

    public double getKissMaxDistance() {
        return kissMaxDistance;
    }

    public double getWoohooMaxDistance() {
        return woohooMaxDistance;
    }

    public int getWoohooHungerCost() {
        return woohooHungerCost;
    }

    public long getWoohooLength() {
        return woohooLength;
    }

    public boolean isWoohooBedNeeded() {
        return woohooBedNeeded;
    }

    public double getKissHealAmount() {
        return kissHealAmount;
    }

    public List<PotionEffect> getWoohooBuffs() {
        return woohooBuffs;
    }

    public long getKissCooldown() {
        return kissCooldown;
    }

    public long getWoohooCooldown() {
        return woohooCooldown;
    }

    public HashMap<String,HashMap<Player, Pair<String,Long>>> getTimers() {
        return timers;
    }

    public List<String> getWoohooSounds() {
        return woohooSounds;
    }

    public double getWoohooSoundMinPitch() {
        return woohooSoundMinPitch;
    }

    public double getWoohooSoundMaxPitch() {
        return woohooSoundMaxPitch;
    }
}
