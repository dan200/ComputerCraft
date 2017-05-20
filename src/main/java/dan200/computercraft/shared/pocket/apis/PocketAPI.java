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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PocketAPI implements ILuaAPI
{
    private final PocketServerComputer m_computer;
    private long m_clock;
    private long m_lastBeepTime;

    public PocketAPI( PocketServerComputer computer )
    {
        this.m_computer = computer;
        m_clock = 0;
        m_lastBeepTime = 0;
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
       m_clock++;
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
            "unequipBack",
            "beep"
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
            case 2:
                // beep
                float volume = 1f;
                float pitch = 1f;

                // Check if arguments are correct
                
                if ( arguments.length > 0 )
                {
                    if ( arguments[0] != null && !(arguments[0] instanceof Double) )  // Arg 0 wrong type
                    {
                        throw new LuaException("Expected number (optional), number (optional)" );
                    }

                    volume = arguments[0] != null ? ((Double) arguments[0]).floatValue() : 1f;

                }

                if ( arguments.length > 1 )
                {
                    if ( arguments[1] != null && !(arguments[1] instanceof Double) )  // Arg 1 wrong type
                    {
                        throw new LuaException("Expected string, number (optional), number (optional)" );
                    }
                    pitch = arguments[1] != null ? ((Double) arguments[1]).floatValue() : 1f;
                }
                
                if( !(m_computer.getEntity() instanceof EntityPlayer) )
                {
                    throw new LuaException( "Cannot find player" );
                }
                
                ResourceLocation resourceName = new ResourceLocation( "block.note.bell" );
                
                //Fallback to harp if this version of minecraft don't have bell.
                if ( !( SoundEvent.REGISTRY.containsKey( resourceName ) ) )
                {
                    resourceName = new ResourceLocation( "block.note.harp" );
                }
                //Please remove block above this after porting to 1.12
                if ( m_clock - m_lastBeepTime >= 1 ) //Once on tick only
                {
                    final EntityPlayer player = (EntityPlayer) m_computer.getEntity();
                    final World world = m_computer.getWorld();
                    final BlockPos pos = player.getPosition().up();
                    final ResourceLocation resource = resourceName;
                    final float vol = Math.min( volume, 1f );
                    final float soundPitch = (float) Math.pow( 2d, (pitch - 12) / 12d );
                    
                    context.issueMainThreadTask( new ILuaTask() 
                    {
                        @Nullable
                        @Override
                        public Object[] execute() throws LuaException {
                            world.playSound( null, pos, SoundEvent.REGISTRY.getObject( resource ), SoundCategory.RECORDS, vol, soundPitch );
                            return null;
                        }
                    });

                    m_lastBeepTime = m_clock;
                    return new Object[]{true}; // Success, return true
                }
                else
                {
                    return new Object[]{false}; // Failed - rate limited, return false
                }
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
