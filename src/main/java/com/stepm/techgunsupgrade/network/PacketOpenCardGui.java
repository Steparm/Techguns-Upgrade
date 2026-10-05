package com.stepm.techgunsupgrade.network;

import com.stepm.techgunsupgrade.card.Card;
import com.stepm.techgunsupgrade.card.CardRarity;
import com.stepm.techgunsupgrade.card.CardType;
import com.stepm.techgunsupgrade.card.CardVotingManager;
import com.stepm.techgunsupgrade.card.gui.GuiCardChoice;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class PacketOpenCardGui implements IMessage {

    private int wave;
    private List<Card> cards;

    public PacketOpenCardGui() {}

    public PacketOpenCardGui(int wave, List<Card> cards) {
        this.wave = wave;
        this.cards = cards;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        wave = buf.readInt();
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        cards = new ArrayList<>();
        NBTTagList list = tag.getTagList("cards", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound cardTag = list.getCompoundTagAt(i);
            CardRarity rarity = CardRarity.valueOf(cardTag.getString("rarity"));
            CardType type = CardType.valueOf(cardTag.getString("type"));
            Card card = new Card(
                cardTag.getString("id"),
                rarity,
                type,
                cardTag.getInteger("value")
            );
            cards.add(card);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(wave);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (Card card : cards) {
            NBTTagCompound cardTag = new NBTTagCompound();
            cardTag.setString("id", card.getId());
            cardTag.setString("rarity", card.getRarity().name());
            cardTag.setString("type", card.getType().name());
            cardTag.setInteger("value", card.getValue());
            list.appendTag(cardTag);
        }
        tag.setTag("cards", list);
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<PacketOpenCardGui, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenCardGui message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                handleClient(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handleClient(PacketOpenCardGui message) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
                if (mc.player != null) {
                    CardVotingManager.setPlayerCards(mc.player, message.cards);
                    mc.displayGuiScreen(new GuiCardChoice(mc.player, message.cards, message.wave));
                }
            });
        }
    }
}