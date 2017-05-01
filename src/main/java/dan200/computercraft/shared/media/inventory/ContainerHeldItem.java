/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.inventory;

import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class ContainerHeldItem extends Container
{
    private final ItemStack m_stack;
    private final int m_slot;

    public ContainerHeldItem( InventoryPlayer player )
    {
        m_slot = player.currentItem;
        m_stack = InventoryUtil.copyItem( player.getStackInSlot( m_slot ) );
    }

    public ItemStack getStack()
    {
        return m_stack;
    }

    @Override
    public boolean canInteractWith( EntityPlayer player )
    {
        if( player != null && player.isEntityAlive() )
        {
            ItemStack stack = player.inventory.getStackInSlot( m_slot );
            if( (stack == m_stack) || (stack != null && m_stack != null && stack.getItem() == m_stack.getItem()) )
            {
                return true;
            }
        }
        return false;
    }
}
