/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class TurtleSuckCommand implements ITurtleCommand
{
    private final InteractDirection m_direction;
    private final int m_quantity;

    public TurtleSuckCommand( InteractDirection direction, int quantity )
    {
        m_direction = direction;
        m_quantity = quantity;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute( @Nonnull ITurtleAccess turtle )
    {
        // Sucking nothing is easy
        if( m_quantity == 0 )
        {
            turtle.playAnimation( TurtleAnimation.Wait );
            return TurtleCommandResult.success();
        }

        // Get world direction from direction
        EnumFacing direction = m_direction.toWorldDir( turtle );

        // Get inventory for thing in front
        World world = turtle.getWorld();
        BlockPos oldPosition = turtle.getPosition();
        BlockPos newPosition = WorldUtil.moveCoords( oldPosition, direction );
        EnumFacing side = direction.getOpposite();

        IItemHandler inventory = InventoryUtil.getInventory( world, newPosition, side );
        if( inventory != null )
        {
            // Take from inventory of thing in front
            ItemStack stack = InventoryUtil.takeItems( m_quantity, inventory );
            if( stack != null )
            {
                // Try to place into the turtle
                ItemStack remainder = InventoryUtil.storeItems( stack, turtle.getItemHandler(), turtle.getSelectedSlot() );
                if( remainder != null )
                {
                    // Put the remainder back in the inventory
                    InventoryUtil.storeItems( remainder, inventory );
                }

                // Return true if we consumed anything
                if( remainder != stack )
                {
                    turtle.playAnimation( TurtleAnimation.Wait );
                    return TurtleCommandResult.success();
                }
                else
                {
                    return TurtleCommandResult.failure( "No space for items" );
                }
            }
            return TurtleCommandResult.failure( "No items to take" );
        }
        else
        {
            // Suck up loose items off the ground
            AxisAlignedBB aabb = new AxisAlignedBB(
                newPosition.getX(), newPosition.getY(), newPosition.getZ(),
                newPosition.getX() + 1.0, newPosition.getY() + 1.0, newPosition.getZ() + 1.0
            );
            List<Entity> list = world.getEntitiesWithinAABBExcludingEntity( null, aabb );
            if( list.size() > 0 )
            {
                boolean foundItems = false;
                boolean storedItems = false;
                for( Entity entity : list )
                {
                    if( entity != null && entity instanceof EntityItem && !entity.isDead )
                    {
                        // Suck up the item
                        foundItems = true;
                        EntityItem entityItem = (EntityItem) entity;
                        ItemStack stack = entityItem.getEntityItem().copy();
                        ItemStack storeStack;
                        ItemStack leaveStack;
                        if( stack.stackSize > m_quantity )
                        {
                            storeStack = stack.splitStack( m_quantity );
                            leaveStack = stack;
                        }
                        else
                        {
                            storeStack = stack;
                            leaveStack = null;
                        }
                        ItemStack remainder = InventoryUtil.storeItems( storeStack, turtle.getItemHandler(), turtle.getSelectedSlot() );
                        if( remainder != storeStack )
                        {
                            storedItems = true;
                            if( remainder == null && leaveStack == null )
                            {
                                entityItem.setDead();
                            }
                            else if( remainder == null )
                            {
                                entityItem.setEntityItemStack( leaveStack );
                            }
                            else if( leaveStack == null )
                            {
                                entityItem.setEntityItemStack( remainder );
                            }
                            else
                            {
                                leaveStack.stackSize += remainder.stackSize;
                                entityItem.setEntityItemStack( leaveStack );
                            }
                            break;
                        }
                    }
                }

                if( foundItems )
                {
                    if( storedItems )
                    {
                        // Play fx
                        world.playBroadcastSound( 1000, oldPosition, 0 );
                        turtle.playAnimation( TurtleAnimation.Wait );
                        return TurtleCommandResult.success();
                    }
                    else
                    {
                        return TurtleCommandResult.failure( "No space for items" );
                    }
                }
            }
            return TurtleCommandResult.failure( "No items to take" );
        }
    }
}
