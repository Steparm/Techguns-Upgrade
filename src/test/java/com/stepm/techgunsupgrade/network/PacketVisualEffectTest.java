package com.stepm.techgunsupgrade.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacketVisualEffectTest {
    @Test
    void roundTripsEveryField() {
        PacketVisualEffect expected = new PacketVisualEffect(
                PacketVisualEffect.NUCLEAR_CHARGE, 6, -1,
                12.25, 64.5, -81.75,
                14.0, 70.25, -79.0,
                15.5f, 0.625f, 0x5A17C0DE);
        ByteBuf buffer = Unpooled.buffer();
        expected.toBytes(buffer);

        PacketVisualEffect actual = new PacketVisualEffect();
        actual.fromBytes(buffer);

        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getStyle(), actual.getStyle());
        assertEquals(expected.getDimension(), actual.getDimension());
        assertEquals(expected.getX(), actual.getX(), 0.0);
        assertEquals(expected.getY(), actual.getY(), 0.0);
        assertEquals(expected.getZ(), actual.getZ(), 0.0);
        assertEquals(expected.getEndX(), actual.getEndX(), 0.0);
        assertEquals(expected.getEndY(), actual.getEndY(), 0.0);
        assertEquals(expected.getEndZ(), actual.getEndZ(), 0.0);
        assertEquals(expected.getRadius(), actual.getRadius(), 0.0f);
        assertEquals(expected.getProgress(), actual.getProgress(), 0.0f);
        assertEquals(expected.getSeed(), actual.getSeed());
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void visualTypeIdsStayStableAndUnique() {
        assertEquals(0, PacketVisualEffect.ZONE);
        assertEquals(1, PacketVisualEffect.AREA);
        assertEquals(2, PacketVisualEffect.BEAM);
        assertEquals(3, PacketVisualEffect.SKY_STRIKE);
        assertEquals(4, PacketVisualEffect.FIRE_RAIN);
        assertEquals(5, PacketVisualEffect.ARTILLERY);
        assertEquals(6, PacketVisualEffect.SMOKE);
        assertEquals(7, PacketVisualEffect.FIRE_ZONE);
        assertEquals(8, PacketVisualEffect.NUCLEAR_CHARGE);
        assertEquals(9, PacketVisualEffect.NUCLEAR_BLAST);
        assertEquals(10, PacketVisualEffect.SPECIAL);
    }
}
