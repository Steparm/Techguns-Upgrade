package com.stepm.techgunsupgrade.network;

import com.stepm.techgunsupgrade.tileentity.TileEntityUpgradeTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketStartUpgrade implements IMessage {

    private BlockPos pos;

    public PacketStartUpgrade() {}

    public PacketStartUpgrade(BlockPos pos) {
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

    public static class Handler implements IMessageHandler<PacketStartUpgrade, IMessage> {
        @Override
        public IMessage onMessage(PacketStartUpgrade message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.getDistanceSq(message.pos) > 64.0D) return;

                TileEntity tile = player.world.getTileEntity(message.pos);
                if (tile instanceof TileEntityUpgradeTable) {
                    ((TileEntityUpgradeTable) tile).startUpgrade();
                }
            });

            return null;
        }
    }
}