package com.stepm.techgunsupgrade.network;

import com.stepm.techgunsupgrade.gui.ContainerUpgradeTable;
import com.stepm.techgunsupgrade.tileentity.TileEntityUpgradeTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Server-authoritative request to remove every TGU upgrade from the input gun. */
public class PacketResetUpgrades implements IMessage {

    private BlockPos pos;

    public PacketResetUpgrades() {
    }

    public PacketResetUpgrades(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
    }

    public static class Handler implements IMessageHandler<PacketResetUpgrades, IMessage> {
        @Override
        public IMessage onMessage(PacketResetUpgrades message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (message.pos == null || player.getDistanceSq(message.pos) > 64.0D
                        || !(player.openContainer instanceof ContainerUpgradeTable)) {
                    return;
                }

                ContainerUpgradeTable container = (ContainerUpgradeTable) player.openContainer;
                if (container.getTile().getPos() == null
                        || !container.getTile().getPos().equals(message.pos)) {
                    return;
                }

                TileEntity tile = player.world.getTileEntity(message.pos);
                if (tile instanceof TileEntityUpgradeTable) {
                    ((TileEntityUpgradeTable) tile).resetInputUpgrades(player);
                }
            });
            return null;
        }
    }
}
