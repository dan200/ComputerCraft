/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.recipes;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.proxy.CCTurtleProxyCommon;
import dan200.computercraft.shared.turtle.items.ITurtleItem;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class TurtleUpgradeRecipe implements IRecipe
{
    public TurtleUpgradeRecipe()
    {
    }

    @Override
    public int getRecipeSize()
    {
        return 3;
    }
    
    @Override
    public ItemStack getRecipeOutput()
    {
        return TurtleItemFactory.create( -1, null, null, ComputerFamily.Normal, null, null, 0, null );
    }

    @Override
    public boolean matches( InventoryCrafting inventory, World world )
    {
        return (getCraftingResult( inventory ) != null);
    }

    @Override
    public ItemStack getCraftingResult( InventoryCrafting inventory )
    {
        // Scan the grid for a row containing a turtle and 1 or 2 items
        ItemStack leftItem = ItemStack.EMPTY;
        ItemStack turtle = ItemStack.EMPTY;
        ItemStack rightItem = ItemStack.EMPTY;

        for( int y=0; y<inventory.getHeight(); ++y )
        {
            if( turtle == ItemStack.EMPTY )
            {
                // Search this row for potential turtles
                boolean finishedRow = false;
                for( int x=0; x<inventory.getWidth(); ++x )
                {
                    ItemStack item = inventory.getStackInRowAndColumn(x, y);
                    if( !item.isEmpty() ) {
                        if( finishedRow ) {
                            return ItemStack.EMPTY;
                        }
                        
                        if( item.getItem() instanceof ITurtleItem ) {
                            // Item is a turtle
                            if( turtle == ItemStack.EMPTY ) {
                                turtle = item;
                            } else {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            // Item is not a turtle
                            if( turtle == ItemStack.EMPTY && leftItem == ItemStack.EMPTY ) {
                                leftItem = item;
                            } else if( turtle != ItemStack.EMPTY && rightItem == ItemStack.EMPTY ) {
                                rightItem = item;
                            } else {
                                return ItemStack.EMPTY;
                            }
                        }
                    } else {
                        // Item is empty
                        if( leftItem != ItemStack.EMPTY || turtle != ItemStack.EMPTY ) {
                            finishedRow = true;
                        }
                    }
                }
                
                // If we found anything, check we found a turtle too
                if( turtle == ItemStack.EMPTY && (leftItem != ItemStack.EMPTY || !rightItem.isEmpty()) )
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                // Turtle is already found, just check this row is empty
                for( int x=0; x<inventory.getWidth(); ++x )
                {
                    ItemStack item = inventory.getStackInRowAndColumn(x, y);
                    if( !item.isEmpty() ) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        
        // See if we found a turtle + one or more items
        if( turtle == ItemStack.EMPTY || (leftItem == ItemStack.EMPTY && rightItem == ItemStack.EMPTY))
        {
            return ItemStack.EMPTY;
        }

        // At this point we have a turtle + 1 or 2 items
        // Get the turtle we already have
        ITurtleItem itemTurtle = (ITurtleItem)turtle.getItem();
        ComputerFamily family = itemTurtle.getFamily( turtle );
        ITurtleUpgrade[] upgrades = {
            itemTurtle.getUpgrade( turtle, TurtleSide.Left ),
            itemTurtle.getUpgrade( turtle, TurtleSide.Right ),
        };

        // Get the upgrades for the new items
        ItemStack[] items = new ItemStack[]{ rightItem, leftItem };
        for( int i=0; i<2; ++i )
        {
            if( items[i] != null )
            {
                ITurtleUpgrade itemUpgrade = ComputerCraft.getTurtleUpgrade( items[ i ] );
                if( itemUpgrade == null )
                {
                    return ItemStack.EMPTY;
                }
                if( upgrades[i] != null )
                {
                    return ItemStack.EMPTY;
                }
                if( !CCTurtleProxyCommon.isUpgradeSuitableForFamily( family, itemUpgrade ) )
                {
                    return ItemStack.EMPTY;
                }
                upgrades[i] = itemUpgrade;
            }
        }

        // Construct the new stack
        int computerID = itemTurtle.getComputerID( turtle );
        String label = itemTurtle.getLabel( turtle );
        int fuelLevel = itemTurtle.getFuelLevel( turtle );
        Colour colour = itemTurtle.getColour( turtle );
        ResourceLocation overlay = itemTurtle.getOverlay( turtle );
        return TurtleItemFactory.create( computerID, label, colour, family, upgrades[0], upgrades[1], fuelLevel, overlay );
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
