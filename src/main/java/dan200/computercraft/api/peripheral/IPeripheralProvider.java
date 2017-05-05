/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.peripheral;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * This interface is used to create peripheral implementations for blocks.
 *
 * @see dan200.computercraft.api.ComputerCraftAPI#registerPeripheralProvider(IPeripheralProvider)
 */
public interface IPeripheralProvider
{
    /**
     * Produce an peripheral implementation from a block location.
     *
     * @param world The world the block is in.
     * @param pos   The position the block is at.
     * @param side  The side to get the peripheral from.
     * @return A peripheral, or {@code null} if there is not a peripheral here you'd like to handle.
     * @see dan200.computercraft.api.ComputerCraftAPI#registerPeripheralProvider(IPeripheralProvider)
     */
    public IPeripheral getPeripheral( World world, BlockPos pos, EnumFacing side );
}
