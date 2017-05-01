/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import dan200.computercraft.ComputerCraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RedstoneUtil
{
    private static Block getBlock( IBlockAccess world, BlockPos pos )
    {
        if( pos.getY() >= 0 )
        {
            return world.getBlockState( pos ).getBlock();
        }
        return null;
    }

    public static int getRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        int power = 0;
        Block block = getBlock( world, pos );
        if( block != null && block != Blocks.AIR )
        {
            IBlockState state = world.getBlockState( pos );
            if( block == Blocks.REDSTONE_WIRE )
            {
                if( side != EnumFacing.UP )
                {
                    power = ((Integer)state.getValue( BlockRedstoneWire.POWER )).intValue();
                }
                else
                {
                    power = 0;
                }
            }
            else if( block.canProvidePower( state ) )
            {
                power = block.getWeakPower( state, world, pos, side.getOpposite() );
            }
            if( block.isNormalCube( state, world, pos ) )
            {
                for( EnumFacing testSide : EnumFacing.VALUES )
                {
                    if( testSide != side )
                    {
                        BlockPos testPos = pos.offset( testSide );
                        Block neighbour = getBlock( world, testPos );
                        if( neighbour != null && neighbour.canProvidePower( state ) )
                        {
                            power = Math.max( power, neighbour.getStrongPower( state, world, testPos, testSide.getOpposite() ) );
                        }
                    }
                }
            }
        }
        return power;
    }

    public static int getBundledRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        int signal = ComputerCraft.getBundledRedstoneOutput( world, pos, side );
        if( signal >= 0 )
        {
            return signal;
        }
        return 0;
    }

    public static void propogateRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        // Propogate ordinary output
        Block block = getBlock( world, pos );
        BlockPos neighbourPos = pos.offset( side );
        Block neighbour = getBlock( world, neighbourPos );
        if( neighbour != null && neighbour != Blocks.AIR )
        {
            world.notifyBlockOfStateChange( neighbourPos, block );
            if( neighbour.isNormalCube( world.getBlockState( neighbourPos ), world, neighbourPos ) )
            {
                world.notifyNeighborsOfStateExcept( neighbourPos, neighbour, side.getOpposite() );
            }
        }
    }
}
