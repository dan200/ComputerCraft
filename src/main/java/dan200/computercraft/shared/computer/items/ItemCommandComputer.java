/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemCommandComputer extends ItemComputer
{
    public ItemCommandComputer( Block block )
    {
        super( block );
        setMaxStackSize( 64 );
        setHasSubtypes( true );
        setUnlocalizedName( "computercraft:command_computer" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
    }

    public ItemStack create( int id, String label, ComputerFamily family )
    {
        // Ignore types we can't handle
        if( family != ComputerFamily.Command )
        {
            return null;
        }

        // Build the stack
        ItemStack result = new ItemStack( this, 1, 0 );

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger( "computerID", id );
        result.setTagCompound( nbt );

        if( label != null )
        {
            result.setStackDisplayName( label );
        }

        return result;
    }

    @Override
    public void getSubItems( @Nonnull Item itemID, @Nullable CreativeTabs tabs, @Nonnull List<ItemStack> list )
    {
        list.add( ComputerItemFactory.create( -1, null, ComputerFamily.Command ) );
    }

    // IComputerItem implementation

    @Override
    public int getComputerID( ItemStack stack )
    {
        if( stack.hasTagCompound() && stack.getTagCompound().hasKey( "computerID" ) )
        {
            return stack.getTagCompound().getInteger( "computerID" );
        }
        return -1;
    }

    @Override
    public ComputerFamily getFamily( int damage )
    {
        return ComputerFamily.Command;
    }
}
