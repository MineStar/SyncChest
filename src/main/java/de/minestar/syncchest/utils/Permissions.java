package de.minestar.syncchest.utils;

import org.bukkit.entity.Player;

import com.bukkit.gemo.utils.UtilPermissions;

public class Permissions {

    //@formatter:off
    public static String ADMIN         = "syncchest.admin";    
    public static String CHEST_ADD     = "syncchest.chest.add";
    public static String CHEST_REMOVE  = "syncchest.chest.remove";
    public static String CHEST_INFO    = "syncchest.chest.info";
    public static String CHEST_USE     = "syncchest.chest.use";    
    //@formatter:on

    public static boolean hasPermission(Player player, String node) {
        return UtilPermissions.playerCanUseCommand(player, node) || UtilPermissions.playerCanUseCommand(player, ADMIN);
    }
}
