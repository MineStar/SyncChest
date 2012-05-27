package de.minestar.syncchest.core;

import org.bukkit.plugin.PluginManager;

import de.minestar.syncchest.commands.AddCommand;
import de.minestar.syncchest.commands.InfoCommand;
import de.minestar.syncchest.commands.RemoveCommand;
import de.minestar.syncchest.commands.SyncChestCommand;
import de.minestar.syncchest.database.DatabaseManager;
import de.minestar.syncchest.library.AbstractCore;
import de.minestar.syncchest.library.CommandList;
import de.minestar.syncchest.library.ConsoleUtils;
import de.minestar.syncchest.listener.ActionListener;
import de.minestar.syncchest.listener.InventoryListener;
import de.minestar.syncchest.units.DataNode;
import de.minestar.syncchest.utils.Permissions;

public class Core extends AbstractCore {
    public static final String NAME = "SyncChests";
    private static Core INSTANCE = null;

    /**
     * Manager
     */
    private DataNode dataNode;
    private DatabaseManager databaseManager;

    /**
     * Listener
     */
    private ActionListener actionListener;
    private InventoryListener inventoryListener;

    public Core() {
        this(NAME);
    }

    public Core(String name) {
        super(NAME);
        INSTANCE = this;
    }

    @Override
    protected boolean createManager() {
        this.databaseManager = new DatabaseManager("SyncChests", this.getDataFolder());
        this.dataNode = this.databaseManager.loadSyncChests();
        return true;
    }

    @Override
    protected boolean createListener() {
        this.actionListener = new ActionListener(this.dataNode, this.databaseManager);
        this.inventoryListener = new InventoryListener(this.dataNode);
        return true;
    }

    @Override
    protected boolean createCommands() {
        //@formatter:off;
        this.cmdList = new CommandList(
                new SyncChestCommand    ("/sync", "", "",
                            new AddCommand      ("add",    "<NodeName>",    Permissions.CHEST_ADD,      this.actionListener),
                            new RemoveCommand   ("remove",    "",           Permissions.CHEST_REMOVE,   this.actionListener),
                            new InfoCommand     ("info",   "",              Permissions.CHEST_INFO,     this.actionListener)
                          )
         );
        // @formatter: on;
        return true;
    }

    @Override
    protected boolean commonDisable() {
        ConsoleUtils.printInfo(Core.NAME, "Shutting down...");
        if(!this.dataNode.doShutDown()) {
            ConsoleUtils.printError(Core.NAME, "Something went wrong!");  
        }
        // close SQLite-Connection
        if(this.databaseManager.hasConnection()) {
            this.databaseManager.closeConnection();
        }
        return true;
    }

    @Override
    protected boolean commonEnable() {
        this.dataNode.doStartUp();
        return this.dataNode != null;
    }

    @Override
    protected boolean registerEvents(PluginManager pm) {
        pm.registerEvents(this.actionListener, this);
        pm.registerEvents(this.inventoryListener, this);
        return true;
    }

    public static Core getInstance() {
       return INSTANCE;
    }
}
