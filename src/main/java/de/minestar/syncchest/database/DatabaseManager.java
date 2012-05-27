package de.minestar.syncchest.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.bukkit.Location;
import org.bukkit.Material;

import de.minestar.syncchest.core.Core;
import de.minestar.syncchest.library.AbstractDatabaseHandler;
import de.minestar.syncchest.library.ConsoleUtils;
import de.minestar.syncchest.library.DatabaseConnection;
import de.minestar.syncchest.library.DatabaseUtils;
import de.minestar.syncchest.units.DataNode;
import de.minestar.syncchest.units.SyncChest;
import de.minestar.syncchest.utils.BlockVector;

public class DatabaseManager extends AbstractDatabaseHandler {

    private PreparedStatement addSyncChest, removeSyncChest, loadSyncChests;

    public DatabaseManager(String pluginName, File dataFolder) {
        super(pluginName, dataFolder);
    }

    @Override
    protected DatabaseConnection createConnection(String pluginName, File dataFolder) throws Exception {
        return new DatabaseConnection(pluginName, "plugins/SyncChests/", "chests");
    }

    @Override
    protected void createStructure(String pluginName, Connection con) throws Exception {
        DatabaseUtils.createStructure(getClass().getResourceAsStream("/structure.sql"), con, pluginName);
    }

    @Override
    protected void createStatements(String pluginName, Connection con) throws Exception {
        this.addSyncChest = con.prepareStatement("INSERT INTO TBL_SyncChests (owner, nodeName, doubleChest, xPos, yPos, zPos, worldName) VALUES (?, ?, ?, ?, ?, ?, ?)");
        this.loadSyncChests = con.prepareStatement("SELECT * FROM TBL_SyncChests ORDER BY id ASC");
        this.removeSyncChest = con.prepareStatement("DELETE FROM TBL_SyncChests WHERE xPos = ? AND yPos = ? AND zPos = ? AND worldName = ?");
    }

    /**
     * Add a SyncChest to the Database
     * 
     * @param syncChest
     * @return <b>true</b> if the SyncChest was added, otherwise <b>false</b>
     */
    public boolean saveSyncChest(SyncChest syncChest) {
        try {
            this.addSyncChest.setString(1, syncChest.getParent().getParent().getPlayerName());
            this.addSyncChest.setString(2, syncChest.getParent().getNodeName());
            this.addSyncChest.setBoolean(3, syncChest.getParent().isDoubleChest());
            this.addSyncChest.setInt(4, syncChest.getPosition().getX());
            this.addSyncChest.setInt(5, syncChest.getPosition().getY());
            this.addSyncChest.setInt(6, syncChest.getPosition().getZ());
            this.addSyncChest.setString(7, syncChest.getPosition().getWorldName());
            return (this.addSyncChest.executeUpdate() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            ConsoleUtils.printException(e, Core.NAME, "Can't save SyncChest in database!");
            return false;
        }
    }

    /**
     * Delete a SyncChest from the Database
     * 
     * @param syncChest
     * @return <b>true</b> if the SyncChest was deleted, otherwise <b>false</b>
     */
    public boolean deleteSyncChest(SyncChest syncChest) {
        try {
            this.removeSyncChest.setInt(1, syncChest.getPosition().getX());
            this.removeSyncChest.setInt(2, syncChest.getPosition().getY());
            this.removeSyncChest.setInt(3, syncChest.getPosition().getZ());
            this.removeSyncChest.setString(4, syncChest.getPosition().getWorldName());
            return (this.removeSyncChest.executeUpdate() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            ConsoleUtils.printException(e, Core.NAME, "Can't delete SyncChest from database!");
            return false;
        }
    }

    /**
     * Load the DataNode from the Database
     * 
     * @return the DataNode
     */
    public DataNode loadSyncChests() {
        try {
            DataNode data = new DataNode();
            // get SyncChests from Database
            ResultSet results = this.loadSyncChests.executeQuery();
            int failed = 0, count = 0;
            while (results.next()) {
                BlockVector position = new BlockVector(results.getString("worldName"), results.getInt("xPos"), results.getInt("yPos"), results.getInt("zPos"));
                Location location = position.getLocation();
                if (location == null || location.getBlock().getTypeId() != Material.CHEST.getId()) {
                    failed++;
                    ConsoleUtils.printError(Core.NAME, "Could not load SyncChest @ " + position.toString());
                    continue;
                }

                // check: create the SyncNode,if it not exists
                String playerName = results.getString("owner");
                String nodeName = results.getString("nodeName");
                boolean doubleChest = results.getBoolean("doubleChest");
                if (!data.hasSyncNode(playerName, nodeName)) {
                    data.addSyncNode(playerName, nodeName, doubleChest);
                }

                data.addChest(playerName, nodeName, position, doubleChest);
                count++;
            }

            ConsoleUtils.printInfo(Core.NAME, "Loaded " + count + " SyncChests in " + data.getNodeCount() + " different SyncNodes for " + data.getPlayerCount() + " players!");
            if (failed > 0) {
                ConsoleUtils.printError(Core.NAME, "Could not load " + failed + " SyncChests!");
            }
            return data;
        } catch (Exception e) {
            ConsoleUtils.printException(e, Core.NAME, "Can't load SyncChests from Database!");
            return null;
        }
    }
}
