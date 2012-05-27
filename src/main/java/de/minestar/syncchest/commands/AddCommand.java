package de.minestar.syncchest.commands;

import org.bukkit.entity.Player;

import de.minestar.syncchest.core.Core;
import de.minestar.syncchest.library.AbstractCommand;
import de.minestar.syncchest.library.PlayerUtils;
import de.minestar.syncchest.listener.ActionListener;
import de.minestar.syncchest.utils.PlayerState;

public class AddCommand extends AbstractCommand {

    private ActionListener actionListener;

    public AddCommand(String syntax, String arguments, String node, ActionListener actionListener) {
        super(Core.NAME, syntax, arguments, node);
        this.description = "Sync a chest.";
        this.actionListener = actionListener;
    }

    public void execute(String[] args, Player player) {
        this.actionListener.setNodeName(player, args[0]);
        this.actionListener.setPlayerState(player, PlayerState.CHEST_ADD);
        PlayerUtils.sendSuccess(player, Core.NAME, "You are now in ADD-MODE!");
        PlayerUtils.sendInfo(player, Core.NAME, "Click on a chest to sync it.");
    }
}