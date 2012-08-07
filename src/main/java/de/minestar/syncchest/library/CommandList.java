/*
 * Copyright (C) 2011 MineStar.de 
 * 
 * This file is part of MineStarLibrary.
 * 
 * MineStarLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * MineStarLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MineStarLibrary.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.syncchest.library;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandList {

    // The commands are stored in this list. The key indicates the
    // commandssyntax and the argument counter
    private HashMap<String, AbstractCommand> commandList;
    private String pluginName = "";

    /**
     * Creates an array where the commands are stored in and add them all to the HashMap
     * 
     * @param commands
     *            A list of all commands the plugin is using
     */
    public CommandList(AbstractCommand... commands) {
        initCommandList(commands);
    }

    /**
     * Creates an array where the commands are stored in and add them all to the HashMap
     * 
     * @param pluginName
     *            The pluginName for the use of prefixes
     * @param commands
     *            A list of all commands the plugin is using
     */
    public CommandList(String pluginName, AbstractCommand... commands) {
        this(commands);
        this.pluginName = pluginName;

    }

    /**
     * Handle a bukkit command by searching all registered commands. <br>
     * Call this method in {@link JavaPlugin#onCommand(CommandSender, Command, String, String[])}
     * 
     * @param sender
     *            The command caller
     * @param label
     *            The label of the command, starts normally without an /
     * @param args
     *            The arguments of the command, can also be label of a subcommand
     */
    public boolean handleCommand(CommandSender sender, String label, String[] args) {
        if (!label.startsWith("/"))
            label = "/" + label;

        // looking for non extended and non super command
        AbstractCommand cmd = commandList.get(label + "_" + args.length);
        if (cmd != null) {
            cmd.run(args, sender);
            return true;
        } else {
            // look for extended commands and super commands
            cmd = commandList.get(label);
            if (cmd != null) {
                cmd.run(args, sender);
                return true;
            }

            // COMMAND NOT FOUND
            else {
                ChatUtils.writeInfo(sender, pluginName, "Command '" + label + "' not found.");

                // print possible command syntax
                for (Entry<String, AbstractCommand> entry : commandList.entrySet()) {
                    if (entry.getKey().startsWith(label))
                        ChatUtils.writeInfo(sender, pluginName, entry.getValue().getSyntax() + " " + entry.getValue().getArguments());
                }
                return false;
            }
        }
    }

    /**
     * Stores the commands from the array to a HashMap. The key is generated by the followning: <br>
     * <code>syntax_numberOfArguments</code> <br>
     * Example: /warp create_1 (because create has one argument)
     * 
     * @param cmds
     *            The array list for commands
     */
    private void initCommandList(AbstractCommand[] cmds) {

        commandList = new HashMap<String, AbstractCommand>();
        for (AbstractCommand cmd : cmds) {
            String key = "";
            // when the command has a variable count of arguments or
            // when the command has sub commands
            if (cmd instanceof AbstractExtendedCommand || cmd instanceof AbstractSuperCommand)
                key = cmd.getSyntax();
            // a normal command(no subcommands/fix argument count)
            else
                key = cmd.getSyntax() + "_" + cmd.getArgumentCount();

            commandList.put(key, cmd);
        }
    }
}
