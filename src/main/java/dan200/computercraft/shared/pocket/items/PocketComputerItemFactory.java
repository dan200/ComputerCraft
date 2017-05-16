/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import net.minecraft.item.ItemStack;

public class PocketComputerItemFactory
{
    public static ItemStack create( int id, String label, int colour, ComputerFamily family, IPocketUpgrade upgrade )
    {
        ItemPocketComputer computer = ComputerCraft.Items.pocketComputer;
        switch( family )
        {
            case Normal:
            case Advanced:
            {
                return computer.create( id, label, colour, family, upgrade );
            }
        }
        return null;
    }
}
