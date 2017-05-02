/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.apis;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.shared.pocket.core.PocketServerComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

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

    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "equip",
            "unequip"
        };
    }

    @Override
    public Object[] callMethod( ILuaContext context, int method, Object[] arguments ) throws LuaException
    {
        switch( method )
        {
            case 0:
            {
                // equip
                if( !(m_computer.getEntity() instanceof EntityPlayer) )
                {
                    throw new LuaException( "Cannot find player" );
                }

                ItemStack pocketStack = m_computer.getStack();
                EntityPlayer player = (EntityPlayer) m_computer.getEntity();
                InventoryPlayer inventory = player.inventory;

                IPocketUpgrade previousUpgrade = m_computer.getUpgrade();
                IPocketUpgrade newUpgrade = null;

                int size = inventory.getSizeInventory(), held = inventory.currentItem;
                for (int i = 0; i < size; i++)
                {
                    ItemStack invStack = inventory.getStackInSlot( (i + held) % size );
                    if( invStack != null )
                    {
                        newUpgrade = ComputerCraft.getPocketUpgrade( invStack );

                        if( newUpgrade != null && newUpgrade != previousUpgrade )
                        {
                            // Consume an item from this stack and exit the loop
                            invStack = invStack.copy();
                            invStack.stackSize--;
                            inventory.setInventorySlotContents( (i + held) % size, invStack.stackSize <= 0 ? null : invStack );

                            break;
                        }
                    }
                }

                if( newUpgrade == null ) throw new LuaException( "Cannot find a valid upgrade" );

                // Remove the current upgrade
                if( previousUpgrade != null )
                {
                    ItemStack stack = previousUpgrade.getCraftingItem();
                    if( stack != null )
                    {
                        stack = InventoryUtil.storeItems( stack, inventory, 0, 36, inventory.currentItem );
                        if( stack != null )
                        {
                            WorldUtil.dropItemStack( stack, player.worldObj, player.posX, player.posY, player.posZ );
                        }
                    }
                }

                // Set the new upgrade
                ItemPocketComputer.setUpgrade( pocketStack, newUpgrade );
                m_computer.setUpgrade( newUpgrade );

                return null;
            }

            case 1:
            {
                // unequip
                if( !(m_computer.getEntity() instanceof EntityPlayer) )
                {
                    throw new LuaException( "Cannot find player" );
                }

                ItemStack pocketStack = m_computer.getStack();
                EntityPlayer player = (EntityPlayer) m_computer.getEntity();
                InventoryPlayer inventory = player.inventory;

                IPocketUpgrade previousUpgrade = m_computer.getUpgrade();

                if( previousUpgrade == null ) throw new LuaException( "Nothing to unequip" );

                ItemPocketComputer.setUpgrade( pocketStack, null );
                m_computer.setUpgrade( null );

                ItemStack stack = previousUpgrade.getCraftingItem();
                if( stack != null )
                {
                    stack = InventoryUtil.storeItems( stack, inventory, 0, 36, inventory.currentItem );
                    if( stack != null )
                    {
                        WorldUtil.dropItemStack( stack, player.worldObj, player.posX, player.posY, player.posZ );
                    }
                }

                return null;
            }
            default:
                return null;
        }
    }
}
