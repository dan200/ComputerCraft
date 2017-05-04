/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import dan200.computercraft.ComputerCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabMain extends CreativeTabs
{
    public CreativeTabMain( int i )
    {
        super( i, "ComputerCraft" );
    }
    
    @Override
    public ItemStack getTabIconItem()
    {
        return new ItemStack(ComputerCraft.Blocks.computer);
    }
}
