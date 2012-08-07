package de.minestar.syncchest.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

import de.minestar.syncchest.core.Core;
import de.minestar.syncchest.units.DataNode;
import de.minestar.syncchest.units.SyncChest;
import de.minestar.syncchest.utils.ChestUpdater;

public class InventoryListener implements Listener {

    private DataNode data;

    public InventoryListener(DataNode data) {
        this.data = data;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // not a chestinventory => return
        if (event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }

        this.data.setActiveSyncChest(event.getPlayer().getName(), null);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // event cancelled => return
        if (event.isCancelled()) {
            return;
        }

        // not a chestinventory => return
        if (event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }

        // not a SyncChest => return
        SyncChest syncChest = this.data.getActiveSyncChest(event.getWhoClicked().getName());
        if (syncChest == null) {
            return;
        }

        // CANCEL, IF WAITING FOR UPDATE
        if (syncChest.getParent().isLocked()) {
            event.setCancelled(true);
            return;
        }

        // UPDATE OTHER CHESTS
        Bukkit.getScheduler().scheduleSyncDelayedTask(Core.getInstance(), new ChestUpdater(syncChest, event.getInventory()), 1);
    }
}
