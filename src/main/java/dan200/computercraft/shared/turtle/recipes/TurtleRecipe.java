/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.recipes;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class TurtleRecipe implements IRecipe
{
    private final Item[] m_recipe;
    private final ComputerFamily m_family;
    
    public TurtleRecipe( Item[] recipe, ComputerFamily family )
    {
        m_recipe = recipe;
        m_family = family;
    }

    @Override
    public int getRecipeSize()
    {
        return 9;
    }
    
    @Override
    public ItemStack getRecipeOutput()
    {
        return TurtleItemFactory.create( -1, null, null, m_family, null, null, 0, null );
    }

    @Override
    public boolean matches( InventoryCrafting _inventory, World world )
    {
        return (getCraftingResult( _inventory ) != null);
    }

    @Override
    public ItemStack getCraftingResult( InventoryCrafting inventory )
    {
        // See if we match the recipe, and extract the input computercraft ID
        int computerID = -1;
        String label = null;
        for( int y=0; y<3; ++y )
        {
            for( int x=0; x<3; ++x )
            {
                ItemStack item = inventory.getStackInRowAndColumn(x, y);
                if( item != null && item.getItem() == m_recipe[ x + y*3 ] )
                {
                    if( item.getItem() instanceof IComputerItem )
                    {
                        IComputerItem itemComputer = (IComputerItem)item.getItem();
                        if( m_family == ComputerFamily.Beginners || itemComputer.getFamily( item ) == m_family )
                        {
                            computerID = itemComputer.getComputerID( item );
                            label = itemComputer.getLabel( item );
                        }
                        else
                        {
                            return null;
                        }
                    }
                }
                else
                {
                    return null;
                }
            }
        }
        
        // Build a turtle with the same ID the computer had
        // Construct the new stack
        if( m_family != ComputerFamily.Beginners )
        {
            return TurtleItemFactory.create( computerID, label, null, m_family, null, null, 0, null );
        }
        else
        {
            return TurtleItemFactory.create( -1, label, null, m_family, null, null, 0, null );
        }
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
