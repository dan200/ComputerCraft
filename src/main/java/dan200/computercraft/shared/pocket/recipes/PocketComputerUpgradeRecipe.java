/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.recipes;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.IPeripheralItem;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class PocketComputerUpgradeRecipe implements IRecipe
{
    public PocketComputerUpgradeRecipe()
    {
    }

    @Override
    public int getRecipeSize()
    {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return PocketComputerItemFactory.create( -1, null, ComputerFamily.Normal, true );
    }

    @Override
    public boolean matches( InventoryCrafting inventory, World world )
    {
        return (getCraftingResult( inventory ) != null);
    }

    @Override
    public ItemStack getCraftingResult( InventoryCrafting inventory )
    {
        // Scan the grid for a pocket computer
        ItemStack computer = null;
        int computerX = -1;
        int computerY = -1;
        for( int y=0; y<inventory.getHeight(); ++y )
        {
            for( int x=0; x<inventory.getWidth(); ++x )
            {
                ItemStack item = inventory.getStackInRowAndColumn( x, y );
                if( item != null && item.getItem() instanceof ItemPocketComputer )
                {
                    computer = item;
                    computerX = x;
                    computerY = y;
                    break;
                }
            }
            if( computer != null )
            {
                break;
            }
        }

        if( computer == null )
        {
            return null;
        }

        // Check for upgrades around the item
        ItemStack upgrade = null;
        for( int y=0; y<inventory.getHeight(); ++y )
        {
            for( int x=0; x<inventory.getWidth(); ++x )
            {
                ItemStack item = inventory.getStackInRowAndColumn( x, y );
                if( x == computerX && y == computerY )
                {
                    continue;
                }
                else if( x == computerX && y == computerY - 1 )
                {
                    if( item != null && item.getItem() instanceof IPeripheralItem &&
                        ((IPeripheralItem)item.getItem()).getPeripheralType( item ) == PeripheralType.WirelessModem )
                    {
                        upgrade = item;
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    if( item != null )
                    {
                        return null;
                    }
                }
            }
        }

        if( upgrade == null )
        {
            return null;
        }

        // At this point we have a computer + 1 upgrade
        ItemPocketComputer itemComputer = (ItemPocketComputer)computer.getItem();
        if( itemComputer.getHasModem( computer ) )
        {
            return null;
        }

        // Construct the new stack
        ComputerFamily family = itemComputer.getFamily( computer );
        int computerID = itemComputer.getComputerID( computer );
        String label = itemComputer.getLabel( computer );
        return PocketComputerItemFactory.create( computerID, label, family, true );
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems( InventoryCrafting inventoryCrafting )
    {
        NonNullList<ItemStack> list = NonNullList.create();
        for (int i = 0; i < inventoryCrafting.getSizeInventory(); ++i)
        {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            list.add( ForgeHooks.getContainerItem(stack) );
        }
        return list;
    }
}
