/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.apache.commons.lang3.tuple.Pair;

public class InventoryUtil
{
    // Methods for comparing things:

    public static boolean areItemsEqual( ItemStack a, ItemStack b )
    {
        return a == b || ItemStack.areItemStacksEqual( a, b );
    }

    public static boolean areItemsStackable( ItemStack a, ItemStack b )
    {
        return a == b || ItemHandlerHelper.canItemStacksStack( a, b );
    }

    public static ItemStack copyItem( ItemStack a )
    {
        if( a != null )
        {
            return a.copy();
        }
        return null;
    }

    // Methods for finding inventories:

    public static IItemHandler getInventory( World world, BlockPos pos, EnumFacing side )
    {
        // Look for tile with inventory
        int y = pos.getY();
        if( y >= 0 && y < world.getHeight() )
        {
            TileEntity tileEntity = world.getTileEntity( pos );
            if( tileEntity != null )
            {
                IItemHandler itemHandler = tileEntity.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side );
                if( itemHandler != null )
                {
                    return itemHandler;
                }
                else if( side != null && tileEntity instanceof ISidedInventory )
                {
                    return new SidedInvWrapper( (ISidedInventory) tileEntity, side );
                }
                else if( tileEntity instanceof IInventory )
                {
                    return new InvWrapper( (IInventory) tileEntity );
                }
            }
        }

        // Look for entity with inventory
        Vec3d vecStart = new Vec3d(
            pos.getX() + 0.5 + 0.6 * side.getFrontOffsetX(),
            pos.getY() + 0.5 + 0.6 * side.getFrontOffsetY(),
            pos.getZ() + 0.5 + 0.6 * side.getFrontOffsetZ()
        );
        EnumFacing dir = side.getOpposite();
        Vec3d vecDir = new Vec3d(
            dir.getFrontOffsetX(), dir.getFrontOffsetY(), dir.getFrontOffsetZ()
        );
        Pair<Entity, Vec3d> hit = WorldUtil.rayTraceEntities( world, vecStart, vecDir, 1.1 );
        if( hit != null )
        {
            Entity entity = hit.getKey();
            if( entity instanceof IInventory )
            {
                return new InvWrapper( (IInventory) entity );
            }
        }
        return null;
    }

    // Methods for placing into inventories:

    public static ItemStack storeItems( ItemStack itemstack, IItemHandler inventory, int start, int range, int begin )
    {
        int[] slots = makeSlotList( start, range, begin );
        return storeItems( itemstack, inventory, slots );
    }

    public static ItemStack storeItems( ItemStack itemstack, IItemHandler inventory, int begin )
    {
        int[] slots = makeSlotList( 0, inventory.getSlots(), begin );
        return storeItems( itemstack, inventory, slots );
    }

    public static ItemStack storeItems( ItemStack itemstack, IItemHandler inventory )
    {
        int[] slots = makeSlotList( 0, inventory.getSlots(), 0 ); // TODO: optimise this out?
        return storeItems( itemstack, inventory, slots );
    }

    // Methods for taking out of inventories

    public static ItemStack takeItems( int count, IItemHandler inventory, int start, int range, int begin )
    {
        int[] slots = makeSlotList( start, range, begin );
        return takeItems( count, inventory, slots );
    }

    public static ItemStack takeItems( int count, IItemHandler inventory, int begin )
    {
        int[] slots = makeSlotList( 0, inventory.getSlots(), begin );
        return takeItems( count, inventory, slots );
    }

    public static ItemStack takeItems( int count, IItemHandler inventory )
    {
        int[] slots = makeSlotList( 0, inventory.getSlots(), 0 );
        return takeItems( count, inventory, slots );
    }

    // Private methods

    private static int[] makeSlotList( int start, int range, int begin )
    {
        if( start < 0 || range == 0 )
        {
            return null;
        }

        int[] slots = new int[ range ];
        for( int n = 0; n < slots.length; ++n )
        {
            slots[ n ] = start + ((n + (begin - start)) % range);
        }
        return slots;
    }

    private static ItemStack storeItems( ItemStack stack, IItemHandler inventory, int[] slots )
    {
        if( slots == null || slots.length == 0 )
        {
            return stack;
        }
        if( stack == null || stack.stackSize == 0 )
        {
            return null;
        }

        // Inspect the slots in order and try to find empty or stackable slots
        ItemStack remainder = stack;
        for( int slot : slots )
        {
            if( remainder == null ) break;
            remainder = inventory.insertItem( slot, remainder, false );
        }
        return remainder;
    }

    private static ItemStack takeItems( int count, IItemHandler inventory, int[] slots )
    {
        if( slots == null )
        {
            return null;
        }

        // Combine multiple stacks from inventory into one if necessary
        ItemStack partialStack = null;
        int countRemaining = count;
        for( int slot : slots )
        {
            if( countRemaining <= 0 ) break;

            ItemStack stack = inventory.getStackInSlot( slot );
            if( stack != null )
            {
                if( partialStack == null || areItemsStackable( stack, partialStack ) )
                {
                    ItemStack extracted = inventory.extractItem( slot, countRemaining, false );
                    if( extracted != null )
                    {
                        countRemaining -= extracted.stackSize;
                        if( partialStack == null )
                        {
                            partialStack = extracted;
                        }
                        else
                        {
                            partialStack.stackSize += extracted.stackSize;
                        }
                    }
                }
            }
        }

        return partialStack;
    }
}
