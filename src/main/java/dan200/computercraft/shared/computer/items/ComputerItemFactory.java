/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.IComputerTile;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.IComputer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ComputerItemFactory
{
    public static ItemStack create( IComputerTile computerTile )
    {
        IComputer computer = computerTile.getComputer();
        if( computer != null )
        {
            String label = computer.getLabel();
            int id = (label != null) ? computer.getID() : -1;
            return create( id, label, computerTile.getFamily() );
        }
        else
        {
            return create( -1, null, computerTile.getFamily() );
        }
    }

    public static ItemStack create( int id, String label, ComputerFamily family )
    {
        ItemComputer computer = ((ItemComputer)Item.getItemFromBlock( ComputerCraft.Blocks.computer ));
        ItemCommandComputer commandComputer = ((ItemCommandComputer)Item.getItemFromBlock( ComputerCraft.Blocks.commandComputer ));
        switch( family )
        {
            case Normal:
            case Advanced:
            {
                return computer.create( id, label, family );
            }
            case Command:
            {
                return commandComputer.create( id, label, family );
            }
        }
        return null;
    }
}
