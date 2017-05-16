package dan200.computercraft.shared.common;

import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.ColourTracker;
import dan200.computercraft.shared.util.ColourUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ColourableRecipe implements IRecipe
{
    @Override
    public boolean matches( @Nonnull InventoryCrafting inv, @Nonnull World worldIn )
    {
        boolean hasColourable = false;
        boolean hasDye = false;
        for( int i = 0; i < inv.getSizeInventory(); i++ )
        {
            ItemStack stack = inv.getStackInSlot( i );
            if( stack == null ) continue;

            if( stack.getItem() instanceof IColouredItem )
            {
                if( hasColourable ) return false;
                hasColourable = true;
            }
            else if( ColourUtils.getStackColour( stack ) >= 0 )
            {
                hasDye = true;
            }
            else
            {
                return false;
            }
        }

        return hasColourable && hasDye;
    }

    @Nullable
    @Override
    public ItemStack getCraftingResult( @Nonnull InventoryCrafting inv )
    {
        ItemStack colourable = null;

        ColourTracker tracker = new ColourTracker();

        for( int i = 0; i < inv.getSizeInventory(); ++i )
        {
            ItemStack stack = inv.getStackInSlot( i );

            if( stack == null ) continue;

            if( stack.getItem() instanceof IColouredItem )
            {
                colourable = stack;
            }
            else
            {
                int index = ColourUtils.getStackColour( stack );
                if( index < 0 ) continue;

                Colour colour = Colour.values()[ index ];
                tracker.addColour( colour.getR(), colour.getG(), colour.getB() );
            }
        }

        if( colourable == null )
        {
            return null;
        }

        return ((IColouredItem) colourable.getItem()).setColour( colourable, tracker.getColour() );
    }

    @Override
    public int getRecipeSize()
    {
        return 2;
    }

    @Nullable
    @Override
    public ItemStack getRecipeOutput()
    {
        return null;
    }

    @Nonnull
    @Override
    public ItemStack[] getRemainingItems( @Nonnull InventoryCrafting inv )
    {
        ItemStack[] results = new ItemStack[ inv.getSizeInventory() ];
        for( int i = 0; i < results.length; ++i )
        {
            ItemStack stack = inv.getStackInSlot( i );
            results[ i ] = ForgeHooks.getContainerItem( stack );
        }
        return results;
    }
}
