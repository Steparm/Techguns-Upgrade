package com.stepm.techgunsupgrade.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BloodParticle extends Particle {

    private float scale;

    public BloodParticle(World world, double x, double y, double z,
                         double vx, double vy, double vz, float size) {
        super(world, x, y, z, vx, vy, vz);

        this.particleRed = 0.7f + rand.nextFloat() * 0.3f;
        this.particleGreen = 0.0f;
        this.particleBlue = 0.0f;

        this.motionX = vx;
        this.motionY = vy;
        this.motionZ = vz;

        this.scale = size;
        this.particleScale = size;

        this.particleMaxAge = 40 + rand.nextInt(30);
        this.particleGravity = 0.15f;

        this.canCollide = true;
        this.setSize(0.02f, 0.02f);

        this.particleAlpha = 0.8f + rand.nextFloat() * 0.2f;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks,
                               float rotationX, float rotationZ, float rotationYZ,
                               float rotationXY, float rotationXZ) {
        float f = (this.particleAge + partialTicks) / this.particleMaxAge;
        float size = this.scale * (1.0f - f * 0.3f);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        float red = this.particleRed;
        float green = this.particleGreen;
        float blue = this.particleBlue;
        float alpha = this.particleAlpha * (1.0f - f * 0.5f);

        Tessellator tessellator = Tessellator.getInstance();
        buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

        float f1 = 0.0f;
        float f2 = 1.0f;
        float f3 = 0.0f;
        float f4 = 1.0f;

        float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);

        int lightmap = this.getBrightnessForRender(partialTicks);
        int lightmapX = lightmap >> 16 & 65535;
        int lightmapY = lightmap & 65535;

        buffer.pos(x - rotationX * size - rotationXY * size, y - rotationZ * size, z - rotationYZ * size - rotationXZ * size)
              .tex(f1, f4).color(red, green, blue, alpha).lightmap(lightmapX, lightmapY).endVertex();
        buffer.pos(x - rotationX * size + rotationXY * size, y + rotationZ * size, z - rotationYZ * size + rotationXZ * size)
              .tex(f2, f4).color(red, green, blue, alpha).lightmap(lightmapX, lightmapY).endVertex();
        buffer.pos(x + rotationX * size + rotationXY * size, y + rotationZ * size, z + rotationYZ * size + rotationXZ * size)
              .tex(f2, f3).color(red, green, blue, alpha).lightmap(lightmapX, lightmapY).endVertex();
        buffer.pos(x + rotationX * size - rotationXY * size, y - rotationZ * size, z + rotationYZ * size - rotationXZ * size)
              .tex(f1, f3).color(red, green, blue, alpha).lightmap(lightmapX, lightmapY).endVertex();

        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }

    @Override
    public int getFXLayer() {
        return 3;
    }
}