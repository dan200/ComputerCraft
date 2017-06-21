/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.apis;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.shared.pocket.core.PocketServerComputer;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
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
                return context.executeMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        if( !(m_computer.getEntity() instanceof EntityPlayer) )
                        {
                            throw new LuaException( "Cannot find player" );
                        }

                        EntityPlayer player = (EntityPlayer) m_computer.getEntity();
                        InventoryPlayer inventory = player.inventory;

                        IPocketUpgrade previousUpgrade = m_computer.getUpgrade();

                        // Attempt to find the upgrade, starting in the main segment, and then looking in the opposite
                        // one. We start from the position the item is currently in and loop round to the start.
                        IPocketUpgrade newUpgrade = findUpgrade( inventory.mainInventory, inventory.currentItem, previousUpgrade );
                        if( newUpgrade == null )
                        {
                            newUpgrade = findUpgrade( inventory.offHandInventory, 0, previousUpgrade );
                        }
                        if( newUpgrade == null ) throw new LuaException( "Cannot find a valid upgrade" );

                        // Remove the current upgrade
                        if( previousUpgrade != null )
                        {
                            ItemStack stack = previousUpgrade.getCraftingItem();
                            if( stack != null )
                            {
                                stack = InventoryUtil.storeItems( stack, new PlayerMainInvWrapper( inventory ), inventory.currentItem );
                                if( stack != null )
                                {
                                    WorldUtil.dropItemStack( stack, player.worldObj, player.posX, player.posY, player.posZ );
                                }
                            }
                        }

                        // Set the new upgrade
                        m_computer.setUpgrade( newUpgrade );

                        return null;
                    }
                } );

            case 1:
                // unequipBack
                return context.executeMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        if( !(m_computer.getEntity() instanceof EntityPlayer) )
                        {
                            throw new LuaException( "Cannot find player" );
                        }

                        EntityPlayer player = (EntityPlayer) m_computer.getEntity();
                        InventoryPlayer inventory = player.inventory;

                        IPocketUpgrade previousUpgrade = m_computer.getUpgrade();

                        if( previousUpgrade == null ) throw new LuaException( "Nothing to unequip" );

                        m_computer.setUpgrade( null );

                        ItemStack stack = previousUpgrade.getCraftingItem();
                        if( stack != null )
                        {
                            stack = InventoryUtil.storeItems( stack, new PlayerMainInvWrapper( inventory ), inventory.currentItem );
                            if( stack != null )
                            {
                                WorldUtil.dropItemStack( stack, player.worldObj, player.posX, player.posY, player.posZ );
                            }
                        }

                        return null;
                    }
                } );
            default:
                return null;
        }
    }

    private static IPocketUpgrade findUpgrade( ItemStack[] inv, int start, IPocketUpgrade previous )
    {
        for (int i = 0; i < inv.length; i++)
        {
            ItemStack invStack = inv[ (i + start) % inv.length ];
            if( invStack != null )
            {
                IPocketUpgrade newUpgrade = ComputerCraft.getPocketUpgrade( invStack );

                if( newUpgrade != null && newUpgrade != previous )
                {
                    // Consume an item from this stack and exit the loop
                    invStack = invStack.copy();
                    invStack.stackSize--;
                    inv[ (i + start) % inv.length ] = invStack.stackSize <= 0 ? null : invStack;

                    return newUpgrade;
                }
            }
        }

        return null;
    }
}
