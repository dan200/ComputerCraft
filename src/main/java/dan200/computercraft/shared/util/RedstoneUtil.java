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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneUtil
{
    public static int getRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        int power = 0;
        IBlockState state = world.getBlockState( pos );
        Block block =   state.getBlock();
        if( block != Blocks.AIR )
        {
            if( block == Blocks.REDSTONE_WIRE )
            {
                if( side != EnumFacing.UP )
                {
                    power = state.getValue( BlockRedstoneWire.POWER );
                }
                else
                {
                    power = 0;
                }
            }
            else if( state.canProvidePower( ) )
            {
                power = state.getWeakPower( world, pos, side.getOpposite() );
            }
            if( block.isNormalCube( state, world, pos ) )
            {
                for( EnumFacing testSide : EnumFacing.VALUES )
                {
                    if( testSide != side )
                    {
                        BlockPos testPos = pos.offset( testSide );
                        IBlockState neighbour = world.getBlockState( testPos );
                        if( neighbour.canProvidePower( ) )
                        {
                            power = Math.max( power, neighbour.getStrongPower( world, testPos, testSide.getOpposite() ) );
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
        IBlockState block = world.getBlockState( pos );
        BlockPos neighbourPos = pos.offset( side );
        IBlockState neighbour = world.getBlockState( neighbourPos );
        if( neighbour.getBlock() != Blocks.AIR )
        {
            world.notifyBlockOfStateChange( neighbourPos, block.getBlock() );
            if( neighbour.getBlock().isNormalCube( neighbour, world, neighbourPos ) )
            {
                world.notifyNeighborsOfStateExcept( neighbourPos, neighbour.getBlock(), side.getOpposite() );
            }
        }
    }
}
