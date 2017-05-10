/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.common;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.computer.blocks.ComputerPeripheral;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class DefaultPeripheralProvider implements IPeripheralProvider
{
    public DefaultPeripheralProvider()
    {
    }

    @Override
    public IPeripheral getPeripheral( @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null )
        {
            // Handle our peripherals
            if( tile instanceof IPeripheralTile )
            {
                IPeripheralTile peripheralTile = (IPeripheralTile)tile;
                return peripheralTile.getPeripheral( side );
            }

            // Handle our computers
            if( tile instanceof TileComputerBase )
            {
                TileComputerBase computerTile = (TileComputerBase)tile;
                if( tile instanceof TileTurtle )
                {
                    if( !((TileTurtle)tile).hasMoved() )
                    {
                        return new ComputerPeripheral( "turtle", computerTile.createServerComputer() );
                    }
                }
                else
                {
                    return new ComputerPeripheral( "computer", computerTile.createServerComputer() );
                }
            }
        }
        return null;
    }
}
