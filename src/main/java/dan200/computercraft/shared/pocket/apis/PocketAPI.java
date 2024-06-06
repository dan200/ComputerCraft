/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.apis;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.shared.pocket.core.PocketServerComputer;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;

public class PocketAPI implements ILuaAPI
{
    private final PocketServerComputer m_computer;

    public PocketAPI( PocketServerComputer computer )
    {
        this.m_computer = computer;
    }

    @Override
    public String[] getNames()
    {
        return new String[] {
            "pocket"
        };
    }

    @Override
    public void startup()
    {
    }

    @Override
    public void advance( double dt )
    {
    }

    @Override
    public void shutdown()
    {
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "equipBack",
            "unequipBack"
        };
    }

    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments ) throws LuaException, InterruptedException
    {
        switch( method )
        {
            case 0:
                // equipBack
                return context.executeMainThreadTask( () ->
                {
                    if( !(m_computer.getEntity() instanceof EntityPlayer) )
                    {
                        return new Object[] { false, "Cannot find player" };
                    }

                    EntityPlayer player = (EntityPlayer) m_computer.getEntity();
                    InventoryPlayer inventory = player.inventory;
                    if( !m_computer.inInventory() ) return new Object[] { false, "Not in an inventory" };

                    IPocketUpgrade previousUpgrade = m_computer.getUpgrade();

                    // Attempt to find the upgrade, starting in the main segment, and then looking in the opposite
                    // one. We start from the position the item is currently in and loop round to the start.
                    IPocketUpgrade newUpgrade = findUpgrade( inventory.mainInventory, inventory.currentItem, previousUpgrade );
                    if( newUpgrade == null )
                    {
                        newUpgrade = findUpgrade( inventory.offHandInventory, 0, previousUpgrade );
                    }
                    if( newUpgrade == null ) return new Object[] { false, "Cannot find a valid upgrade" };

                    // Remove the current upgrade
                    if( previousUpgrade != null )
                    {
                        ItemStack stack = previousUpgrade.getCraftingItem();
                        if( !stack.isEmpty() )
                        {
                            stack = InventoryUtil.storeItems( stack, new PlayerMainInvWrapper( inventory ), inventory.currentItem );
                            if( !stack.isEmpty() )
                            {
                                WorldUtil.dropItemStack( stack, player.getEntityWorld(), player.posX, player.posY, player.posZ );
                            }
                        }
                    }

                    // Set the new upgrade
                    m_computer.setUpgrade( newUpgrade );

                    return new Object[] { true };
                } );

            case 1:
                // unequipBack
                return context.executeMainThreadTask( () ->
                {
                    if( !(m_computer.getEntity() instanceof EntityPlayer) )
                    {
                        return new Object[] { false, "Cannot find player" };
                    }

                    EntityPlayer player = (EntityPlayer) m_computer.getEntity();
                    InventoryPlayer inventory = player.inventory;
                    if( !m_computer.inInventory() ) return new Object[] { false, "Not in an inventory" };

                    IPocketUpgrade previousUpgrade = m_computer.getUpgrade();
                    if( previousUpgrade == null ) return new Object[] { false, "Nothing to unequip" };

                    m_computer.setUpgrade( null );

                    ItemStack stack = previousUpgrade.getCraftingItem();
                    if( !stack.isEmpty() )
                    {
                        stack = InventoryUtil.storeItems( stack, new PlayerMainInvWrapper( inventory ), inventory.currentItem );
                        if( stack.isEmpty() )
                        {
                            WorldUtil.dropItemStack( stack, player.getEntityWorld(), player.posX, player.posY, player.posZ );
                        }

                        return new Object[] { true };
                    }

                    return null;
                } );
            default:
                return null;
        }
    }

    private static IPocketUpgrade findUpgrade( NonNullList<ItemStack> inv, int start, IPocketUpgrade previous )
    {
        for( int i = 0; i < inv.size(); i++ )
        {
            ItemStack invStack = inv.get( (i + start) % inv.size() );
            if( !invStack.isEmpty() )
            {
                IPocketUpgrade newUpgrade = ComputerCraft.getPocketUpgrade( invStack );

                if( newUpgrade != null && newUpgrade != previous )
                {
                    // Consume an item from this stack and exit the loop
                    invStack = invStack.copy();
                    invStack.shrink( 1 );
                    inv.set( (i + start) % inv.size(), invStack.isEmpty() ? ItemStack.EMPTY : invStack );

                    return newUpgrade;
                }
            }
        }

        return null;
    }
}
