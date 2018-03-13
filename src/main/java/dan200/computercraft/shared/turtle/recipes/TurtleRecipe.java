/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.recipes;

import com.google.gson.JsonObject;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.RecipeUtil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;

public class TurtleRecipe extends ShapedRecipes
{
    private final NonNullList<Ingredient> m_recipe;
    private final ComputerFamily m_family;

    public TurtleRecipe( String group, int width, int height, NonNullList<Ingredient> recipe, ComputerFamily family )
    {
        super( group, width, height, recipe, TurtleItemFactory.create( -1, null, -1, family, null, null, 0, null ) );
        m_recipe = recipe;
        m_family = family;
    }

    @Override
    public boolean matches( @Nonnull InventoryCrafting _inventory, @Nonnull World world )
    {
        return !getCraftingResult( _inventory ).isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult( @Nonnull InventoryCrafting inventory )
    {
        // See if we match the recipe, and extract the input computercraft ID
        int computerID = -1;
        String label = null;
        for( int y = 0; y < 3; ++y )
        {
            for( int x = 0; x < 3; ++x )
            {
                ItemStack item = inventory.getStackInRowAndColumn( x, y );
                Ingredient target = m_recipe.get( x + y * 3 );

                if( item.getItem() instanceof IComputerItem )
                {
                    IComputerItem itemComputer = (IComputerItem) item.getItem();
                    if( itemComputer.getFamily( item ) != m_family ) return ItemStack.EMPTY;

                    computerID = itemComputer.getComputerID( item );
                    label = itemComputer.getLabel( item );
                }
                else if( !target.apply( item ) )
                {
                    return ItemStack.EMPTY;
                }
            }
        }

        // Build a turtle with the same ID the computer had
        // Construct the new stack
        if( m_family != ComputerFamily.Beginners )
        {
            return TurtleItemFactory.create( computerID, label, -1, m_family, null, null, 0, null );
        }
        else
        {
            return TurtleItemFactory.create( -1, label, -1, m_family, null, null, 0, null );
        }
    }

    public static class Factory implements IRecipeFactory
    {
        @Override
        public IRecipe parse( JsonContext context, JsonObject json )
        {
            String group = JsonUtils.getString( json, "group", "" );
            ComputerFamily family = RecipeUtil.getFamily( json, "family" );
            CraftingHelper.ShapedPrimer primer = RecipeUtil.getPrimer( context, json );
            return new TurtleRecipe( group, primer.width, primer.height, primer.input, family );
        }
    }
}
