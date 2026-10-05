package com.stepm.techgunsupgrade.network;

import com.stepm.techgunsupgrade.apocalypse.ZombieApocalypseManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSyncApocalypseData implements IMessage {

    private boolean isActive;
    private int currentWave;
    private int totalKills;
    private int zombiesAlive;

    public PacketSyncApocalypseData() {}

    public PacketSyncApocalypseData(boolean isActive, int currentWave, int totalKills, int zombiesAlive) {
        this.isActive = isActive;
        this.currentWave = currentWave;
        this.totalKills = totalKills;
        this.zombiesAlive = zombiesAlive;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isActive = buf.readBoolean();
        currentWave = buf.readInt();
        totalKills = buf.readInt();
        zombiesAlive = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isActive);
        buf.writeInt(currentWave);
        buf.writeInt(totalKills);
        buf.writeInt(zombiesAlive);
    }

    public static class Handler implements IMessageHandler<PacketSyncApocalypseData, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncApocalypseData message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                handleClient(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handleClient(PacketSyncApocalypseData message) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                ZombieApocalypseManager.setClientData(
                    message.isActive,
                    message.currentWave,
                    message.totalKills,
                    message.zombiesAlive
                );
            });
        }
    }
}