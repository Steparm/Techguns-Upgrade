package com.stepm.techgunsupgrade.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileUpgradeStation extends TileEntity {

    private boolean active = false;
    private int upgradeProgress = 0;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        markDirty();
    }

    public int getUpgradeProgress() {
        return upgradeProgress;
    }

    public void setUpgradeProgress(int progress) {
        this.upgradeProgress = progress;
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.active = compound.getBoolean("Active");
        this.upgradeProgress = compound.getInteger("UpgradeProgress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("Active", this.active);
        compound.setInteger("UpgradeProgress", this.upgradeProgress);
        return compound;
    }
}