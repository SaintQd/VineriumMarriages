package org.saintqd.vineriummarriages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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
}
