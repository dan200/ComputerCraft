/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;

public class ImpostorShapelessRecipe extends ShapelessRecipes
{
    public ImpostorShapelessRecipe( ItemStack result, ItemStack[] ingredients )
    {
        super( result, new ArrayList<ItemStack>(Arrays.asList( ingredients )));
    }

    @Override
    public boolean matches( InventoryCrafting inv, World world )
    {
        return false;
    }

    @Override
    public ItemStack getCraftingResult( InventoryCrafting _inventory )
    {
        return null;
    }
}
