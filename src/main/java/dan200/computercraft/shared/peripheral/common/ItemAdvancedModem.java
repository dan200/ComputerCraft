/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.common;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemAdvancedModem extends ItemPeripheralBase
{
    public ItemAdvancedModem( Block block )
    {
        super( block );
        setUnlocalizedName( "computercraft:advanced_modem" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
    }

    public ItemStack create( PeripheralType type, String label, int quantity )
    {
        ItemStack stack;
        switch( type )
        {
            case AdvancedModem:
            {
                stack = new ItemStack( this, quantity, 0 );
                break;
            }
            default:
            {
                // Ignore types we can't handle
                return null;
            }
        }
        if( label != null )
        {
            stack.setStackDisplayName( label );
        }
        return stack;
    }

    @Override
    public void getSubItems( @Nonnull Item itemID, @Nullable CreativeTabs tabs, @Nonnull List<ItemStack> list )
    {
        list.add( PeripheralItemFactory.create( PeripheralType.AdvancedModem, null, 1 ) );
    }

    @Override
    public PeripheralType getPeripheralType( int damage )
    {
        return PeripheralType.AdvancedModem;
    }
}
