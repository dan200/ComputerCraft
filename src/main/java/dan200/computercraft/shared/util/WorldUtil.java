/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class WorldUtil
{
    public static boolean isBlockInWorld( World world, BlockPos pos )
    {
        return pos.getY() >= 0 && pos.getY() < world.getHeight();
    }

    public static boolean isLiquidBlock( World world, BlockPos pos )
    {
        return isBlockInWorld( world, pos ) && world.getBlockState( pos ).getMaterial().isLiquid();
    }

    public static BlockPos moveCoords( BlockPos pos, EnumFacing dir )
    {
        return pos.offset( dir );
    }

    public static Pair<Entity, Vec3d> rayTraceEntities( World world, Vec3d vecStart, Vec3d vecDir, double distance )
    {
        Vec3d vecEnd = vecStart.addVector( vecDir.xCoord * distance, vecDir.yCoord * distance, vecDir.zCoord * distance );

        // Raycast for blocks
        RayTraceResult result = world.rayTraceBlocks( vecStart.addVector(0.0,0.0,0.0), vecEnd.addVector(0.0,0.0,0.0) );
        if( result != null && result.typeOfHit == RayTraceResult.Type.BLOCK )
        {
            distance = vecStart.distanceTo( result.hitVec );
            vecEnd = vecStart.addVector( vecDir.xCoord * distance, vecDir.yCoord * distance, vecDir.zCoord * distance );
        }

        // Check for entities
        float xStretch = (Math.abs(vecDir.xCoord) > 0.25f) ? 0.0f : 1.0f;
        float yStretch = (Math.abs(vecDir.yCoord) > 0.25f) ? 0.0f : 1.0f;
        float zStretch = (Math.abs(vecDir.zCoord) > 0.25f) ? 0.0f : 1.0f;
        AxisAlignedBB bigBox = new AxisAlignedBB(
            Math.min(vecStart.xCoord, vecEnd.xCoord) - 0.375f * xStretch,
            Math.min(vecStart.yCoord, vecEnd.yCoord) - 0.375f * yStretch,
            Math.min(vecStart.zCoord, vecEnd.zCoord) - 0.375f * zStretch,
            Math.max(vecStart.xCoord, vecEnd.xCoord) + 0.375f * xStretch,
            Math.max(vecStart.yCoord, vecEnd.yCoord) + 0.375f * yStretch,
            Math.max(vecStart.zCoord, vecEnd.zCoord) + 0.375f * zStretch
        );

        Entity closest = null;
        double closestDist = 99.0;
        List list = world.getEntitiesWithinAABBExcludingEntity( null, bigBox );
        for( int i=0; i<list.size(); i++ )
        {
            Entity entity = (net.minecraft.entity.Entity)list.get(i);
            if( entity.isDead || !entity.canBeCollidedWith() )
            {
                continue;
            }

            AxisAlignedBB littleBox = entity.getEntityBoundingBox();
            if( littleBox == null )
            {
                littleBox = entity.getCollisionBoundingBox();
                if( littleBox == null )
                {
                    continue;
                }
            }

            if( littleBox.isVecInside( vecStart ) )
            {
                closest = entity;
                closestDist = 0.0f;
                continue;
            }

            RayTraceResult littleBoxResult = littleBox.calculateIntercept( vecStart, vecEnd );
            if( littleBoxResult != null )
            {
                double dist = vecStart.distanceTo( littleBoxResult.hitVec );
                if( closest == null || dist <= closestDist )
                {
                    closest = entity;
                    closestDist = dist;
                }
            }
            else if( littleBox.intersectsWith( bigBox ) )
            {
                if( closest == null )
                {
                    closest = entity;
                    closestDist = distance;
                }
            }
        }
        if( closest != null && closestDist <= distance )
        {
            Vec3d closestPos = vecStart.addVector( vecDir.xCoord * closestDist, vecDir.yCoord * closestDist, vecDir.zCoord * closestDist );
            return Pair.of( closest, closestPos );
        }
        return null;
    }

    public static void dropItemStack( ItemStack stack, World world, BlockPos pos )
    {
        dropItemStack( stack, world, pos, null );
    }

    public static void dropItemStack( ItemStack stack, World world, BlockPos pos, EnumFacing direction )
    {
        double xDir;
        double yDir;
        double zDir;
        if( direction != null )
        {
            xDir = (double)direction.getFrontOffsetX();
            yDir = (double)direction.getFrontOffsetY();
            zDir = (double)direction.getFrontOffsetZ();
        }
        else
        {
            xDir = 0.0;
            yDir = 0.0;
            zDir = 0.0;
        }

        double xPos = pos.getX() + 0.5 + xDir * 0.4;
        double yPos = pos.getY() + 0.5 + yDir * 0.4;
        double zPos = pos.getZ() + 0.5 + zDir * 0.4;
        dropItemStack( stack, world, xPos, yPos, zPos, xDir, yDir, zDir );
    }

    public static void dropItemStack( ItemStack stack, World world, double xPos, double yPos, double zPos )
    {
        dropItemStack( stack, world, xPos, yPos, zPos, 0.0, 0.0, 0.0 );
    }

    public static void dropItemStack( ItemStack stack, World world, double xPos, double yPos, double zPos, double xDir, double yDir, double zDir )
    {
        EntityItem entityItem = new EntityItem( world, xPos, yPos, zPos, stack.copy() );
        entityItem.motionX = xDir * 0.7 + world.rand.nextFloat() * 0.2 - 0.1;
        entityItem.motionY = yDir * 0.7 + world.rand.nextFloat() * 0.2 - 0.1;
        entityItem.motionZ = zDir * 0.7 + world.rand.nextFloat() * 0.2 - 0.1;
        entityItem.setDefaultPickupDelay();
        world.spawnEntityInWorld( entityItem );
    }
}
