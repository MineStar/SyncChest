package de.minestar.syncchest.commands;

import org.bukkit.entity.Player;

import de.minestar.syncchest.core.Core;
import de.minestar.syncchest.library.AbstractCommand;
import de.minestar.syncchest.library.PlayerUtils;
import de.minestar.syncchest.listener.ActionListener;
import de.minestar.syncchest.utils.PlayerState;

public class InfoCommand extends AbstractCommand {

    private ActionListener actionListener;

    public InfoCommand(String syntax, String arguments, String node, ActionListener actionListener) {
        super(Core.NAME, syntax, arguments, node);
        this.description = "Get some information about a chest.";
        this.actionListener = actionListener;
    }

    public void execute(String[] args, Player player) {
        this.actionListener.setPlayerState(player, PlayerState.CHEST_INFO);
        PlayerUtils.sendSuccess(player, Core.NAME, "You are now in INFO-MODE!");
        PlayerUtils.sendInfo(player, Core.NAME, "Click on a chest to get informations about it.");
    }
}