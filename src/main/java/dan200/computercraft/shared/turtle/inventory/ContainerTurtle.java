/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.inventory;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IContainerComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class ContainerTurtle extends Container
    implements IContainerComputer
{
    private static final int PROGRESS_ID_SELECTED_SLOT = 0;

    public final int m_playerInvStartY;
    public final int m_turtleInvStartX;

    protected ITurtleAccess m_turtle;
    private IComputer m_computer;
    private int m_selectedSlot;

    protected ContainerTurtle( IInventory playerInventory, ITurtleAccess turtle, int playerInvStartY, int turtleInvStartX )
    {
        m_playerInvStartY = playerInvStartY;
        m_turtleInvStartX = turtleInvStartX;

        m_turtle = turtle;
        if( !m_turtle.getWorld().isRemote )
        {
            m_selectedSlot = m_turtle.getSelectedSlot();
        }
        else
        {
            m_selectedSlot = 0;
        }

        // Turtle inventory
        for( int y = 0; y < 4; y++ )
        {
            for( int x = 0; x < 4; x++ )
            {
                addSlotToContainer( new Slot( m_turtle.getInventory(), x + y * 4, turtleInvStartX + 1 + x * 18, playerInvStartY + 1 + y * 18) );
            }
        }

        // Player inventory
        for( int y = 0; y < 3; y++)
        {
            for( int x = 0; x < 9; x++ )
            {
                addSlotToContainer( new Slot( playerInventory, x + y * 9 + 9, 8 + x * 18, playerInvStartY + 1 + y * 18 ) );
            }
        }

        // Player hotbar
        for( int x = 0; x < 9; x++ )
        {
            addSlotToContainer( new Slot( playerInventory, x, 8 + x * 18, playerInvStartY + 3 * 18 + 5  ) );
        }
    }

    public ContainerTurtle( IInventory playerInventory, ITurtleAccess turtle )
    {
        this( playerInventory, turtle, 134, 175 );
    }

    public ContainerTurtle( IInventory playerInventory, ITurtleAccess turtle, IComputer computer )
    {
        this( playerInventory, turtle );
        m_computer = computer;
    }

    public int getSelectedSlot()
    {
        return m_selectedSlot;
    }
    
    private void sendStateToPlayer( IContainerListener icrafting )
    {
        int selectedSlot = m_turtle.getSelectedSlot();
        icrafting.sendProgressBarUpdate( this, PROGRESS_ID_SELECTED_SLOT, selectedSlot );
    }
                        
    @Override
    public void addListener( IContainerListener crafting )
    {
        super.addListener( crafting );
        sendStateToPlayer( crafting );
    }
    
    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
        
        int selectedSlot = m_turtle.getSelectedSlot();
        for( int i=0; i<listeners.size(); ++i )
        {
            IContainerListener icrafting = (IContainerListener)listeners.get(i);
            if( m_selectedSlot != selectedSlot )
            {
                icrafting.sendProgressBarUpdate( this, PROGRESS_ID_SELECTED_SLOT, selectedSlot );
            }
        }
        m_selectedSlot = selectedSlot;
    }
    
    @Override
    public void updateProgressBar( int id, int value )
    {
        super.updateProgressBar( id, value);
        switch( id )
        {
            case PROGRESS_ID_SELECTED_SLOT:
            {
                m_selectedSlot = value;
                break;
            }
        }
    }
    
    @Override
    public boolean canInteractWith( EntityPlayer player )
    {
        TileTurtle turtle = ((TurtleBrain)m_turtle).getOwner();
        if( turtle != null )
        {
            return turtle.isUseableByPlayer( player );
        }
        return false;
    }

    protected ItemStack tryItemMerge( EntityPlayer player, int slotNum, int firstSlot, int lastSlot, boolean reverse )
    {
        Slot slot = (Slot)inventorySlots.get( slotNum );
        ItemStack originalStack = null;
        if( slot != null && slot.getHasStack() )
        {
            ItemStack clickedStack = slot.getStack();
            originalStack = clickedStack.copy();
            if( !mergeItemStack( clickedStack, firstSlot, lastSlot, reverse ) )
            {
                return null;
            }

            if( clickedStack.stackSize == 0 )
            {
                slot.putStack( null );
            }
            else
            {
                slot.onSlotChanged();
            }

            if( clickedStack.stackSize != originalStack.stackSize )
            {
                slot.onPickupFromSlot( player, clickedStack );
            }
            else
            {
                return null;
            }
        }
        return originalStack;
    }

    @Override
    public ItemStack transferStackInSlot( EntityPlayer player, int slotNum )
    {
        if( slotNum >= 0 && slotNum < 16 )
        {
            return tryItemMerge( player, slotNum, 16, 52, true );
        }
        else if( slotNum >= 16 )
        {
            return tryItemMerge( player, slotNum, 0, 16, false );
        }
        return null;
    }

    @Nullable
    @Override
    public IComputer getComputer()
    {
        return m_computer;
    }
}
