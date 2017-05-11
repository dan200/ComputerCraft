/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import dan200.computercraft.ComputerCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class CreativeTabMain extends CreativeTabs
{
    public CreativeTabMain( int i )
    {
        super( i, "ComputerCraft" );
    }
    
    @Nonnull
    @Override
    public ItemStack getTabIconItem()
    {
        return new ItemStack( ComputerCraft.Blocks.computer );
    }
    
    @Nonnull
    @Override
    public String getTranslatedTabLabel()
    {
        return getTabLabel();
    }
}
