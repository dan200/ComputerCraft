/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemDiskExpanded extends ItemDiskLegacy
{
    public ItemDiskExpanded()
    {
    }

    public static ItemStack createFromIDAndColour( int id, String label, int colour )
    {
        ItemStack stack = new ItemStack( ComputerCraft.Items.diskExpanded, 1, 0 );

        NBTTagCompound nbt = stack.getTagCompound();
        if( nbt == null )
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        nbt.setInteger( "color", colour );
        ComputerCraft.Items.diskExpanded.setDiskID( stack, id );
        ComputerCraft.Items.diskExpanded.setLabel( stack, label );
        return stack;
    }

    @Override
    public int getDiskID( ItemStack stack )
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if( nbt != null && nbt.hasKey( "diskID" ) )
        {
            return nbt.getInteger( "diskID" );
        }
        return -1;
    }

    @Override
    protected void setDiskID( ItemStack stack, int id )
    {
        if( id >= 0 )
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if( nbt == null )
            {
                nbt = new NBTTagCompound();
                stack.setTagCompound( nbt );
            }
            nbt.setInteger( "diskID", id );
        }
    }

    public int getColor( ItemStack stack )
    {
        NBTTagCompound nbt = stack.getTagCompound();
           return nbt != null && nbt.hasKey("color") ? nbt.getInteger("color") : Colour.values()[ Math.min(15, stack.getItemDamage()) ].getHex();
    }
}
