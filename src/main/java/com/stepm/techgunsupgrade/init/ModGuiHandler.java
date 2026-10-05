package com.stepm.techgunsupgrade.init;

import com.stepm.techgunsupgrade.gui.ContainerUpgradeTable;
import com.stepm.techgunsupgrade.gui.GuiUpgradeTable;
import com.stepm.techgunsupgrade.tileentity.TileEntityUpgradeTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ModGuiHandler implements IGuiHandler {

    public static final int GUN_UPGRADE_TABLE = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUN_UPGRADE_TABLE) {
            TileEntityUpgradeTable tile = (TileEntityUpgradeTable) world.getTileEntity(new BlockPos(x, y, z));
            return new ContainerUpgradeTable(player, tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUN_UPGRADE_TABLE) {
            TileEntityUpgradeTable tile = (TileEntityUpgradeTable) world.getTileEntity(new BlockPos(x, y, z));
            return new GuiUpgradeTable(new ContainerUpgradeTable(player, tile), tile);
        }
        return null;
    }
}