package com.stepm.techgunsupgrade.network;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** One compact description of an elaborate client-rendered combat effect. */
public final class PacketVisualEffect implements IMessage {
    public static final int ZONE = 0;
    public static final int AREA = 1;
    public static final int BEAM = 2;
    public static final int SKY_STRIKE = 3;
    public static final int FIRE_RAIN = 4;
    public static final int ARTILLERY = 5;
    public static final int SMOKE = 6;
    public static final int FIRE_ZONE = 7;
    public static final int NUCLEAR_CHARGE = 8;
    public static final int NUCLEAR_BLAST = 9;
    public static final int SPECIAL = 10;

    private int type;
    private int style;
    private int dimension;
    private double x;
    private double y;
    private double z;
    private double endX;
    private double endY;
    private double endZ;
    private float radius;
    private float progress;
    private int seed;

    public PacketVisualEffect() {
    }

    public PacketVisualEffect(int type, int style, int dimension,
                              double x, double y, double z,
                              double endX, double endY, double endZ,
                              float radius, float progress, int seed) {
        this.type = type;
        this.style = style;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.radius = radius;
        this.progress = progress;
        this.seed = seed;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = buf.readUnsignedByte();
        style = buf.readUnsignedByte();
        dimension = buf.readInt();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        endX = buf.readDouble();
        endY = buf.readDouble();
        endZ = buf.readDouble();
        radius = buf.readFloat();
        progress = buf.readFloat();
        seed = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(type);
        buf.writeByte(style);
        buf.writeInt(dimension);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(endX);
        buf.writeDouble(endY);
        buf.writeDouble(endZ);
        buf.writeFloat(radius);
        buf.writeFloat(progress);
        buf.writeInt(seed);
    }

    public int getType() {
        return type;
    }

    public int getStyle() {
        return style;
    }

    public int getDimension() {
        return dimension;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public double getEndZ() {
        return endZ;
    }

    public float getRadius() {
        return radius;
    }

    public float getProgress() {
        return progress;
    }

    public int getSeed() {
        return seed;
    }

    public static final class Handler implements IMessageHandler<PacketVisualEffect, IMessage> {
        @Override
        public IMessage onMessage(PacketVisualEffect message, MessageContext context) {
            // The sided proxy keeps every net.minecraft.client reference out of
            // this common packet class, so dedicated servers can load it safely.
            TechgunsUpgradeMod.proxy.handleVisualEffect(message);
            return null;
        }
    }
}
