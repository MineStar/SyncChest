package de.minestar.syncchest.listener;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import de.minestar.syncchest.core.Core;
import de.minestar.syncchest.database.DatabaseManager;
import de.minestar.syncchest.library.PlayerUtils;
import de.minestar.syncchest.units.DataNode;
import de.minestar.syncchest.units.SyncChest;
import de.minestar.syncchest.utils.BlockVector;
import de.minestar.syncchest.utils.ChestUtils;
import de.minestar.syncchest.utils.Permissions;
import de.minestar.syncchest.utils.PlayerState;

public class ActionListener implements Listener {

    private final HashMap<String, PlayerState> playerStates;
    private final HashMap<String, String> nodeNames;
    private final DataNode data;
    private final DatabaseManager databaseManager;

    public ActionListener(DataNode data, DatabaseManager databaseManager) {
        this.data = data;
        this.databaseManager = databaseManager;
        this.playerStates = new HashMap<String, PlayerState>();
        this.nodeNames = new HashMap<String, String>();
    }

    public void setNodeName(Player player, String nodeName) {
        this.setNodeName(player.getName(), nodeName);
    }

    public void setNodeName(String playerName, String nodeName) {
        this.nodeNames.put(playerName, nodeName);
    }

    public PlayerState setPlayerState(Player player, PlayerState state) {
        return this.setPlayerState(player.getName(), state);
    }

    public PlayerState setPlayerState(String playerName, PlayerState state) {
        if (state != PlayerState.NONE) {
            this.playerStates.put(playerName, state);
        } else {
            this.playerStates.remove(playerName);
        }
        return state;
    }

    public PlayerState getPlayerState(Player player) {
        return this.getPlayerState(player.getName());
    }

    public PlayerState getPlayerState(String playerName) {
        PlayerState state = this.playerStates.get(playerName);
        if (state == null) {
            return PlayerState.NONE;
        }
        return state;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // event cancelled => return
        if (event.isCancelled()) {
            return;
        }

        // destroyed NOT on a chest => return
        if (event.getBlock().getTypeId() != Material.CHEST.getId()) {
            return;
        }

        // handle blockbreak of chests
        if (ChestUtils.getSyncChest(this.data, new BlockVector(event.getBlock().getLocation())) != null) {
            PlayerUtils.sendError(event.getPlayer(), Core.NAME, "You cannot break this chest.");
            PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, "This chest is a SyncChest. Desync it first.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // event cancelled => return
        if (event.isCancelled())
            return;

        // not a shop-sign => return;
        for (Block block : event.blockList()) {
            // only chests are affected
            if (block.getTypeId() != Material.CHEST.getId()) {
                continue;
            }
            // check for a SyncChest
            if (ChestUtils.getSyncChest(this.data, new BlockVector(block.getLocation())) != null) {
                event.setYield(0.0f);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        // event cancelled => return
        if (event.isCancelled())
            return;

        // only chests are affected
        if (event.getBlock().getTypeId() != Material.CHEST.getId()) {
            return;
        }

        // check for a SyncChest
        if (ChestUtils.getSyncChest(this.data, new BlockVector(event.getBlock().getLocation())) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // event cancelled => return
        if (event.isCancelled()) {
            return;
        }

        // destroyed NOT on a chest => return
        if (event.getBlock().getTypeId() != Material.CHEST.getId()) {
            return;
        }

        // handle blockplace of chests
        if (ChestUtils.getSyncChest(this.data, new BlockVector(event.getBlock().getLocation())) != null) {
            PlayerUtils.sendError(event.getPlayer(), Core.NAME, "You cannot place a chest here.");
            PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, "The adjacent chest is a SyncChest. Desync it first.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // event cancelled => return
        if (event.isCancelled()) {
            return;
        }

        // clicked air => return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // get PlayerState
        final PlayerState playerState = this.getPlayerState(event.getPlayer());

        // clicked NOT on a chest => return
        if (event.getClickedBlock().getTypeId() != Material.CHEST.getId()) {
            if (playerState != PlayerState.NONE) {
                this.setPlayerState(event.getPlayer(), PlayerState.NONE);
                // cancel event
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
                // print error
                PlayerUtils.sendError(event.getPlayer(), Core.NAME, "You need to click on a chest!");
            }
            return;
        }

        // normal state => return
        if (playerState == PlayerState.NONE) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                SyncChest syncChest = ChestUtils.getSyncChest(this.data, new BlockVector(event.getClickedBlock().getLocation()));
                if (syncChest != null) {
                    // check permissions
                    if (Permissions.hasPermission(event.getPlayer(), Permissions.CHEST_USE)) {
                        this.data.setActiveSyncChest(event.getPlayer().getName(), syncChest);
                    } else {
                        // cancel event
                        event.setUseInteractedBlock(Event.Result.DENY);
                        event.setUseItemInHand(Event.Result.DENY);
                        event.setCancelled(true);
                        // print error
                        PlayerUtils.sendError(event.getPlayer(), Core.NAME, "You are not allowed to use SyncChests.");
                    }
                } else {
                    this.data.setActiveSyncChest(event.getPlayer().getName(), null);
                }
            }
            return;
        }

        // cancel event
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);

        // handle states
        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        BlockVector position = new BlockVector(location);
        Chest doubleChest = ChestUtils.getDoubleChest(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        boolean isDoubleChest = doubleChest != null;
        switch (playerState) {
            case CHEST_ADD : {
                this.setPlayerState(player, PlayerState.NONE);

                // check permissions
                if (!Permissions.hasPermission(player, Permissions.CHEST_ADD)) {
                    PlayerUtils.sendError(player, Core.NAME, "You are not allowed to create SyncChests.");
                    return;
                }

                // check first chest
                SyncChest syncChest = this.data.getChest(position);
                if (syncChest != null) {
                    PlayerUtils.sendError(player, Core.NAME, "This chest is already synced by '" + syncChest.getParent().getParent().getPlayerName() + "'.");
                    return;
                }
                // check DoubleChest
                if (isDoubleChest) {
                    // check DoubleChest
                    position = new BlockVector(doubleChest.getLocation());
                    syncChest = this.data.getChest(position);
                    if (syncChest != null) {
                        PlayerUtils.sendError(player, Core.NAME, "This chest is already synced by '" + syncChest.getParent().getParent().getPlayerName() + "'.");
                        return;
                    }
                }

                // try to add the SyncChest
                this.tryToAddSyncChest(player, location, isDoubleChest);
                return;
            }
            case CHEST_REMOVE : {
                this.setPlayerState(player, PlayerState.NONE);

                // check permissions
                if (!Permissions.hasPermission(player, Permissions.CHEST_REMOVE)) {
                    PlayerUtils.sendError(player, Core.NAME, "You are not allowed to remove SyncChests.");
                    return;
                }

                SyncChest syncChest = this.data.getChest(position);
                if (syncChest == null) {
                    // check for a DoubleChest
                    if (!isDoubleChest) {
                        PlayerUtils.sendError(player, Core.NAME, "This chest is not synced yet.");
                        return;
                    }

                    // check DoubleChest
                    position = new BlockVector(doubleChest.getLocation());
                    syncChest = this.data.getChest(position);
                    if (syncChest == null) {
                        PlayerUtils.sendError(player, Core.NAME, "This chest is not synced yet.");
                        return;
                    }

                    // try to remove the SyncChest
                    this.tryToRemoveSyncChest(player, syncChest);
                    return;
                }

                // try to remove the SyncChest
                this.tryToRemoveSyncChest(player, syncChest);
                break;
            }
            case CHEST_INFO : {
                this.setPlayerState(player, PlayerState.NONE);

                // check permissions
                if (!Permissions.hasPermission(player, Permissions.CHEST_INFO)) {
                    PlayerUtils.sendError(player, Core.NAME, "You are not allowed to get informations about SyncChests.");
                    return;
                }

                SyncChest syncChest = this.data.getChest(position);
                if (syncChest == null) {
                    // check for a DoubleChest
                    if (!isDoubleChest) {
                        PlayerUtils.sendInfo(player, Core.NAME, "This chest is not synced yet.");
                        return;
                    }

                    // check DoubleChest
                    syncChest = this.data.getChest(new BlockVector(doubleChest.getLocation()));
                    if (syncChest == null) {
                        PlayerUtils.sendInfo(player, Core.NAME, "This chest is not synced yet.");
                        return;
                    }

                    // print info
                    PlayerUtils.sendInfo(player, Core.NAME, "This chest is synced by '" + syncChest.getParent().getParent().getPlayerName() + "' in node '" + syncChest.getParent().getNodeName() + "'.");
                    return;
                }

                // print info
                PlayerUtils.sendInfo(player, Core.NAME, "This chest is synced by '" + syncChest.getParent().getParent().getPlayerName() + "' in node '" + syncChest.getParent().getNodeName() + "'.");
                return;
            }
        }
    }

    private void tryToAddSyncChest(Player player, Location location, boolean doubleChest) {
        BlockVector position = new BlockVector(location);

        String nodeName = this.nodeNames.get(player.getName());

        // check: does the SyncNode exist? If so: empty the new chest
        if (this.data.hasSyncNode(player.getName(), nodeName)) {
            Chest chest = (Chest) location.getBlock().getState();
            ItemStack itemStack;
            for (int i = 0; i < chest.getInventory().getSize(); i++) {
                itemStack = chest.getInventory().getItem(i);
                if (itemStack != null && itemStack.getTypeId() != Material.AIR.getId()) {
                    location.getWorld().dropItem(location, itemStack.clone());
                }
            }
            chest.getInventory().clear();
        }

        // check: create the SyncNode,if it not exists
        if (!this.data.hasSyncNode(player.getName(), nodeName)) {
            this.data.addSyncNode(player.getName(), nodeName, doubleChest);
        }

        // check: is the SyncNode currently locked?
        if (this.data.getSyncNode(player.getName(), nodeName).isLocked()) {
            PlayerUtils.sendError(player, Core.NAME, "This node is currently in use!");
            PlayerUtils.sendInfo(player, "Please try again...");
            return;
        }

        // lock the node
        this.data.getSyncNode(player.getName(), nodeName).setLocked(true);

        // create the SyncChest
        SyncChest syncChest = this.data.addChest(player.getName(), nodeName, position, doubleChest);

        // remove the SyncChest again, if the Inventorysizes are different
        if (this.data.getSyncNode(player.getName(), nodeName).isDoubleChest() != doubleChest) {
            if (this.data.getSyncNode(player.getName(), nodeName).isDoubleChest()) {
                PlayerUtils.sendError(player, Core.NAME, "All chests in this nodes must be double chests!");
            } else {
                PlayerUtils.sendError(player, Core.NAME, "All chests in this nodes must be single chests!");
            }
            this.data.removeChest(position);
            this.nodeNames.remove(player.getName());
            this.data.getSyncNode(player.getName(), nodeName).setLocked(false);
            return;
        }

        // is the chest valid?
        if (syncChest == null) {
            PlayerUtils.sendError(player, Core.NAME, "Could not create SyncChest! [0x00]");
            this.data.removeChest(position);
            this.nodeNames.remove(player.getName());
            this.data.getSyncNode(player.getName(), nodeName).setLocked(false);
            return;
        }

        // try to save the SyncChest into the database
        if (!this.databaseManager.saveSyncChest(syncChest)) {
            PlayerUtils.sendError(player, Core.NAME, "Could not save SyncChest! [0x01]");
            this.data.removeChest(position);
            this.nodeNames.remove(player.getName());
            this.data.getSyncNode(player.getName(), nodeName).setLocked(false);
            return;
        }

        // synchronize inventories
        if (!this.data.getSyncNode(player.getName(), nodeName).doStartUp()) {
            PlayerUtils.sendError(player, Core.NAME, "Could not synchronize inventories. [0x02]");
            this.data.removeChest(position);
            this.nodeNames.remove(player.getName());
            this.data.getSyncNode(player.getName(), nodeName).setLocked(false);
            return;
        }

        // unlock the node
        this.data.getSyncNode(player.getName(), nodeName).setLocked(false);

        // print info
        PlayerUtils.sendSuccess(player, Core.NAME, "This chest is now synced in node '" + this.nodeNames.get(player.getName()) + "'.");
        this.nodeNames.remove(player.getName());
    }

    private boolean tryToRemoveSyncChest(Player player, SyncChest syncChest) {
        // check owner
        if (!syncChest.isOwner(player.getName()) && !Permissions.hasPermission(player, Permissions.ADMIN)) {
            PlayerUtils.sendError(player, Core.NAME, "You are not allowed to remove this SyncChest.");
            return false;
        }

        // check: is the SyncNode currently locked?
        if (syncChest.getParent().isLocked()) {
            PlayerUtils.sendError(player, Core.NAME, "This node is currently in use!");
            PlayerUtils.sendInfo(player, "Please try again...");
            return false;
        }

        // try to remove the SyncChest from the Database
        if (!this.databaseManager.deleteSyncChest(syncChest)) {
            PlayerUtils.sendError(player, Core.NAME, "Could not remove SyncChest from Database! [1x00]");
            return false;
        }

        // remove SyncChest
        this.data.removeChest(syncChest.getPosition());

        // print info
        PlayerUtils.sendSuccess(player, Core.NAME, "This chest is no longer a SyncChest.");
        return true;
    }
}
