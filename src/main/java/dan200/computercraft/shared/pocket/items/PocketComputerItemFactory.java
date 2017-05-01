/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import net.minecraft.item.ItemStack;

public class PocketComputerItemFactory
{
    public static ItemStack create( int id, String label, ComputerFamily family, boolean modem )
    {
        ItemPocketComputer computer = ComputerCraft.Items.pocketComputer;
        switch( family )
        {
            case Normal:
            case Advanced:
            {
                return computer.create( id, label, family, modem );
            }
        }
        return null;
    }
}
