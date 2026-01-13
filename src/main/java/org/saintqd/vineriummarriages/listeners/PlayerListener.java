package org.saintqd.vineriummarriages.listeners;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.saintqd.vineriumlib.utils.VinUtils;
import org.saintqd.vineriummarriages.VineriumMarriages;
import org.saintqd.vineriummarriages.managers.MarriedPlayersManager;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();
        if (!marriedPlayersManager.getMarriedPlayerNames().containsKey(player.getName())) return;

        String secondPlayerName = marriedPlayersManager.getMarriedPlayerNames().get(player.getName());
        String secondPlayerPartner = marriedPlayersManager.getMarriedPlayerNames().get(secondPlayerName);
        if (secondPlayerPartner == null || !secondPlayerPartner.equals(player.getName())) {
            player.sendMessage(VinUtils.parseString("<red>Вы больше не состоите в браке с <yellow>"+secondPlayerName+"<gold>."));
            marriedPlayersManager.getMarriedPlayerNames().remove(player.getName());
        }
    }

    @EventHandler
    public void onPlayerCandleUse(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player secondPlayer)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!VineriumMarriages.inst().getConfig().getBoolean("Marriages.CandleDamage.Enabled",true)) return;
        if (VineriumMarriages.inst().getConfig().getBoolean("Marriages.CandleDamage.RequiresSneaking",true) && !event.getPlayer().isSneaking()) return;
        if (!event.getPlayer().getInventory().getItemInMainHand().getType().name().contains("CANDLE")) return;

        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();
        if (!marriedPlayersManager.getMarriedPlayerNames().containsKey(event.getPlayer().getName())) return;

        String secondPlayerName = marriedPlayersManager.getMarriedPlayerNames().get(event.getPlayer().getName());
        if (secondPlayerName != null && secondPlayerName.equals(secondPlayer.getName())) {
            secondPlayer.damage(1,event.getPlayer());
            secondPlayer.getWorld().playSound(secondPlayer.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, SoundCategory.PLAYERS,1f,1f);
            secondPlayer.getWorld().spawnParticle(Particle.HEART, secondPlayer.getLocation().clone().add(0.0, 1.0, 0.0), 10, 0.25, 0.5, 0.25, 0.0);
            secondPlayer.getWorld().spawnParticle(Particle.FALLING_WATER, secondPlayer.getLocation().clone().add(0.0, 0.6, 0.0), 10, 0.1, 0.0, 0.1, 0.0);
        }
    }
}
