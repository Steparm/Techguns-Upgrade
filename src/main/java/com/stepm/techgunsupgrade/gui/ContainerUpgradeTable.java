package com.stepm.techgunsupgrade.gui;

import com.stepm.techgunsupgrade.items.ItemTicket;
import com.stepm.techgunsupgrade.tileentity.TileEntityUpgradeTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerUpgradeTable extends Container {

    private final TileEntityUpgradeTable tile;
    private int lastUpgradeProgress = 0;
    private boolean lastIsUpgrading = false;

    public ContainerUpgradeTable(EntityPlayer player, TileEntityUpgradeTable tile) {
        this.tile = tile;

        addSlotToContainer(new Slot(tile, 0, 18, 32) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return true;
            }
        });
        
        addSlotToContainer(new Slot(tile, 1, 18, 62) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemTicket;
            }
        });
        
        addSlotToContainer(new Slot(tile, 2, 216, 87) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
            
            @Override
            public boolean canTakeStack(EntityPlayer playerIn) {
                return !tile.isUpgrading();
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, 43 + col * 18, 116 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(player.inventory, col, 43 + col * 18, 174));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();

            if (index < 3) {
                if (!mergeItemStack(stackInSlot, 3, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stackInSlot.getItem() instanceof ItemTicket && !tile.isUpgrading()) {
                    if (!mergeItemStack(stackInSlot, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!tile.isUpgrading()) {
                    if (!mergeItemStack(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return stack;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        
        for (IContainerListener listener : listeners) {
            if (lastUpgradeProgress != tile.getUpgradeProgress()) {
                listener.sendWindowProperty(this, 0, tile.getUpgradeProgress());
            }
            if (lastIsUpgrading != tile.isUpgrading()) {
                listener.sendWindowProperty(this, 1, tile.isUpgrading() ? 1 : 0);
            }
        }
        
        lastUpgradeProgress = tile.getUpgradeProgress();
        lastIsUpgrading = tile.isUpgrading();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            tile.setField(1, data);
        } else if (id == 1) {
            tile.setField(0, data);
        }
    }

    public TileEntityUpgradeTable getTile() {
        return tile;
    }
}
