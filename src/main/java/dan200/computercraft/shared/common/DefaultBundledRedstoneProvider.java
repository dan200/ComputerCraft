/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.common;

import dan200.computercraft.api.redstone.IBundledRedstoneProvider;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class DefaultBundledRedstoneProvider implements IBundledRedstoneProvider
{
    public DefaultBundledRedstoneProvider()
    {
    }

    @Override
    public int getBundledRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        return getDefaultBundledRedstoneOutput( world, pos, side );
    }

    public static int getDefaultBundledRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        Block block = world.getBlockState( pos ).getBlock();
        if( block != null && block instanceof BlockGeneric )
        {
            BlockGeneric generic = (BlockGeneric)block;
            if( generic.getBundledRedstoneConnectivity( world, pos, side ) )
            {
                return generic.getBundledRedstoneOutput( world, pos, side );
            }
        }
        return -1;
    }
}
