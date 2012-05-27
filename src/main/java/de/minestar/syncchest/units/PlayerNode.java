package de.minestar.syncchest.units;

import java.util.HashMap;

import de.minestar.syncchest.utils.BlockVector;

public class PlayerNode {
    private final String playerName;
    private final HashMap<String, SyncNode> nodes;

    public PlayerNode(String playerName) {
        this.playerName = playerName;
        this.nodes = new HashMap<String, SyncNode>();
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public boolean isEmpty() {
        return this.nodes.size() == 0;
    }

    public boolean hasNode(String nodeName) {
        return this.nodes.containsKey(nodeName);
    }

    public SyncNode getNode(String nodeName) {
        return this.nodes.get(nodeName);
    }

    public SyncNode addNode(String nodeName, boolean doubleChest) {
        if (this.hasNode(nodeName)) {
            return null;
        }

        SyncNode newNode = new SyncNode(nodeName, this, doubleChest);
        this.nodes.put(nodeName, newNode);
        return newNode;
    }

    public SyncNode removeNode(String nodeName) {
        return this.nodes.remove(nodeName);
    }

    public boolean hasChest(BlockVector position) {
        for (SyncNode node : this.nodes.values()) {
            if (node.hasChest(position))
                return true;
        }
        return false;
    }

    public SyncChest addChest(String nodeName, BlockVector position, boolean doubleChest) {
        if (!this.hasNode(nodeName)) {
            return null;
        }
        return this.getNode(nodeName).addChest(position, doubleChest);
    }

    public SyncChest removeChest(String nodeName, BlockVector position) {
        if (!this.hasNode(nodeName)) {
            return null;
        }
        SyncNode node = this.getNode(nodeName);
        SyncChest removed = node.removeChest(position);
        if (node.isEmpty()) {
            this.removeNode(nodeName);
        }
        return removed;
    }

    public SyncChest getChest(String nodeName, BlockVector position) {
        if (!this.hasNode(nodeName)) {
            return null;
        }
        return this.getNode(nodeName).getChest(position);
    }

    public SyncChest getChest(BlockVector position) {
        for (SyncNode node : this.nodes.values()) {
            if (node.hasChest(position)) {
                return node.getChest(position);
            }
        }
        return null;
    }

    public boolean doStartUp() {
        boolean success = true;
        for (SyncNode node : this.nodes.values()) {
            if (!node.doStartUp()) {
                success = false;
            }
        }
        return success;
    }

    public boolean doShutDown() {
        boolean success = true;
        for (SyncNode node : this.nodes.values()) {
            if (!node.doShutDown()) {
                success = false;
            }
        }
        return success;
    }

    public String getPlayerName() {
        return playerName;
    }

    public SyncNode getOrCreateSyncNode(String nodeName, boolean doubleChest) {
        SyncNode syncNode = this.nodes.get(nodeName);
        if (syncNode != null)
            return syncNode;

        syncNode = new SyncNode(nodeName, this, doubleChest);
        this.nodes.put(nodeName, syncNode);
        return syncNode;
    }
}
