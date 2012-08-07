package de.minestar.syncchest.units;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.minestar.syncchest.utils.BlockVector;

public class SyncNode {
    private final String nodeName;
    private final ArrayList<SyncChest> chests;
    private final PlayerNode parent;
    private final boolean doubleChest;
    private boolean locked = false;

    public SyncNode(String nodeName, PlayerNode parent, boolean doubleChest) {
        this.nodeName = nodeName;
        this.parent = parent;
        this.doubleChest = doubleChest;
        this.chests = new ArrayList<SyncChest>();
    }

    public boolean isEmpty() {
        return this.chests.size() == 0;
    }

    public String getNodeName() {
        return nodeName;
    }

    public boolean hasChest(BlockVector position) {
        return this.getChest(position) != null;
    }

    public SyncChest getChest(BlockVector position) {
        for (SyncChest syncChest : this.chests) {
            if (syncChest.getPosition().equals(position))
                return syncChest;
        }
        return null;
    }

    public SyncChest addChest(BlockVector position, boolean doubleChest) {
        if (this.hasChest(position)) {
            return null;
        }
        SyncChest newChest = new SyncChest(position, this);
        this.chests.add(newChest);
        return newChest;
    }

    public SyncChest removeChest(BlockVector position) {
        // clear the inventory, when removing a chest
        if (this.chests.size() > 1) {
            // init vars
            Location location;
            Block block;
            Chest chest;

            // we need a valid location
            location = position.getLocation();
            if (location == null) {
                return null;
            }

            // blocks must be a chest
            block = location.getBlock();
            if (block.getTypeId() != Material.CHEST.getId()) {
                return null;
            }

            // synchronize inventories
            chest = (Chest) block.getState();
            chest.getInventory().clear();
        }
        // finally remove the chest
        SyncChest syncChest;
        for (int i = 0; i < this.chests.size(); i++) {
            syncChest = this.chests.get(i);
            if (syncChest.getPosition().equals(position)) {
                this.chests.remove(i);
                return syncChest;
            }
        }
        return null;
    }

    public PlayerNode getParent() {
        return parent;
    }

    /**
     * @return <b>true</b> if this node contains doublechests, otherwise <b>false</b>
     */
    public boolean isDoubleChest() {
        return doubleChest;
    }

    /**
     * @return <b>true</b> if this node is currently locked, otherwise <b>false</b>
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Lock or Unlock the node
     * 
     * @param locked
     *            the locked to set
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Synchronize the inventories
     * 
     * @param position
     * @param inventory
     * @return <b>true</b> if synchronizing went fine, otherwise <b>false</b>
     */
    public boolean synchronizeInventories(BlockVector position, Inventory inventory) {
        // init vars
        Location location;
        Block block;
        Chest chest;
        ItemStack itemStack;
        boolean success = true;
        // iterate...
        for (SyncChest syncChest : this.chests) {
            // only synchronize the other chests
            if (position.equals(syncChest.getPosition())) {
                continue;
            }

            // we need a valid location
            location = syncChest.getPosition().getLocation();
            if (location == null) {
                success = false;
                continue;
            }

            // blocks must be a chest
            block = location.getBlock();
            if (block.getTypeId() != Material.CHEST.getId()) {
                success = false;
                continue;
            }

            // inventorysizes must be equal
            chest = (Chest) block.getState();
            if (chest.getInventory().getSize() != inventory.getSize()) {
                success = false;
                continue;
            }

            // clear inventory
            chest.getInventory().clear();

            // copy itemstacks one by one
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                itemStack = inventory.getItem(slot);
                if (itemStack == null || itemStack.getTypeId() == Material.AIR.getId()) {
                    chest.getInventory().clear(slot);
                } else {
                    chest.getInventory().setItem(slot, itemStack);
                }
            }
        }
        return success;
    }

    /**
     * Clear all inventories on shutdown, except the first one
     * 
     * @param position
     * @param inventory
     * @return <b>true</b> if everything went fine, otherwise <b>false</b>
     */
    public boolean doShutDown() {
        // init vars
        Location location;
        Block block;
        Chest chest;
        boolean success = true;
        // lock this node
        this.setLocked(true);
        // iterate...
        SyncChest syncChest;
        for (int i = 1; i < this.chests.size(); i++) {
            syncChest = this.chests.get(i);

            // we need a valid location
            location = syncChest.getPosition().getLocation();
            if (location == null) {
                success = false;
                continue;
            }

            // blocks must be a chest
            block = location.getBlock();
            if (block.getTypeId() != Material.CHEST.getId()) {
                success = false;
                continue;
            }

            // clear inventory
            chest = (Chest) block.getState();
            chest.getInventory().clear();
        }
        // unlock this node
        this.setLocked(false);
        return success;
    }

    /**
     * Synchronize all Inventories on startup. (Based on the first chest of the ArrayList)
     * 
     * @return <b>true</b> if synchronizing went fine, otherwise <b>false</b>
     */
    public boolean doStartUp() {
        if (this.chests.size() <= 1)
            return true;

        Location location = this.chests.get(0).getPosition().getLocation();
        if (location == null)
            return false;
        Block block = location.getBlock();
        if (block.getTypeId() != Material.CHEST.getId())
            return false;
        Chest chest = (Chest) block.getState();
        return this.synchronizeInventories(this.chests.get(0).getPosition(), chest.getInventory());
    }
}
