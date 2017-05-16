/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.recipes;

import dan200.computercraft.shared.media.items.ItemDiskLegacy;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.ColourTracker;
import dan200.computercraft.shared.util.ColourUtils;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class DiskRecipe implements IRecipe
{
    @Override
    public boolean matches( @Nonnull InventoryCrafting inv, @Nonnull World world )
    {
        boolean paperFound = false;
        boolean redstoneFound = false;

        for( int i = 0; i < inv.getSizeInventory(); ++i )
        {
            ItemStack stack = inv.getStackInSlot( i );

            if( stack != null )
            {
                if( stack.getItem() == Items.PAPER )
                {
                    if( paperFound ) return false;
                    paperFound = true;
                }
                else if( stack.getItem() == Items.REDSTONE )
                {
                    if( redstoneFound ) return false;
                    redstoneFound = true;
                }
                else if( ColourUtils.getStackColour( stack ) < 0 )
                {
                    return false;
                }
            }
        }

        return redstoneFound && paperFound;
    }

    @Override
    public ItemStack getCraftingResult( @Nonnull InventoryCrafting inv )
    {
        ColourTracker tracker = new ColourTracker();

        for( int i = 0; i < inv.getSizeInventory(); ++i )
        {
            ItemStack stack = inv.getStackInSlot( i );

            if( stack == null ) continue;

            if( stack.getItem() != Items.PAPER && stack.getItem() != Items.REDSTONE )
            {
                int index = ColourUtils.getStackColour( stack );
                if( index < 0 ) continue;

                Colour colour = Colour.values()[ index ];
                tracker.addColour( colour.getR(), colour.getG(), colour.getB() );
            }
        }

        return ItemDiskLegacy.createFromIDAndColour( -1, null, tracker.hasColour() ? tracker.getColour() : Colour.Blue.getHex() );
    }

    @Override
    public int getRecipeSize()
    {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return ItemDiskLegacy.createFromIDAndColour( -1, null, Colour.Blue.getHex() );
    }

    @Nonnull
    @Override
    public ItemStack[] getRemainingItems( @Nonnull InventoryCrafting inv )
    {
        ItemStack[] results = new ItemStack[ inv.getSizeInventory() ];
        for( int i = 0; i < results.length; ++i )
        {
            ItemStack stack = inv.getStackInSlot( i );
            results[ i ] = net.minecraftforge.common.ForgeHooks.getContainerItem( stack );
        }
        return results;
    }
}
