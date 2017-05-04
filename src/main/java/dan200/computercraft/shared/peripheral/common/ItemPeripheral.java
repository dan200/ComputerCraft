/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.common;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;

public class ItemPeripheral extends ItemPeripheralBase
{
    public ItemPeripheral( Block block )
    {
        super( block );
        setUnlocalizedName( "computercraft:peripheral" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
    }

    public ItemStack create( PeripheralType type, String label, int quantity )
    {
        ItemStack stack;
        switch( type )
        {
            case DiskDrive:
            {
                stack = new ItemStack( this, quantity, 0 );
                break;
            }
            case WirelessModem:
            {
                stack = new ItemStack( this, quantity, 1 );
                break;
            }
            case Monitor:
            {
                stack = new ItemStack( this, quantity, 2 );
                break;
            }
            case Printer:
            {
                stack = new ItemStack( this, quantity, 3 );
                break;
            }
            case AdvancedMonitor:
            {
                stack = new ItemStack( this, quantity, 4 );
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
    public void getSubItems( Item itemID, CreativeTabs tabs, NonNullList<ItemStack> list )
    {
        list.add( PeripheralItemFactory.create( PeripheralType.DiskDrive, null, 1 ) );
        list.add( PeripheralItemFactory.create( PeripheralType.Printer, null, 1 ) );
        list.add( PeripheralItemFactory.create( PeripheralType.Monitor, null, 1 ) );
        list.add( PeripheralItemFactory.create( PeripheralType.AdvancedMonitor, null, 1 ) );
        list.add( PeripheralItemFactory.create( PeripheralType.WirelessModem, null, 1 ) );
    }

    @Override
    public PeripheralType getPeripheralType( int damage )
    {
        switch( damage )
        {
            case 0:
            default:
            {
                return PeripheralType.DiskDrive;
            }
            case 1:
            {
                return PeripheralType.WirelessModem;
            }
            case 2:
            {
                return PeripheralType.Monitor;
            }
            case 3:
            {
                return PeripheralType.Printer;
            }
            case 4:
            {
                return PeripheralType.AdvancedMonitor;
            }
        }
    }
}
