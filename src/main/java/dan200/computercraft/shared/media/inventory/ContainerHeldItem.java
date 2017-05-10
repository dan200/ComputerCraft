/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.inventory;

import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import javax.annotation.Nonnull;

public class ContainerHeldItem extends Container
{
    private final ItemStack m_stack;
    private final EnumHand m_hand;

    public ContainerHeldItem( EntityPlayer player, EnumHand hand )
    {
        m_hand = hand;
        m_stack = InventoryUtil.copyItem( player.getHeldItem( hand ) );
    }

    public ItemStack getStack()
    {
        return m_stack;
    }

    @Override
    public boolean canInteractWith( @Nonnull EntityPlayer player )
    {
        if( player != null && player.isEntityAlive() )
        {
            ItemStack stack = player.getHeldItem( m_hand );
            if( (stack == m_stack) || (stack != null && m_stack != null && stack.getItem() == m_stack.getItem()) )
            {
                return true;
            }
        }
        return false;
    }
}
