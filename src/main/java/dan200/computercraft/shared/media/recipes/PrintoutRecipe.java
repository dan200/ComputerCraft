/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.recipes;

import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PrintoutRecipe implements IRecipe
{
    public PrintoutRecipe( )
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
        return ItemPrintout.createMultipleFromTitleAndText( null, null, null );
    }

    @Override
    public boolean matches( @Nonnull InventoryCrafting _inventory, @Nonnull World world )
    {
        return (getCraftingResult( _inventory ) != null);
    }

    @Override
    public ItemStack getCraftingResult( @Nonnull InventoryCrafting inventory )
    {
        // See if we match the recipe, and extract the input disk ID and dye colour
        int numPages = 0;
        int numPrintouts = 0;
        ItemStack[] printouts = null;
        boolean stringFound = false;
        boolean leatherFound = false;
        boolean printoutFound = false;
        for( int y=0; y<inventory.getHeight(); ++y )
        {
            for( int x=0; x<inventory.getWidth(); ++x )
            {
                ItemStack stack = inventory.getStackInRowAndColumn(x, y);
                if( stack != null )
                {
                    Item item = stack.getItem();
                    if( item instanceof ItemPrintout && ItemPrintout.getType( stack ) != ItemPrintout.Type.Book )
                    {
                        if( printouts == null )
                        {
                            printouts = new ItemStack[9];
                        }
                        printouts[ numPrintouts ] = stack;
                        numPages = numPages + ItemPrintout.getPageCount( stack );
                        numPrintouts++;
                        printoutFound = true;
                    }
                    else if( item == Items.PAPER )
                    {
                        if( printouts == null )
                        {
                            printouts = new ItemStack[9];
                        }
                        printouts[ numPrintouts ] = stack;
                        numPages++;
                        numPrintouts++;
                    }
                    else if( item == Items.STRING && !stringFound )
                    {
                        stringFound = true;
                    }
                    else if( item == Items.LEATHER && !leatherFound )
                    {
                        leatherFound = true;
                    }
                    else
                    {
                        return null;
                    }
                }
            }
        }
        
        // Build some pages with what was passed in
        if( numPages <= ItemPrintout.MAX_PAGES && stringFound && printoutFound && numPrintouts >= (leatherFound ? 1 : 2) )
        {
            String[] text = new String[ numPages * ItemPrintout.LINES_PER_PAGE ];
            String[] colours = new String[ numPages * ItemPrintout.LINES_PER_PAGE ];
            int line = 0;
            
            for( int printout=0; printout<numPrintouts; ++printout )
            {
                ItemStack stack = printouts[printout];
                if( stack.getItem() instanceof ItemPrintout )
                {
                    // Add a printout
                    String[] pageText = ItemPrintout.getText( printouts[printout] );
                    String[] pageColours = ItemPrintout.getColours( printouts[printout] );
                    for( int pageLine=0; pageLine<pageText.length; ++pageLine )
                    {
                        text[ line ] = pageText[ pageLine ];
                        colours[ line ] = pageColours[ pageLine ];
                        line++;
                    }
                }
                else
                {
                    // Add a blank page
                    for( int pageLine=0; pageLine<ItemPrintout.LINES_PER_PAGE; ++pageLine )
                    {
                        text[ line ] = "";
                        colours[ line ] = "";
                        line++;
                    }
                }
            }
            
            String title = null;
            if( printouts[0].getItem() instanceof ItemPrintout )
            {
                title = ItemPrintout.getTitle( printouts[0] );
            }
            
            if( leatherFound )
            {
                return ItemPrintout.createBookFromTitleAndText( title, text, colours );
            }
            else
            {
                return ItemPrintout.createMultipleFromTitleAndText( title, text, colours );
            }
        }

        return null;
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
