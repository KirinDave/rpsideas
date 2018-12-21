package com.kamefrede.rpsideas.network;

import com.kamefrede.rpsideas.entity.EntityGaussPulse.AmmoStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import vazkii.psi.common.Psi;

import javax.annotation.Nonnull;
import java.util.Random;

public class MessageSparkleSphere extends PacketBase {

    private Vec3d position;
    private AmmoStatus status;

    @SuppressWarnings("unused")
    public MessageSparkleSphere() {
        // NO-OP
    }

    public MessageSparkleSphere(Vec3d position, AmmoStatus status) {
        this.position = position;
        this.status = status;
    }

    @Override
    public void handle(@Nonnull MessageContext context) {
        World world = Minecraft.getMinecraft().world;
        Random rand = world.rand;

        int color = status.color;
        float r = ((color & 0xFF0000) >> 16) / 255f;
        float g = ((color & 0x00FF00) >> 8) / 255f;
        float b = (color & 0x0000FF) / 255f;

        for (int thetaInitial = 0; thetaInitial < 360; thetaInitial += 10) {
            for (int azimuthInitial = -90; azimuthInitial < 90; azimuthInitial += 10) {
                float theta = (thetaInitial + rand.nextInt(10)) * (float) Math.PI / 180;
                float azimuth = (azimuthInitial + rand.nextInt(10)) * (float) Math.PI / 180;
                float dist = (rand.nextFloat() * 2 + 3) * 0.065f;

                float x = MathHelper.sin(theta) * MathHelper.cos(azimuth) * dist;
                float y = MathHelper.sin(azimuth) * dist;
                float z = MathHelper.cos(theta) * MathHelper.cos(azimuth) * dist;

                Psi.proxy.sparkleFX(world, position.x, position.y, position.z, r, g, b, x, y, z, 1.2f, 12);
            }
        }
    }

    @Override
    public void read(@Nonnull PacketBuffer buf) {
        position = readVec(buf);
        status = readEnum(buf, AmmoStatus.class);
    }

    @Override
    public void write(@Nonnull PacketBuffer buf) {
        writeVec(buf, position);
        writeEnum(buf, status);
    }
}
