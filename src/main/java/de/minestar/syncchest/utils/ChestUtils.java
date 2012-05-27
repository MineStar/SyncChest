package de.minestar.syncchest.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;

import de.minestar.syncchest.units.DataNode;
import de.minestar.syncchest.units.SyncChest;

public class ChestUtils {

    public static SyncChest getSyncChest(DataNode data, BlockVector position) {
        SyncChest syncChest = data.getChest(position);
        if (syncChest != null) {
            return syncChest;
        }

        Location location = position.getLocation();
        Chest doubleChest = ChestUtils.getDoubleChest(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (doubleChest != null) {
            return data.getChest(new BlockVector(doubleChest.getLocation()));
        }

        return null;
    }

    public static boolean isDoubleChest(World world, int x, int y, int z) {
        if (world.getBlockTypeIdAt(x + 1, y, z) == Material.CHEST.getId())
            return true;

        if (world.getBlockTypeIdAt(x - 1, y, z) == Material.CHEST.getId())
            return true;

        if (world.getBlockTypeIdAt(x, y, z + 1) == Material.CHEST.getId())
            return true;

        if (world.getBlockTypeIdAt(x, y, z - 1) == Material.CHEST.getId())
            return true;

        return false;
    }

    public static Chest getDoubleChest(World world, int x, int y, int z) {
        if (world.getBlockTypeIdAt(x + 1, y, z) == Material.CHEST.getId())
            return (Chest) world.getBlockAt(x + 1, y, z).getState();

        if (world.getBlockTypeIdAt(x - 1, y, z) == Material.CHEST.getId())
            return (Chest) world.getBlockAt(x - 1, y, z).getState();

        if (world.getBlockTypeIdAt(x, y, z + 1) == Material.CHEST.getId())
            return (Chest) world.getBlockAt(x, y, z + 1).getState();

        if (world.getBlockTypeIdAt(x, y, z - 1) == Material.CHEST.getId())
            return (Chest) world.getBlockAt(x, y, z - 1).getState();

        return null;
    }
}
