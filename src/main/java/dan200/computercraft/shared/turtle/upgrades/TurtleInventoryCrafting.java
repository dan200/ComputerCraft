/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.upgrades;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class TurtleInventoryCrafting extends InventoryCrafting
{
    private ITurtleAccess m_turtle;
    private int m_xStart;
    private int m_yStart;

    public TurtleInventoryCrafting( ITurtleAccess turtle )
    {
        super( null, 0, 0 );
        m_turtle = turtle;
        m_xStart = 0;
        m_yStart = 0;
    }

    private ItemStack tryCrafting( int xStart, int yStart )
    {
        m_xStart = xStart;
        m_yStart = yStart;

        // Check the non-relevant parts of the inventory are empty
        for( int x=0; x<TileTurtle.INVENTORY_WIDTH; ++x )
        {
            for( int y=0; y<TileTurtle.INVENTORY_HEIGHT; ++y )
            {
                if( x < m_xStart || x >= m_xStart + 3 ||
                    y < m_yStart || y >= m_yStart + 3 )
                {
                    if( m_turtle.getInventory().getStackInSlot( x + y * TileTurtle.INVENTORY_WIDTH ) != null )
                    {
                        return null;
                    }
                }
            }
        }

        // Check the actual crafting
        return CraftingManager.getInstance().findMatchingRecipe( this, m_turtle.getWorld() );
    }

    public ArrayList<ItemStack> doCrafting( World world, int maxCount )
    {
        if( world.isRemote || !(world instanceof WorldServer) )
        {
            return null;
        }

        // Find out what we can craft
        ItemStack result = tryCrafting( 0, 0 );
        if( result == null )
        {
            result = tryCrafting( 0, 1 );
        }
        if( result == null )
        {
            result = tryCrafting( 1, 0 );
        }
        if( result == null )
        {
            result = tryCrafting( 1, 1 );
        }

        // Craft it
        if( result != null )
        {
            // Special case: craft(0) just returns an empty list if crafting was possible
            ArrayList<ItemStack> results = new ArrayList<ItemStack>();
            if( maxCount == 0 )
            {
                return results;
            }
        
            // Find out how many we can craft
            int numToCraft = 1;
            int size = getSizeInventory();
            if( maxCount > 1 )
            {
                int minStackSize = 0;
                for( int n=0; n<size; ++n )
                {
                    ItemStack stack = getStackInSlot( n );
                    if( stack != null && (minStackSize == 0 || minStackSize > stack.stackSize) )
                    {
                        minStackSize = stack.stackSize;
                    }
                }
                
                if( minStackSize > 1 )
                {            
                    numToCraft = Math.min( minStackSize, result.getMaxStackSize() / result.stackSize );
                    numToCraft = Math.min( numToCraft, maxCount );
                    result.stackSize = result.stackSize * numToCraft;
                }
            }

            // Do post-pickup stuff
            TurtlePlayer turtlePlayer = new TurtlePlayer( (WorldServer)world );
            result.onCrafting( world, turtlePlayer, numToCraft );
            results.add( result );

            // Consume resources from the inventory
            ItemStack[] remainingItems = CraftingManager.getInstance().getRemainingItems( this, world );
            for( int n=0; n<size; ++n )
            {
                ItemStack stack = getStackInSlot( n );
                if( stack != null )
                {
                    decrStackSize( n, numToCraft );

                    ItemStack replacement = remainingItems[n];
                    if( replacement != null )
                    {
                        if( !(replacement.isItemStackDamageable() && replacement.getItemDamage() >= replacement.getMaxDamage()) )
                        {
                            replacement.stackSize = Math.min( numToCraft, replacement.getMaxStackSize() );
                            if( getStackInSlot( n ) == null )
                            {
                                setInventorySlotContents( n, replacement );
                            }
                            else
                            {
                                results.add( replacement );
                            }
                        }
                    }
                }
            }
            return results;
        }
        
        return null;
    }

    @Override
    public ItemStack getStackInRowAndColumn(int x, int y)
    {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight() )
        {
            return getStackInSlot( x + y * getWidth() );
        }
        return null;
    }

    @Override
    public int getWidth()
    {
        return 3;
    }

    @Override
    public int getHeight()
    {
        return 3;
    }

    private int modifyIndex( int index )
    {
        int x = m_xStart + (index % getWidth());
        int y = m_yStart + (index / getHeight());
        if( x >= 0 && x < TileTurtle.INVENTORY_WIDTH &&
            y >= 0 && y < TileTurtle.INVENTORY_HEIGHT )
        {
            return x + y * TileTurtle.INVENTORY_WIDTH;
        }
        return -1;
    }

    // IInventory implementation
    
    @Override
    public int getSizeInventory()
    {
        return getWidth() * getHeight();
    }

    @Override
    public ItemStack getStackInSlot( int i )
    {
        i = modifyIndex( i );
        return m_turtle.getInventory().getStackInSlot( i );
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentString( "" );
    }

    @Override
    public ItemStack removeStackFromSlot( int i )
    {
        i = modifyIndex( i );
        return m_turtle.getInventory().removeStackFromSlot( i );
    }

    @Override
    public ItemStack decrStackSize( int i, int size )
    {
        i = modifyIndex( i );
        return m_turtle.getInventory().decrStackSize( i, size );
    }

    @Override
    public void setInventorySlotContents( int i, ItemStack stack )
    {
        i = modifyIndex( i );
        m_turtle.getInventory().setInventorySlotContents( i, stack );
    }

    @Override
    public int getInventoryStackLimit()
    {
        return m_turtle.getInventory().getInventoryStackLimit();
    }

    @Override
    public void markDirty()
    {
        m_turtle.getInventory().markDirty();
    }

    @Override
    public boolean isUseableByPlayer( EntityPlayer player )
    {
        return true;
    }

    @Override
    public void openInventory( EntityPlayer player )
    {
    }

    @Override
    public void closeInventory( EntityPlayer player )
    {
    }

    @Override
    public boolean isItemValidForSlot( int i, ItemStack stack )
    {
        i = modifyIndex( i );
        return m_turtle.getInventory().isItemValidForSlot( i, stack );
    }

    @Override
    public int getField( int id )
    {
        return 0;
    }

    @Override
    public void setField( int id, int value )
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
        for( int i=0; i<getSizeInventory(); ++i )
        {
            int j = modifyIndex( i );
            m_turtle.getInventory().setInventorySlotContents( j, null );
        }
    }
}
