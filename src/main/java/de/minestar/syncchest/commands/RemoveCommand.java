package de.minestar.syncchest.commands;

import org.bukkit.entity.Player;

import de.minestar.syncchest.core.Core;
import de.minestar.syncchest.library.AbstractCommand;
import de.minestar.syncchest.library.PlayerUtils;
import de.minestar.syncchest.listener.ActionListener;
import de.minestar.syncchest.utils.PlayerState;

public class RemoveCommand extends AbstractCommand {

    private ActionListener actionListener;

    public RemoveCommand(String syntax, String arguments, String node, ActionListener actionListener) {
        super(Core.NAME, syntax, arguments, node);
        this.description = "Unsync a chest.";
        this.actionListener = actionListener;
    }

    public void execute(String[] args, Player player) {
        this.actionListener.setPlayerState(player, PlayerState.CHEST_REMOVE);
        PlayerUtils.sendSuccess(player, Core.NAME, "You are now in REMOVE-MODE!");
        PlayerUtils.sendInfo(player, Core.NAME, "Click on a chest to unsync it.");
    }
}