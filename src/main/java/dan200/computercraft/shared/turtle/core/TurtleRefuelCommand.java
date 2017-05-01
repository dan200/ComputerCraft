/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class TurtleRefuelCommand implements ITurtleCommand
{
    private final int m_limit;

    public TurtleRefuelCommand( int limit )
    {
        m_limit = limit;
    }

    @Override
    public TurtleCommandResult execute( ITurtleAccess turtle )
    {
        if( m_limit == 0 )
        {
            // If limit is zero, just check the item is combustible
            ItemStack dummyStack = turtle.getInventory().getStackInSlot( turtle.getSelectedSlot() );
            if( dummyStack != null )
            {
                return refuel( turtle, dummyStack, true );
            }
        }
        else
        {
            // Otherwise, refuel for real
            // Remove items from inventory
            ItemStack stack = InventoryUtil.takeItems( m_limit, turtle.getInventory(), turtle.getSelectedSlot(), 1, turtle.getSelectedSlot() );
            if( stack != null )
            {
                TurtleCommandResult result = refuel( turtle, stack, false );
                if( !result.isSuccess() )
                {
                    // If the items weren't burnt, put them back
                    InventoryUtil.storeItems( stack, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot() );
                }
                return result;
            }
        }
        return TurtleCommandResult.failure( "No items to combust" );
    }

    private int getFuelPerItem( ItemStack stack )
    {
        return (TileEntityFurnace.getItemBurnTime( stack ) * 5) / 100;
    }

    private TurtleCommandResult refuel( ITurtleAccess turtle, ItemStack stack, boolean testOnly )
    {
        // Check if item is fuel
        int fuelPerItem = getFuelPerItem( stack );
        if( fuelPerItem <= 0 )
        {
            return TurtleCommandResult.failure( "Items not combustible" );
        }

        if( !testOnly )
        {
            // Determine fuel to give and replacement item to leave behind
            int fuelToGive = fuelPerItem * stack.stackSize;
            ItemStack replacementStack = stack.getItem().getContainerItem( stack );

            // Update fuel level
            turtle.addFuel( fuelToGive );

            // Store the replacement item in the inventory
            if( replacementStack != null )
            {
                InventoryUtil.storeItems( replacementStack, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot() );
            }

            // Animate
            turtle.playAnimation( TurtleAnimation.Wait );
        }

        return TurtleCommandResult.success();
    }
}
