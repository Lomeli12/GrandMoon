package net.lomeli.grandmoon;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;

import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHandler {
    private float renderTick;
    private boolean hasReset;

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent tickEvent) {
        if (tickEvent.phase == TickEvent.Phase.END)
            renderTick = tickEvent.renderTickTime;
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent playerTickEvent) {
        if (playerTickEvent.phase == TickEvent.Phase.END) {
            EntityPlayer player = playerTickEvent.player;
            if (!player.worldObj.isRemote && player.worldObj.isDaytime()) {
                if (!hasReset) {
                    MoonClientHooks.INSTANCE.resetSize();
                    hasReset = true;
                }
            }
        }
    }

    @SubscribeEvent
    public void fireBowEvent(ArrowLooseEvent event) {
        if (!event.isCanceled()) {
            EntityPlayer player = event.entityPlayer;
            if (isLookingAtMoon(player.worldObj, player, renderTick, true)) {
                MoonClientHooks.INSTANCE.changeMoonSize();
                hasReset = false;
            }
        }
    }

    // ------- Copied from iChunUtil. Thanks iChun, I owe you 1 chocolate cupcake ------

    private boolean isLookingAtMoon(World world, EntityLivingBase entity, float renderTick, boolean canLookThroughGlass) {
        if (entity.dimension == -1 || entity.dimension == 1)
            return false;
        double de = 2.71828183D;
        float f = world.getCelestialAngle(1.0F);

        if (!(f >= 0.26D && f <= 0.74D))
            return false;

        float f2 = f > 0.5F ? f - 0.5F : 0.5F - f;
        float f3 = entity.rotationYaw > 0F ? 270 : -90;
        f3 = f > 0.5F ? entity.rotationYaw > 0F ? 90 : -270 : f3;
        f = f > 0.5F ? 1.0F - f : f;

        if (f <= 0.475)
            de = 2.71828183D;
        else if (f <= 0.4875)
            de = 3.88377D;
        else if (f <= 0.4935)
            de = 4.91616;
        else if (f <= 0.4965)
            de = 5.40624;
        else if (f <= 0.5000)
            de = 9.8;

        //yaw check = player.rotationYaw % 360 <= Math.pow(de, (4.92574 * mc.theWorld.getCelestialAngle(1.0F))) + f3 && mc.thePlayer.rotationYaw % 360 >= -Math.pow(de, (4.92574 * mc.theWorld.getCelestialAngle(1.0F))) + f3
        boolean yawCheck = entity.rotationYaw % 360 <= Math.pow(de, (4.92574 * world.getCelestialAngle(1.0F))) + f3 && entity.rotationYaw % 360 >= -Math.pow(de, (4.92574 * world.getCelestialAngle(1.0F))) + f3;
        float ff = world.getCelestialAngle(1.0F);
        ff = ff > 0.5F ? 1.0F - ff : ff;
        ff -= 0.26F;
        ff = (ff / 0.26F) * -94F - 4F;
        //pitch check = mc.thePlayer.rotationPitch <= ff + 2.5F && mc.thePlayer.rotationPitch >= ff - 2.5F
        boolean pitchCheck = entity.rotationPitch <= ff + 2.5F && entity.rotationPitch >= ff - 2.5F;
        Vec3 vec3d = entity.getPositionEyes(renderTick);
        Vec3 vec3d1 = entity.getLook(renderTick);
        Vec3 vec3d2 = vec3d.addVector(vec3d1.xCoord * 500D, vec3d1.yCoord * 500D, vec3d1.zCoord * 500D);
        boolean mopCheck = rayTrace(entity.worldObj, vec3d, vec3d2, true, false, canLookThroughGlass, 500) == null;
        return (yawCheck && pitchCheck && mopCheck);
    }

    public MovingObjectPosition rayTrace(World world, Vec3 vec3d, Vec3 vec3d1, boolean flag, boolean flag1, boolean goThroughTransparentBlocks, int distance) {
        if (Double.isNaN(vec3d.xCoord) || Double.isNaN(vec3d.yCoord) || Double.isNaN(vec3d.zCoord))
            return null;
        if (Double.isNaN(vec3d1.xCoord) || Double.isNaN(vec3d1.yCoord) || Double.isNaN(vec3d1.zCoord))
            return null;

        int i = MathHelper.floor_double(vec3d1.xCoord);
        int j = MathHelper.floor_double(vec3d1.yCoord);
        int k = MathHelper.floor_double(vec3d1.zCoord);
        int l = MathHelper.floor_double(vec3d.xCoord);
        int i1 = MathHelper.floor_double(vec3d.yCoord);
        int j1 = MathHelper.floor_double(vec3d.zCoord);
        BlockPos blockpos = new BlockPos(l, i1, j1);
        IBlockState iblockstate = world.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if ((!flag1 || block.getCollisionBoundingBox(world, blockpos, iblockstate) != null) && block.canCollideCheck(iblockstate, flag)) {
            MovingObjectPosition movingobjectposition = block.collisionRayTrace(world, blockpos, vec3d, vec3d1);

            if (movingobjectposition != null)
                return movingobjectposition;
        }

        for (int l1 = distance; l1-- >= 0; ) {
            if (Double.isNaN(vec3d.xCoord) || Double.isNaN(vec3d.yCoord) || Double.isNaN(vec3d.zCoord))
                return null;

            if (l == i && i1 == j && j1 == k)
                return null;

            boolean flag5 = true;
            boolean flag3 = true;
            boolean flag4 = true;
            double d0 = 999.0D;
            double d1 = 999.0D;
            double d2 = 999.0D;

            if (i > l)
                d0 = (double) l + 1.0D;
            else if (i < l)
                d0 = (double) l + 0.0D;
            else
                flag5 = false;

            if (j > i1)
                d1 = (double) i1 + 1.0D;
            else if (j < i1)
                d1 = (double) i1 + 0.0D;
            else
                flag3 = false;

            if (k > j1)
                d2 = (double) j1 + 1.0D;
            else if (k < j1)
                d2 = (double) j1 + 0.0D;
            else
                flag4 = false;

            double d3 = 999.0D;
            double d4 = 999.0D;
            double d5 = 999.0D;
            double d6 = vec3d1.xCoord - vec3d.xCoord;
            double d7 = vec3d1.yCoord - vec3d.yCoord;
            double d8 = vec3d1.zCoord - vec3d.zCoord;

            if (flag5)
                d3 = (d0 - vec3d.xCoord) / d6;

            if (flag3)
                d4 = (d1 - vec3d.yCoord) / d7;

            if (flag4)
                d5 = (d2 - vec3d.zCoord) / d8;

            if (d3 == -0.0D)
                d3 = -1.0E-4D;

            if (d4 == -0.0D)
                d4 = -1.0E-4D;

            if (d5 == -0.0D)
                d5 = -1.0E-4D;

            EnumFacing enumfacing;

            if (d3 < d4 && d3 < d5) {
                enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                vec3d = new Vec3(d0, vec3d.yCoord + d7 * d3, vec3d.zCoord + d8 * d3);
            } else if (d4 < d5) {
                enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                vec3d = new Vec3(vec3d.xCoord + d6 * d4, d1, vec3d.zCoord + d8 * d4);
            } else {
                enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                vec3d = new Vec3(vec3d.xCoord + d6 * d5, vec3d.yCoord + d7 * d5, d2);
            }

            l = MathHelper.floor_double(vec3d.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
            i1 = MathHelper.floor_double(vec3d.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
            j1 = MathHelper.floor_double(vec3d.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);

            blockpos = new BlockPos(l, i1, j1);
            IBlockState iblockstate1 = world.getBlockState(blockpos);
            Block block1 = iblockstate1.getBlock();

            if (goThroughTransparentBlocks && block1.getLightOpacity() != 0xff)
                continue;

            if ((!flag1 || block1.getCollisionBoundingBox(world, blockpos, iblockstate1) != null) && block1.canCollideCheck(iblockstate1, flag)) {
                MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(world, blockpos, vec3d, vec3d1);

                if (movingobjectposition1 != null)
                    return movingobjectposition1;
            }
        }
        return null;
    }
}
