package com.stepm.techgunsupgrade.client;

import com.stepm.techgunsupgrade.blocks.BlockUpgradeTable;
import com.stepm.techgunsupgrade.tileentity.TileEntityUpgradeTable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/** Renders the actual input/output weapon inside the station's glass chamber. */
public class UpgradeTableRenderer extends TileEntitySpecialRenderer<TileEntityUpgradeTable> {

    @Override
    public void render(TileEntityUpgradeTable tile, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        ItemStack displayedStack = tile.getStackInSlot(0);
        if (displayedStack.isEmpty()) {
            displayedStack = tile.getStackInSlot(2);
        }
        if (displayedStack.isEmpty()) {
            return;
        }

        EnumFacing facing = EnumFacing.SOUTH;
        if (tile.hasWorld()) {
            IBlockState state = tile.getWorld().getBlockState(tile.getPos());
            if (state.getBlock() instanceof BlockUpgradeTable) {
                facing = state.getValue(BlockUpgradeTable.FACING);
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.77D, z + 0.5D);
        GlStateManager.rotate(getFacingRotation(facing), 0.0F, 1.0F, 0.0F);

        // The chamber window is on the local south side of the model.
        // Keep the item slightly behind the reinforced glass. Techguns models
        // already apply their own FIXED transform and scale, so the previous
        // 0.68 outer scale made rifles and heavy weapons leave the chamber.
        GlStateManager.translate(0.0D, 0.0D, 0.145D);
        float displayScale = getDisplayScale(displayedStack);
        GlStateManager.scale(displayScale, displayScale, displayScale);
        GlStateManager.enableRescaleNormal();
        Minecraft.getMinecraft().getRenderItem().renderItem(
                displayedStack,
                ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    private float getDisplayScale(ItemStack stack) {
        ResourceLocation registryName = stack.getItem().getRegistryName();
        if (registryName == null) {
            return 0.32F;
        }

        String path = registryName.toString();
        if (path.contains("minigun")
                || path.contains("rocketlauncher")
                || path.contains("guidedmissilelauncher")
                || path.contains("grenadelauncher")
                || path.contains("nucleardeathray")
                || path.contains("flamethrower")) {
            return 0.25F;
        }
        if (path.contains("pistol")
                || path.contains("revolver")
                || path.contains("handcannon")) {
            return 0.40F;
        }
        return 0.32F;
    }

    private float getFacingRotation(EnumFacing facing) {
        switch (facing) {
            case NORTH:
                return 180.0F;
            case EAST:
                return 270.0F;
            case WEST:
                return 90.0F;
            case SOUTH:
            default:
                return 0.0F;
        }
    }

}
