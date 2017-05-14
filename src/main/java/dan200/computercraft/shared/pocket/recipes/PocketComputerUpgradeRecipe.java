/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.recipes;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

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
        return PocketComputerItemFactory.create( -1, null, -1, ComputerFamily.Normal, null );
    }

    @Override
    public boolean matches( @Nonnull InventoryCrafting inventory, @Nonnull World world )
    {
        return (getCraftingResult( inventory ) != null);
    }

    @Override
    public ItemStack getCraftingResult( @Nonnull InventoryCrafting inventory )
    {
        // Scan the grid for a pocket computer
        ItemStack computer = null;
        int computerX = -1;
        int computerY = -1;
        for (int y = 0; y < inventory.getHeight(); ++y)
        {
            for (int x = 0; x < inventory.getWidth(); ++x)
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

        ItemPocketComputer itemComputer = (ItemPocketComputer)computer.getItem();
        if( itemComputer.getUpgrade( computer ) != null )
        {
            return null;
        }

        // Check for upgrades around the item
        IPocketUpgrade upgrade = null;
        for (int y = 0; y < inventory.getHeight(); ++y)
        {
            for (int x = 0; x < inventory.getWidth(); ++x)
            {
                ItemStack item = inventory.getStackInRowAndColumn( x, y );
                if( x == computerX && y == computerY )
                {
                    continue;
                }
                else if( x == computerX && y == computerY - 1 )
                {
                    upgrade = ComputerCraft.getPocketUpgrade( item );
                    if( upgrade == null ) return null;
                }
                else if( item != null )
                {
                    return null;
                }
            }
        }

        if( upgrade == null )
        {
            return null;
        }

        // Construct the new stack
        ComputerFamily family = itemComputer.getFamily( computer );
        int computerID = itemComputer.getComputerID( computer );
        String label = itemComputer.getLabel( computer );
        int colour = itemComputer.getColour( computer );
        return PocketComputerItemFactory.create( computerID, label, colour, family, upgrade );
    }

    @Nonnull
    @Override
    public ItemStack[] getRemainingItems( @Nonnull InventoryCrafting inventoryCrafting )
    {
        ItemStack[] results = new ItemStack[ inventoryCrafting.getSizeInventory() ];
        for (int i = 0; i < results.length; ++i)
        {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            results[i] = net.minecraftforge.common.ForgeHooks.getContainerItem(stack);
        }
        return results;
    }
}
