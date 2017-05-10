/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.proxy.CCTurtleProxyCommon;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class TurtleEquipCommand implements ITurtleCommand
{
    private final TurtleSide m_side;

    public TurtleEquipCommand( TurtleSide side )
    {
        m_side = side;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute( @Nonnull ITurtleAccess turtle )
    {
        // Determine the upgrade to equipLeft
        ITurtleUpgrade newUpgrade;
        ItemStack newUpgradeStack;
        IInventory inventory = turtle.getInventory();
        ItemStack selectedStack = inventory.getStackInSlot( turtle.getSelectedSlot() );
        if( selectedStack != null )
        {
            newUpgradeStack = selectedStack.copy();
            newUpgrade = ComputerCraft.getTurtleUpgrade( newUpgradeStack );
            if( newUpgrade == null || !CCTurtleProxyCommon.isUpgradeSuitableForFamily( ((TurtleBrain)turtle).getFamily(), newUpgrade ) )
            {
                return TurtleCommandResult.failure( "Not a valid upgrade" );
            }
        }
        else
        {
            newUpgradeStack = null;
            newUpgrade = null;
        }

        // Determine the upgrade to replace
        ItemStack oldUpgradeStack;
        ITurtleUpgrade oldUpgrade = turtle.getUpgrade( m_side );
        if( oldUpgrade != null )
        {
            ItemStack craftingItem = oldUpgrade.getCraftingItem();
            oldUpgradeStack = (craftingItem != null) ? craftingItem.copy() : null;
        }
        else
        {
            oldUpgradeStack = null;
        }

        // Do the swapping:
        if( newUpgradeStack != null )
        {
            // Consume new upgrades item
            InventoryUtil.takeItems( 1, inventory, turtle.getSelectedSlot(), 1, turtle.getSelectedSlot() );
            inventory.markDirty();
        }
        if( oldUpgradeStack != null )
        {
            // Store old upgrades item
            ItemStack remainder = InventoryUtil.storeItems( oldUpgradeStack, inventory, 0, inventory.getSizeInventory(), turtle.getSelectedSlot() );
            if( remainder != null )
            {
                // If there's no room for the items, drop them
                BlockPos position = turtle.getPosition();
                WorldUtil.dropItemStack( remainder, turtle.getWorld(), position, turtle.getDirection() );
            }
            inventory.markDirty();
        }
        turtle.setUpgrade( m_side, newUpgrade );

        // Animate
        if( newUpgrade != null || oldUpgrade != null )
        {
            turtle.playAnimation( TurtleAnimation.Wait );
        }

        return TurtleCommandResult.success();
    }
}
