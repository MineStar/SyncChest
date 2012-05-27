package de.minestar.syncchest.units;

import java.util.HashMap;

import de.minestar.syncchest.utils.BlockVector;

public class DataNode {
    private final HashMap<String, PlayerNode> players;
    private final HashMap<String, SyncChest> activeSyncChests;

    public DataNode() {
        this.players = new HashMap<String, PlayerNode>();
        this.activeSyncChests = new HashMap<String, SyncChest>();
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getNodeCount() {
        int count = 0;
        for (PlayerNode playerNode : this.players.values()) {
            count += playerNode.getNodeCount();
        }
        return count;
    }

    public void setActiveSyncChest(String playerName, SyncChest syncChest) {
        if (syncChest != null) {
            this.activeSyncChests.put(playerName, syncChest);
        } else {
            this.activeSyncChests.remove(playerName);
        }
    }

    public SyncChest getActiveSyncChest(String playerName) {
        return this.activeSyncChests.get(playerName);
    }

    private PlayerNode getOrCreatePlayerNode(String playerName) {
        PlayerNode playerNode = this.players.get(playerName);
        if (playerNode != null) {
            return playerNode;
        }
        playerNode = new PlayerNode(playerName);
        this.players.put(playerName, playerNode);
        return playerNode;
    }

    /**
     * Remove a PlayerNode
     * 
     * @param playerName
     * @return the removed PlayerNode
     */
    private PlayerNode removePlayerNode(String playerName) {
        return this.players.remove(playerName);
    }

    /**
     * Add a SyncChest at the given position
     * 
     * @param playerName
     * @param nodeName
     * @param position
     * @return the new SyncChest
     */
    public SyncChest addChest(String playerName, String nodeName, BlockVector position, boolean doubleChest) {
        if (this.hasChest(position))
            return null;

        PlayerNode playerNode = this.getOrCreatePlayerNode(playerName);
        SyncNode syncNode = playerNode.getOrCreateSyncNode(nodeName, doubleChest);
        return syncNode.addChest(position, doubleChest);
    }

    /**
     * Remove a SyncChest at the given position
     * 
     * @param playerName
     * @param nodeName
     * @param position
     * @return
     */
    public boolean removeChest(BlockVector position) {
        SyncChest chest = this.getChest(position);
        if (chest == null) {
            return false;
        }

        SyncNode syncNode = chest.getParent();
        PlayerNode playerNode = syncNode.getParent();
        SyncChest removedChest = playerNode.removeChest(syncNode.getNodeName(), position);
        if (playerNode.isEmpty()) {
            this.removePlayerNode(playerNode.getPlayerName());
        }
        return removedChest != null;
    }

    /**
     * Get the SyncChest at the given position
     * 
     * @param position
     * @return the SyncChest
     */
    public SyncChest getChest(BlockVector position) {
        SyncChest chest;
        for (PlayerNode playerNode : this.players.values()) {
            chest = playerNode.getChest(position);
            if (chest != null) {
                return chest;
            }
        }
        return null;
    }

    /**
     * Check if there is any SyncChest at the given position
     * 
     * @param position
     * @return <b>true</b> if so, otherwise <b>false</b>
     */
    public boolean hasChest(BlockVector position) {
        return this.getChest(position) != null;
    }

    public boolean hasSyncNode(String playerName, String nodeName) {
        PlayerNode playerNode = this.players.get(playerName);
        if (playerNode == null) {
            return false;
        } else {
            return playerNode.getNode(nodeName) != null;
        }
    }

    public void addSyncNode(String playerName, String nodeName, boolean doubleChest) {
        PlayerNode playerNode = this.getOrCreatePlayerNode(playerName);
        playerNode.addNode(nodeName, doubleChest);
    }

    public SyncNode getSyncNode(String playerName, String nodeName) {
        PlayerNode playerNode = this.getOrCreatePlayerNode(playerName);
        return playerNode.getNode(nodeName);
    }

    public boolean doStartUp() {
        // TODO: load the Database

        boolean success = true;
        for (PlayerNode playerNode : this.players.values()) {
            if (!playerNode.doStartUp()) {
                success = false;
            }
        }
        return success;
    }

    public boolean doShutDown() {
        boolean success = true;
        for (PlayerNode playerNode : this.players.values()) {
            if (!playerNode.doShutDown()) {
                success = false;
            }
        }
        return success;
    }
}
