/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ImpostorRecipe extends ShapedRecipes
{
    public ImpostorRecipe( int width, int height, ItemStack[] ingredients, ItemStack result )
    {
        super( width, height, ingredients, result );
    }

    @Override
    public boolean matches( @Nonnull InventoryCrafting inv, World world )
    {
        return false;
    }
    
    @Override
    public ItemStack getCraftingResult( @Nonnull InventoryCrafting _inventory )
    {
        return null;
    }
}
