package org.saintqd.vineriummarriages.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import kotlin.Pair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.intellij.lang.annotations.Subst;
import org.saintqd.vineriumlib.VineriumLib;
import org.saintqd.vineriumlib.utils.VinUtils;
import org.saintqd.vineriummarriages.VineriumMarriages;
import org.saintqd.vineriummarriages.managers.MarriedPlayersManager;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class MarryCommandsManager {

    public static void setupCommands(VineriumMarriages plugin) {

        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("vinmarry")
                            .executes(ctx -> {
                                helpCommand(ctx.getSource().getSender(),1);
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.literal("reload")
                                    .requires(predicate -> predicate.getSender().hasPermission("vineriummarriages.admin"))
                                    .executes(ctx -> {
                                        reloadCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("savedata")
                                    .requires(predicate -> predicate.getSender().hasPermission("vineriummarriages.admin"))
                                    .executes(ctx -> {
                                        saveDataCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("help")
                                    .executes(ctx -> {
                                        helpCommand(ctx.getSource().getSender(),1);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("page", IntegerArgumentType.integer(0,2))
                                            .executes(ctx -> {
                                                helpCommand(ctx.getSource().getSender(),ctx.getArgument("page",Integer.class));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("offer")
                                    .requires(predicate -> predicate.getSender().hasPermission("vineriummarriages.offer"))
                                    .then(Commands.argument("player", ArgumentTypes.player())
                                            .executes(ctx -> {
                                                offerCommand(ctx.getSource().getSender(),ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst(),null,false);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                            .then(Commands.argument("secondplayer",ArgumentTypes.player())
                                                    .requires(predicate -> predicate.getSender().hasPermission("vineriummarriages.admin"))
                                                    .executes(ctx -> {
                                                        offerCommand(
                                                                ctx.getSource().getSender(),
                                                                ctx.getLastChild().getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst(),
                                                                ctx.getArgument("secondplayer", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst(),false
                                                        );
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                                    .then(Commands.argument("silent", BoolArgumentType.bool())
                                                            .executes(ctx -> {
                                                                offerCommand(
                                                                        ctx.getSource().getSender(),
                                                                        ctx.getLastChild().getLastChild().getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst(),
                                                                        ctx.getLastChild().getArgument("secondplayer", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst(),
                                                                        ctx.getArgument("silent",Boolean.class)
                                                                );
                                                                return Command.SINGLE_SUCCESS;
                                                            })
                                                    )
                                            )
                                    )
                            )
                            .then(Commands.literal("divorce")
                                    .requires(predicate -> predicate.getSender().hasPermission("vineriummarriages.divorce"))
                                    .executes(ctx -> {
                                        divorceCommand(ctx.getSource().getSender(),null);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(Commands.argument("player",ArgumentTypes.player())
                                            .requires(predicate -> predicate.getSender().hasPermission("vineriummarriages.admin"))
                                            .executes(ctx -> {
                                                divorceCommand(ctx.getSource().getSender(),ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst());
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("accept")
                                    .requires(predicate -> predicate.getSender() instanceof Player player
                                            && !VineriumMarriages.inst().getMarriedPlayersManager().getMarriedPlayerNames().containsKey(player.getName()))
                                    .executes(ctx -> {
                                        acceptMarryCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("decline")
                                    .requires(predicate -> predicate.getSender() instanceof Player)
                                    .executes(ctx -> {
                                        declineMarryCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("confirm")
                                    .requires(predicate -> predicate.getSender() instanceof Player)
                                    .executes(ctx -> {
                                        confirmCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("ender")
                                    .requires(predicate -> predicate.getSender() instanceof Player player
                                            && predicate.getSender().hasPermission("vineriummarriages.ender")
                                            && VineriumMarriages.inst().getMarriedPlayersManager().getMarriedPlayerNames().containsKey(player.getName()))
                                    .executes(ctx -> {
                                        enderChestCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("tp")
                                    .requires(predicate -> predicate.getSender() instanceof Player player
                                            && predicate.getSender().hasPermission("vineriummarriages.tp")
                                            && VineriumMarriages.inst().getMarriedPlayersManager().getMarriedPlayerNames().containsKey(player.getName()))
                                    .executes(ctx -> {
                                        teleportCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("kiss")
                                    .requires(predicate -> predicate.getSender() instanceof Player player
                                            && predicate.getSender().hasPermission("vineriummarriages.kiss")
                                            && VineriumMarriages.inst().getMarriedPlayersManager().getMarriedPlayerNames().containsKey(player.getName()))
                                    .executes(ctx -> {
                                        kissCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(Commands.literal("woohoo")
                                    .requires(predicate -> predicate.getSender() instanceof Player player
                                            && predicate.getSender().hasPermission("vineriummarriages.woohoo")
                                            && VineriumMarriages.inst().getMarriedPlayersManager().getMarriedPlayerNames().containsKey(player.getName()))
                                    .executes(ctx -> {
                                        woohooCommand(ctx.getSource().getSender());
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .build(),
                    "Основная команда браков."
            );}
        );
    }

    private static void reloadCommand(CommandSender sender) {
        VineriumMarriages.inst().loadData();
        sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_reload"));
    }

    private static void saveDataCommand(CommandSender sender) {
        VineriumMarriages.inst().saveData();
        sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_save_data"));
    }

    private static void helpCommand(CommandSender sender, int page) {
        if (page < 0 || page > 1) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_wrong_page"));
            return;
        }
        switch (page) {
            case 1 -> {
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_header",Integer.toString(page)));
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_help"));
                if (sender.hasPermission("vineriummarriages.offer"))
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_offer"));
                if (sender.hasPermission("vineriummarriages.divorce"))
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_divorce"));
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_accept"));
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_decline"));
                if (sender.hasPermission("vineriummarriages.tp"))
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_tp"));
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_confirm"));
                if (sender.hasPermission("vineriummarriages.ender"))
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_ender"));
                if (sender.hasPermission("vineriummarriages.kiss"))
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_kiss"));
                if (sender.hasPermission("vineriummarriages.woohoo"))
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_woohoo"));
                if (sender.hasPermission("asurecore.admin")) {
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_admin_offer"));
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"command_help_command_admin_divorce"));
                }
            }
        }
    }

    private static void offerCommand(CommandSender sender, Player offeredPlayer, Player secondPlayer, boolean silent) {
        if (!(sender instanceof Player) && secondPlayer == null) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_only_by_player"));
            return;
        }
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();
        String offeredPlayerMarryPartner = VineriumMarriages.inst().getMarriedPlayersManager().getMarriedPlayerNames().get(offeredPlayer.getName());

        if (offeredPlayerMarryPartner != null && secondPlayer == null) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"player_already_married"));
            return;
        }
        if (offeredPlayer == sender) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"cant_marry_yourself"));
            return;
        }
        if (secondPlayer == null) {
            double offerDistance = marriedPlayersManager.getOfferMaxDistance();
            long inviteTime = marriedPlayersManager.getAcceptInviteTime();
            Player senderPlayer = ((Player) sender).getPlayer();
            if (offerDistance > 0) {
                if (senderPlayer.getWorld() != offeredPlayer.getWorld() || senderPlayer.getLocation().distance(offeredPlayer.getLocation())
                        > offerDistance) {
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_too_far"));
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"max_offer_distance",Double.toString(offerDistance)));
                    return;
                }
            }
            long inviteTimeInSeconds = inviteTime / 20;
            HashMap<Player,Pair<String,Long>> offerTimers = marriedPlayersManager.getTimers().getOrDefault("MarryOffer",new HashMap<>());
            Pair<String,Long> variable = offerTimers.getOrDefault(senderPlayer,new Pair<>(senderPlayer.getName(),0L));
            if (variable.getSecond() > VinUtils.getCurrentTick())
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"player_already_has_offer"));

            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"offer_sent",offeredPlayer.getName(),Long.toString(inviteTimeInSeconds)));
            senderPlayer.playSound(senderPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1F, 0.5F);

            offerTimers.put(offeredPlayer,new Pair<>(senderPlayer.getName(),VinUtils.getCurrentTick() + inviteTime));
            marriedPlayersManager.getTimers().put("MarryOffer",offerTimers);
            offeredPlayer.playSound(offeredPlayer, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1, 2);
            offeredPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"offer_received",senderPlayer.getName(),Long.toString(inviteTimeInSeconds)));

            Component component = VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"accept_button")
                    .clickEvent(ClickEvent.runCommand("/vinmarry accept"))
                    .hoverEvent(HoverEvent.showText(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"accept_button_hover")))
                    .append(Component.text(" / ").color(NamedTextColor.GRAY))
                    .append(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"decline_button")
                            .clickEvent(ClickEvent.runCommand("/vinmarry decline"))
                            .hoverEvent(HoverEvent.showText(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"decline_button_hover")))
                    );

            offeredPlayer.sendMessage(component);
            offeredPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"offer_hint"));
        }
        else {
            marriedPlayersManager.getMarriedPlayerNames().put(offeredPlayer.getName(),secondPlayer.getName());
            marriedPlayersManager.getMarriedPlayerNames().put(secondPlayer.getName(),offeredPlayer.getName());

            HashMap<Player,Pair<String,Long>> offerTimers = marriedPlayersManager.getTimers().getOrDefault("MarryOffer",new HashMap<>());
            offerTimers.remove(offeredPlayer);
            offerTimers.remove(secondPlayer);
            marriedPlayersManager.getTimers().put("MarryOffer",offerTimers);

            offeredPlayer.playSound(offeredPlayer,Sound.UI_TOAST_CHALLENGE_COMPLETE,SoundCategory.PLAYERS,1f,1f);
            secondPlayer.playSound(secondPlayer,Sound.UI_TOAST_CHALLENGE_COMPLETE,SoundCategory.PLAYERS,1f,1f);
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"marriage_admin",offeredPlayer.getName(),secondPlayer.getName()));
            offeredPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"marriage_accepted",secondPlayer.getName()));
            secondPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"marriage_accepted",offeredPlayer.getName()));

            if (!silent)
                Audience.audience(Bukkit.getOnlinePlayers()).sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"marriage_announcement",offeredPlayer.getName(),secondPlayer.getName()));

            offeredPlayer.updateCommands();
            secondPlayer.updateCommands();
        }
    }

    private static void divorceCommand(CommandSender sender, Player player) {
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();
        if (!(sender instanceof Player) && player == null) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_only_by_player"));
            return;
        }
        if (player == null) {
            Player senderPlayer = (Player) sender;
            if (marriedPlayersManager.getMarriedPlayerNames().get(senderPlayer.getName()) == null) {
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"not_married"));
                return;
            }
            HashMap<Player,Pair<String,Long>> divorceTimers = marriedPlayersManager.getTimers().getOrDefault("MarryDivorce",new HashMap<>());
            Pair<String,Long> variable = new Pair<>(senderPlayer.getName(),VinUtils.getCurrentTick() + 2400);
            divorceTimers.put(senderPlayer,variable);
            marriedPlayersManager.getTimers().put("MarryDivorce",divorceTimers);

            Component component = VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"divorce_button")
                    .clickEvent(ClickEvent.runCommand("/vinmarry confirm"))
                    .hoverEvent(HoverEvent.showText(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"divorce_buttonHover"))
                    );

            senderPlayer.sendMessage(component);
            senderPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"divorce_hint"));
        }
        else {
            if (marriedPlayersManager.getMarriedPlayerNames().get(player.getName()) == null) {
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"player_not_married"));
                return;
            }
            String secondPlayerName = marriedPlayersManager.getMarriedPlayerNames().get(player.getName());
            Player secondPlayer = Bukkit.getPlayer(secondPlayerName);
            if (secondPlayer != null) {
                secondPlayer.playSound(secondPlayer,Sound.BLOCK_GLASS_BREAK,SoundCategory.PLAYERS,1f,1f);
                secondPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"divorce_accepted",player.getName()));
                secondPlayer.updateCommands();
            }

            marriedPlayersManager.getMarriedPlayerNames().remove(secondPlayerName);
            marriedPlayersManager.getMarriedPlayerNames().remove(player.getName());
            player.playSound(player,Sound.BLOCK_GLASS_BREAK,SoundCategory.PLAYERS,1f,1f);
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"divorce_admin",player.getName()));
            player.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"divorce_accepted",secondPlayerName));

            player.updateCommands();
        }
    }

    private static void acceptMarryCommand(CommandSender sender) {

        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_only_by_player"));
            return;
        }
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();

        HashMap<Player,Pair<String,Long>> offerTimers = marriedPlayersManager.getTimers().getOrDefault("MarryOffer",new HashMap<>());
        Pair<String,Long> variable = offerTimers.getOrDefault(senderPlayer,new Pair<>(senderPlayer.getName(),0L));

        if (variable.getSecond() < VinUtils.getCurrentTick()) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"no_active_offer"));
            return;
        }
        String offeredPlayerName = variable.getFirst();
        Player offeredPlayer = Bukkit.getPlayer(offeredPlayerName);
        if (offeredPlayer == null) {
            offerTimers.remove(senderPlayer);
            marriedPlayersManager.getTimers().put("MarryOffer",offerTimers);
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"player_is_offline"));
            return;
        }
        if (marriedPlayersManager.getMarriedPlayerNames().containsKey(offeredPlayerName)) {
            offerTimers.remove(senderPlayer);
            marriedPlayersManager.getTimers().put("MarryOffer",offerTimers);
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"player_already_married"));
            return;
        }

        marriedPlayersManager.getMarriedPlayerNames().put(offeredPlayer.getName(),senderPlayer.getName());
        marriedPlayersManager.getMarriedPlayerNames().put(senderPlayer.getName(),offeredPlayer.getName());

        offerTimers.remove(senderPlayer);
        offerTimers.remove(offeredPlayer);
        marriedPlayersManager.getTimers().put("MarryOffer",offerTimers);

        senderPlayer.playSound(senderPlayer,Sound.UI_TOAST_CHALLENGE_COMPLETE,SoundCategory.PLAYERS,1f,1f);
        offeredPlayer.playSound(senderPlayer,Sound.UI_TOAST_CHALLENGE_COMPLETE,SoundCategory.PLAYERS,1f,1f);
        sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"marriage_accepted",senderPlayer.getName()));
        offeredPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"marriage_accepted",offeredPlayer.getName()));

        offeredPlayer.updateCommands();
        senderPlayer.updateCommands();
    }

    private static void declineMarryCommand(CommandSender sender) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_only_by_player"));
            return;
        }
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();

        HashMap<Player,Pair<String,Long>> offerTimers = marriedPlayersManager.getTimers().getOrDefault("MarryOffer",new HashMap<>());
        Pair<String,Long> variable = offerTimers.getOrDefault(senderPlayer,new Pair<>(senderPlayer.getName(),0L));

        if (variable.getSecond() < VinUtils.getCurrentTick()) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"no_active_offer"));
            return;
        }
        offerTimers.remove(senderPlayer);
        marriedPlayersManager.getTimers().put("MarryOffer",offerTimers);

        sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"offer_declined"));
    }

    private static void confirmCommand(CommandSender sender) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_only_by_player"));
            return;
        }
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();

        HashMap<Player,Pair<String,Long>> offerTimers = marriedPlayersManager.getTimers().getOrDefault("MarryDivorce",new HashMap<>());
        Pair<String,Long> variable = offerTimers.getOrDefault(senderPlayer,new Pair<>(senderPlayer.getName(),0L));

        if (variable.getSecond() < VinUtils.getCurrentTick()) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"no_active_action"));
            return;
        }

        String secondPlayerName = marriedPlayersManager.getMarriedPlayerNames().get(senderPlayer.getName());
        Player secondPlayer = Bukkit.getPlayer(secondPlayerName);
        if (secondPlayer != null) {
            secondPlayer.playSound(secondPlayer,Sound.BLOCK_GLASS_BREAK,SoundCategory.PLAYERS,1f,1f);
            secondPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"divorce_accepted",senderPlayer.getName()));
            secondPlayer.updateCommands();
        }
        senderPlayer.playSound(senderPlayer,Sound.BLOCK_GLASS_BREAK,SoundCategory.PLAYERS,1f,1f);
        senderPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"divorce_accepted",secondPlayerName));
        marriedPlayersManager.getMarriedPlayerNames().remove(secondPlayerName);
        marriedPlayersManager.getMarriedPlayerNames().remove(senderPlayer.getName());
        senderPlayer.updateCommands();
    }

    private static void enderChestCommand(CommandSender sender) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_only_by_player"));
            return;
        }
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();

        String secondPlayerName = marriedPlayersManager.getMarriedPlayerNames().get(senderPlayer.getName());
        String secondPlayerPartner = marriedPlayersManager.getMarriedPlayerNames().get(secondPlayerName);
        if (secondPlayerPartner == null || !secondPlayerPartner.equals(senderPlayer.getName())) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"player_not_married_with_sender"));
            marriedPlayersManager.getMarriedPlayerNames().remove(senderPlayer.getName());
            return;
        }

        senderPlayer.playSound(senderPlayer, Sound.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 1, 1);
        if (VineriumLib.inst().getVaultManager() != null) {
            VineriumLib.inst().getVaultManager().getPermissionProvider().playerAdd(senderPlayer, "cmi.command.ender");
            senderPlayer.performCommand("cmi ender " + marriedPlayersManager.getMarriedPlayerNames().get(senderPlayer.getName()));
            VineriumLib.inst().getVaultManager().getPermissionProvider().playerRemove(senderPlayer, "cmi.command.ender");
        }
    }

    private static void teleportCommand(CommandSender sender) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_only_by_player"));
            return;
        }
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();

        String secondPlayerName = marriedPlayersManager.getMarriedPlayerNames().get(senderPlayer.getName());
        String secondPlayerPartner = marriedPlayersManager.getMarriedPlayerNames().get(secondPlayerName);
        if (secondPlayerPartner == null || !secondPlayerPartner.equals(senderPlayer.getName())) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"player_not_married_with_sender"));
            marriedPlayersManager.getMarriedPlayerNames().remove(senderPlayer.getName());
            return;
        }

        Player secondPlayer = Bukkit.getPlayer(secondPlayerName);
        if (secondPlayer != null) {
            senderPlayer.teleport(secondPlayer);
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"tp_message"));
        }
        else {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"partner_Is_offline"));
        }
    }

    private static void kissCommand(CommandSender sender) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_only_by_player"));
            return;
        }
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();

        String secondPlayerName = marriedPlayersManager.getMarriedPlayerNames().get(senderPlayer.getName());
        String secondPlayerPartner = marriedPlayersManager.getMarriedPlayerNames().get(secondPlayerName);
        if (secondPlayerPartner == null || !secondPlayerPartner.equals(senderPlayer.getName())) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"player_not_married_with_sender"));
            marriedPlayersManager.getMarriedPlayerNames().remove(senderPlayer.getName());
            return;
        }

        Player secondPlayer = Bukkit.getPlayer(secondPlayerName);
        if (secondPlayer == null) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"partner_Is_offline"));
            return;
        }
        double kissDistance = marriedPlayersManager.getKissMaxDistance();
        if (kissDistance > 0) {
            if (senderPlayer.getWorld() != secondPlayer.getWorld() || senderPlayer.getLocation().distance(secondPlayer.getLocation()) > kissDistance) {
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_too_far"));
                return;
            }
        }
        boolean kissHealCheck = true;
        HashMap<Player,Pair<String,Long>> kissTimers = marriedPlayersManager.getTimers().getOrDefault("KissCooldown",new HashMap<>());
        Pair<String,Long> variable = kissTimers.getOrDefault(senderPlayer,new Pair<>(senderPlayer.getName(),0L));
        if (variable.getSecond() >= VinUtils.getCurrentTick())
            kissHealCheck = false;
        if (kissHealCheck) {
            senderPlayer.heal(marriedPlayersManager.getKissHealAmount());
            secondPlayer.heal(marriedPlayersManager.getKissHealAmount());
        }

        senderPlayer.getWorld().spawnParticle(Particle.HEART,senderPlayer.getX(),senderPlayer.getY()+1.7,senderPlayer.getZ(),10,0.2,0.2,0.2);
        secondPlayer.getWorld().spawnParticle(Particle.HEART,secondPlayer.getX(),secondPlayer.getY()+1.7,secondPlayer.getZ(),10,0.2,0.2,0.2);

        senderPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"kiss_partner",secondPlayer.getName()));
        secondPlayer.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"kissed_by_partner",senderPlayer.getName()));
        variable = new Pair<>(null,VinUtils.getCurrentTick()+marriedPlayersManager.getKissCooldown());
        kissTimers.put(senderPlayer,variable);
        kissTimers.put(secondPlayer,variable);
        marriedPlayersManager.getTimers().put("KissCooldown",kissTimers);
    }

    private static void woohooCommand(CommandSender sender) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_only_by_player"));
            return;
        }
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();

        String secondPlayerName = marriedPlayersManager.getMarriedPlayerNames().get(senderPlayer.getName());
        String secondPlayerPartner = marriedPlayersManager.getMarriedPlayerNames().get(secondPlayerName);
        if (secondPlayerPartner == null || !secondPlayerPartner.equals(senderPlayer.getName())) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"player_not_married_with_sender"));
            marriedPlayersManager.getMarriedPlayerNames().remove(senderPlayer.getName());
            return;
        }

        Player secondPlayer = Bukkit.getPlayer(secondPlayerName);
        if (secondPlayer == null) {
            sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"partner_Is_offline"));
            return;
        }
        int woohooDistance = (int) marriedPlayersManager.getWoohooMaxDistance();
        int hungerCost = marriedPlayersManager.getWoohooHungerCost();
        if (woohooDistance > 0) {
            if (senderPlayer.getWorld() != secondPlayer.getWorld() || senderPlayer.getLocation().distance(secondPlayer.getLocation()) > woohooDistance) {
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"action_too_far"));
                return;
            }
            if (marriedPlayersManager.isWoohooBedNeeded()) {
                boolean bedFound = false;
                Block middleBlock = senderPlayer.getLocation().getBlock();
                for (int x = woohooDistance; x >= -woohooDistance; x--) {
                    for (int y = woohooDistance; y >= -woohooDistance; y--) {
                        for (int z = woohooDistance; z >= -woohooDistance; z--) {
                            if (middleBlock.getRelative(x, y, z).getType().name().toUpperCase().endsWith("_BED")) {
                                bedFound = true;
                            }
                        }
                    }
                }
                if (!bedFound) {
                    sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"bed_needed"));
                    return;
                }
            }
        }
        if (marriedPlayersManager.getWoohooCooldown() > 0) {
            HashMap<Player,Pair<String,Long>> woohooTimers = marriedPlayersManager.getTimers().getOrDefault("woohoo_cooldown",new HashMap<>());
            Pair<String,Long> variable = woohooTimers.getOrDefault(senderPlayer,new Pair<>(senderPlayer.getName(),0L));
            if (variable.getSecond() >= VinUtils.getCurrentTick()) {
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"woohoo_cooldown"));
                return;
            }
        }
        if (hungerCost > 0) {
            if (senderPlayer.getFoodLevel() < hungerCost) {
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"woohoo_sender_not_enough_hunger"));
                return;
            }
            if (secondPlayer.getFoodLevel() < hungerCost) {
                sender.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"woohoo_partner_not_enough_hunger"));
                return;
            }
        }
        senderPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (int) marriedPlayersManager.getWoohooLength(),4));
        secondPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (int) marriedPlayersManager.getWoohooLength(),4));
        Audience.audience(Set.of(senderPlayer,secondPlayer)).sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"woohoo_start"));
        BukkitTask repeatableTask = new BukkitRunnable() {
            @Override
            public void run() {
                woohooRepeatableTask(senderPlayer,secondPlayer,marriedPlayersManager);
            }
        }.runTaskTimer(VineriumMarriages.inst(),0,10L);

        BukkitTask cancellingTask = new BukkitRunnable() {
            @Override
            public void run() {
                finishWoohooTask(senderPlayer,secondPlayer);
                repeatableTask.cancel();
            }
        }.runTaskLater(VineriumMarriages.inst(),marriedPlayersManager.getWoohooLength());
    }

    private static void woohooRepeatableTask(Player firstPlayer, Player secondPlayer, MarriedPlayersManager marriedPlayersManager) {
        Audience audience = Audience.audience(firstPlayer.getLocation().getNearbyPlayers(25,25,25));
        Player sourceSelector = ThreadLocalRandom.current().nextBoolean() ? firstPlayer : secondPlayer;
        if (!marriedPlayersManager.getWoohooSounds().isEmpty()) {
            int soundSelector = ThreadLocalRandom.current().nextInt(0,marriedPlayersManager.getWoohooSounds().size());
            double randomPitch = ThreadLocalRandom.current().nextDouble(marriedPlayersManager.getWoohooSoundMinPitch(), marriedPlayersManager.getWoohooSoundMaxPitch());
            @Subst("entity.villager.trade") String soundName = marriedPlayersManager.getWoohooSounds().get(soundSelector);
            net.kyori.adventure.sound.Sound sound = net.kyori.adventure.sound.Sound.sound(
                    Key.key("minecraft:"+soundName),SoundCategory.PLAYERS,1, (float) randomPitch);

            audience.playSound(sound,sourceSelector);
        }

        firstPlayer.getWorld().spawnParticle(Particle.HEART,firstPlayer.getX(),firstPlayer.getY()+1.7,firstPlayer.getZ(),10,0.2,0.2,0.2);
        secondPlayer.getWorld().spawnParticle(Particle.HEART,secondPlayer.getX(),secondPlayer.getY()+1.7,secondPlayer.getZ(),10,0.2,0.2,0.2);
    }

    private static void finishWoohooTask(Player firstPlayer, Player secondPlayer) {
        MarriedPlayersManager marriedPlayersManager = VineriumMarriages.inst().getMarriedPlayersManager();
        int hungerCost = marriedPlayersManager.getWoohooHungerCost();
        Audience playerAudience = Audience.audience(Set.of(firstPlayer,secondPlayer)) ;
        playerAudience.sendMessage(VineriumLib.inst().getLangManager().parseLangString(VineriumMarriages.inst(),"woohoo_finish"));

        firstPlayer.setFoodLevel(firstPlayer.getFoodLevel()-hungerCost);
        secondPlayer.setFoodLevel(secondPlayer.getFoodLevel()-hungerCost);
        for (PotionEffect potionEffect : marriedPlayersManager.getWoohooBuffs()) {
            firstPlayer.addPotionEffect(potionEffect);
            secondPlayer.addPotionEffect(potionEffect);
        }

        int soundSelector = ThreadLocalRandom.current().nextInt(0,marriedPlayersManager.getWoohooSounds().size());
        Audience audience = Audience.audience(firstPlayer.getLocation().getNearbyPlayers(25,25,25));
        @Subst("entity.villager.trade") String soundName = marriedPlayersManager.getWoohooSounds().get(soundSelector);
        net.kyori.adventure.sound.Sound sound = net.kyori.adventure.sound.Sound.sound(
                Key.key("minecraft:"+soundName),SoundCategory.PLAYERS,1, 0.5f);
        audience.playSound(sound,firstPlayer);
        audience.playSound(sound,secondPlayer);

        HashMap<Player,Pair<String,Long>> woohooTimers = marriedPlayersManager.getTimers().getOrDefault("woohoo_cooldown",new HashMap<>());
        Pair<String,Long> variable = new Pair<>(null,VinUtils.getCurrentTick()+marriedPlayersManager.getWoohooCooldown());
        woohooTimers.put(firstPlayer,variable);
        woohooTimers.put(secondPlayer,variable);
        marriedPlayersManager.getTimers().put("woohoo_cooldown",woohooTimers);
    }
}
