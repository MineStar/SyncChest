package de.minestar.syncchest.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import de.minestar.syncchest.units.SyncChest;

public class ChestUpdater implements Runnable {
    private SyncChest syncChest;
    private Inventory inventory;

    public ChestUpdater(SyncChest syncChest, Inventory inventory) {
        this.syncChest = syncChest;
        this.inventory = inventory;
        this.syncChest.getParent().setLocked(true);
    }

    @Override
    public void run() {
        Block block = syncChest.getPosition().getLocation().getBlock();
        if (block.getTypeId() != Material.CHEST.getId())
            return;

        Chest chest = (Chest) block.getState();
        if (chest == null)
            return;

        this.syncChest.getParent().synchronizeInventories(this.syncChest.getPosition(), this.inventory);
        this.syncChest.getParent().setLocked(false);
    }
}
