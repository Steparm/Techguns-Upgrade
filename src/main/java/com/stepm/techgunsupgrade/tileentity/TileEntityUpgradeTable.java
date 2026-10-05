package com.stepm.techgunsupgrade.tileentity;

import com.stepm.techgunsupgrade.items.ItemTicket;
import com.stepm.techgunsupgrade.manager.UpgradeApplicator;
import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeData;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TileEntityUpgradeTable extends TileEntity implements IInventory, ICapabilityProvider, ITickable {

    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            syncToClient();
        }
    };

    private boolean isUpgrading = false;
    private int upgradeProgress = 0;
    private int upgradeTime = 40;
    private ItemStack resultStack = ItemStack.EMPTY;
    private String selectedRarity = "";
    private String selectedBuffName = "";
    private Random random = new Random();

    private void syncToClient() {
        if (world != null && !world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    public void startUpgrade() {
        ItemStack weapon = inventory.getStackInSlot(0);
        ItemStack ticket = inventory.getStackInSlot(1);

        if (weapon.isEmpty() || ticket.isEmpty()) return;
        if (!isTicket(ticket)) return;
        if (isUpgrading) return;
        if (!inventory.getStackInSlot(2).isEmpty()) return;
        if (UpgradeData.getUpgrades(weapon).size() >= 2) return;
        // Temporarily disabled frozen check
        // if (UpgradeData.isFrozen(weapon)) return;

        UpgradeRarity rarity = selectRarity(ticket);
        if (rarity == null) {
            isUpgrading = false;
            return;
        }

        String weaponId = weapon.getItem().getRegistryName().toString();
        UpgradeBuff buff = selectBuffForWeapon(weaponId, rarity);
        if (buff == null) {
            isUpgrading = false;
            return;
        }

        resultStack = weapon.copy();
        UpgradeData.addUpgrade(resultStack, buff.getId());
        UpgradeApplicator.applyUpgradesToGun(resultStack);

        selectedRarity = rarity.getName();
        selectedBuffName = buff.getDisplayName();

        NBTTagCompound upgradeTag = resultStack.getOrCreateSubCompound("techgunsupgrade");
        upgradeTag.setString("buffName", buff.getDisplayName());
        upgradeTag.setString("buffRarity", rarity.getName());
        upgradeTag.setString("buffDescription", buff.getDescription());
        upgradeTag.setString("buffId", buff.getId());

        if (buff.isStacking()) {
            upgradeTag.setInteger("stackCount", 0);
            upgradeTag.setInteger("maxStack", buff.getMaxStack());
        }

        completeUpgrade();
    }

    public boolean resetInputUpgrades(EntityPlayer player) {
        if (world == null || world.isRemote || player == null || isUpgrading
                || !inventory.getStackInSlot(2).isEmpty()) {
            return false;
        }
        ItemStack weapon = inventory.getStackInSlot(0);
        if (weapon.isEmpty() || UpgradeData.getUpgrades(weapon).isEmpty()) {
            return false;
        }

        ItemStack cleared = weapon.copy();
        UpgradeApplicator.removeAllUpgrades(cleared);
        inventory.setStackInSlot(0, cleared);
        markDirty();
        syncToClient();
        return true;
    }

    private UpgradeRarity selectRarity(ItemStack ticket) {
        double roll = random.nextDouble() * 100;
        int ticketTier = ((ItemTicket) ticket.getItem()).getTier();

        switch (ticketTier) {
            case 0: return getIronRarity(roll);
            case 1: return getGoldenRarity(roll);
            case 2: return getDiamondRarity(roll);
            case 3: return getNetheriteRarity(roll);
            case 4: return getStarRarity(roll);
            case 5: return UpgradeRarity.ULTRA_MYTHIC;
            default: return getIronRarity(roll);
        }
    }

    private UpgradeRarity getIronRarity(double roll) {
        if (roll < 0.1) return UpgradeRarity.ULTRA_MYTHIC;
        if (roll < 0.8) return UpgradeRarity.MYTHIC;
        if (roll < 2.8) return UpgradeRarity.LEGENDARY;
        if (roll < 9.8) return UpgradeRarity.EPIC;
        if (roll < 24.8) return UpgradeRarity.RARE;
        if (roll < 52.8) return UpgradeRarity.UNCOMMON;
        return UpgradeRarity.COMMON;
    }

    private UpgradeRarity getGoldenRarity(double roll) {
        if (roll < 0.25) return UpgradeRarity.ULTRA_MYTHIC;
        if (roll < 2.75) return UpgradeRarity.MYTHIC;
        if (roll < 6.75) return UpgradeRarity.LEGENDARY;
        if (roll < 15.75) return UpgradeRarity.EPIC;
        if (roll < 32.75) return UpgradeRarity.RARE;
        if (roll < 59.75) return UpgradeRarity.UNCOMMON;
        return UpgradeRarity.COMMON;
    }

    private UpgradeRarity getDiamondRarity(double roll) {
        if (roll < 0.5) return UpgradeRarity.ULTRA_MYTHIC;
        if (roll < 4.5) return UpgradeRarity.MYTHIC;
        if (roll < 11.5) return UpgradeRarity.LEGENDARY;
        if (roll < 24.5) return UpgradeRarity.EPIC;
        if (roll < 43.5) return UpgradeRarity.RARE;
        if (roll < 67.5) return UpgradeRarity.UNCOMMON;
        return UpgradeRarity.COMMON;
    }

    private UpgradeRarity getNetheriteRarity(double roll) {
        if (roll < 0.75) return UpgradeRarity.ULTRA_MYTHIC;
        if (roll < 8.25) return UpgradeRarity.MYTHIC;
        if (roll < 20.25) return UpgradeRarity.LEGENDARY;
        if (roll < 38.25) return UpgradeRarity.EPIC;
        if (roll < 57.25) return UpgradeRarity.RARE;
        if (roll < 77.25) return UpgradeRarity.UNCOMMON;
        return UpgradeRarity.COMMON;
    }

    private UpgradeRarity getStarRarity(double roll) {
        if (roll < 1.0) return UpgradeRarity.ULTRA_MYTHIC;
        if (roll < 31.0) return UpgradeRarity.MYTHIC;
        if (roll < 81.0) return UpgradeRarity.LEGENDARY;
        if (roll < 96.0) return UpgradeRarity.EPIC;
        return UpgradeRarity.RARE;
    }

    private UpgradeBuff selectBuffForWeapon(String weaponId, UpgradeRarity rarity) {
        List<UpgradeBuff> buffs = WeaponUpgrades.getUpgradesByRarity(weaponId, rarity);
        if (buffs.isEmpty()) return null;
        return buffs.get(random.nextInt(buffs.size()));
    }

    private boolean isTicket(ItemStack stack) {
        return stack.getItem() instanceof ItemTicket;
    }

    @Override
    public void update() {
        if (isUpgrading) {
            upgradeProgress++;
            if (upgradeProgress >= upgradeTime) {
                completeUpgrade();
            }
            markDirty();
        }
    }

    private void completeUpgrade() {
        if (!resultStack.isEmpty()) {
            inventory.setStackInSlot(2, resultStack);
            inventory.setStackInSlot(0, ItemStack.EMPTY);

            ItemStack ticket = inventory.getStackInSlot(1);
            ticket.shrink(1);
            inventory.setStackInSlot(1, ticket.isEmpty() ? ItemStack.EMPTY : ticket);
        }

        isUpgrading = false;
        upgradeProgress = 0;
        resultStack = ItemStack.EMPTY;
        selectedRarity = "";
        selectedBuffName = "";
        markDirty();
    }

    public boolean isUpgrading() {
        return isUpgrading;
    }

    public int getUpgradeProgress() {
        return upgradeProgress;
    }

    public int getUpgradeTime() {
        return upgradeTime;
    }

    public String getSelectedRarity() {
        return selectedRarity;
    }

    public String getSelectedBuffName() {
        return selectedBuffName;
    }

    public ItemStack getResultStack() {
        return resultStack;
    }

    @Override
    public int getSizeInventory() { return 4; }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < 4; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return inventory.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = inventory.getStackInSlot(index);
        inventory.setStackInSlot(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.setStackInSlot(index, stack);
    }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0) return true;
        if (index == 1) return isTicket(stack);
        if (index == 2) return false;
        return true;
    }

    @Override
    public int getField(int id) {
        if (id == 0) return isUpgrading ? 1 : 0;
        if (id == 1) return upgradeProgress;
        if (id == 2) return upgradeTime;
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        if (id == 0) isUpgrading = value == 1;
        if (id == 1) upgradeProgress = value;
        if (id == 2) upgradeTime = value;
    }

    @Override
    public int getFieldCount() { return 3; }

    @Override
    public void clear() {
        for (int i = 0; i < 4; i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public String getName() {
        return "container.upgrade_table";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("Gun Upgrade Table");
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("inventory", inventory.serializeNBT());
        compound.setBoolean("isUpgrading", isUpgrading);
        compound.setInteger("upgradeProgress", upgradeProgress);
        compound.setInteger("upgradeTime", upgradeTime);
        compound.setString("selectedRarity", selectedRarity);
        compound.setString("selectedBuffName", selectedBuffName);
        if (!resultStack.isEmpty()) {
            NBTTagCompound resultTag = new NBTTagCompound();
            resultStack.writeToNBT(resultTag);
            compound.setTag("resultStack", resultTag);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        isUpgrading = compound.getBoolean("isUpgrading");
        upgradeProgress = compound.getInteger("upgradeProgress");
        upgradeTime = compound.getInteger("upgradeTime");
        selectedRarity = compound.getString("selectedRarity");
        selectedBuffName = compound.getString("selectedBuffName");
        if (compound.hasKey("resultStack")) {
            resultStack = new ItemStack(compound.getCompoundTag("resultStack"));
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }
}