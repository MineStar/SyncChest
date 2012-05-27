package de.minestar.syncchest.units;

import de.minestar.syncchest.utils.BlockVector;

public class SyncChest {
    private final BlockVector position;
    private final SyncNode parent;

    public SyncChest(BlockVector position, SyncNode parent) {
        this.position = position;
        this.parent = parent;
    }

    public BlockVector getPosition() {
        return position;
    }

    public SyncNode getParent() {
        return parent;
    }

    public boolean isOwner(String playerName) {
        return playerName.equalsIgnoreCase(this.parent.getParent().getPlayerName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SyncChest) {
            return this.getPosition().equals(((SyncChest) obj).getPosition());
        }
        return false;
    }
}
