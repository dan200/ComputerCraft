/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public class InventoryUtil
{
    // Methods for comparing things:

    public static boolean areItemsEqual( ItemStack a, ItemStack b )
    {
        if( areItemsStackable( a, b ) )
        {
            if( a == null || a.stackSize == b.stackSize )
            {
                return true;
            }
        }
        return false;
    }

    public static boolean areItemsStackable( ItemStack a, ItemStack b )
    {
        if( a == b )
        {
            return true;
        }
        
        if( a != null && b != null && a.getItem() == b.getItem() )
        {
            if( a.getItemDamage() == b.getItemDamage() )
            {
                if( (a.getTagCompound() == null && b.getTagCompound() == null) ||
                    (a.getTagCompound() != null && b.getTagCompound() != null && a.getTagCompound().equals( b.getTagCompound() ) ) )
                {
                    return true;
                }
            }
        }
        return false;
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

    public static IInventory getInventory( World world, BlockPos pos, EnumFacing side )
    {
        // Look for tile with inventory
        int y = pos.getY();
        if( y >= 0 && y < world.getHeight() )
        {
            TileEntity tileEntity = world.getTileEntity( pos );
            if( tileEntity != null && tileEntity instanceof IInventory )
            {
                // Special case code for double chests
                Block block = world.getBlockState( pos ).getBlock();
                if( block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST )
                {
                    // Check if it's a double chest, and return a combined inventory if so
                    if( world.getBlockState( pos.west() ).getBlock() == block )
                    {
                        return new InventoryLargeChest( "Large chest", (ILockableContainer)world.getTileEntity( pos.west() ), (ILockableContainer)tileEntity );
                    }
                    if( world.getBlockState( pos.east() ).getBlock() == block )
                    {
                        return new InventoryLargeChest( "Large chest", (ILockableContainer)tileEntity, (ILockableContainer)world.getTileEntity( pos.east() ) );
                    }
                    if( world.getBlockState( pos.north() ).getBlock() == block )
                    {
                        return new InventoryLargeChest( "Large chest", (ILockableContainer)world.getTileEntity( pos.north() ), (ILockableContainer)tileEntity );
                    }
                    if( world.getBlockState( pos.south() ).getBlock() == block )
                    {
                        return new InventoryLargeChest( "Large chest", (ILockableContainer)tileEntity, (ILockableContainer)world.getTileEntity( pos.south() ) );
                    }
                }

                // Otherwise, get tile inventory
                return (IInventory)tileEntity;
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
                return (IInventory) entity;
            }
        }
        return null;
    }
    
    // Methods for placing into inventories:
    
    public static ItemStack storeItems( ItemStack itemstack, IInventory inventory, int start, int range, int begin )
    {
        int[] slots = makeSlotList( start, range, begin );
        return storeItems( itemstack, inventory, slots, null );
    }

    public static ItemStack storeItems( ItemStack itemstack, IInventory inventory, EnumFacing side )
    {
        // Try ISidedInventory
        if( inventory instanceof ISidedInventory )
        {
            // Place into ISidedInventory
            ISidedInventory sidedInventory = (ISidedInventory)inventory;
            int[] slots = sidedInventory.getSlotsForFace( side );
            return storeItems( itemstack, inventory, slots, side );
        }

        // No ISidedInventory, store into any slot
        int[] slots = makeSlotList( 0, inventory.getSizeInventory(), 0 ); // TODO: optimise this out?
        return storeItems( itemstack, inventory, slots, side );
    }
    
    // Methods for taking out of inventories
    
    public static ItemStack takeItems( int count, IInventory inventory, int start, int range, int begin )
    {
        int[] slots = makeSlotList( start, range, begin );
        return takeItems( count, inventory, slots, null );
    }
        
    public static ItemStack takeItems( int count, IInventory inventory, EnumFacing side )
    {
        // Try ISidedInventory
        if( inventory instanceof ISidedInventory )
        {
            // Place into ISidedInventory
            ISidedInventory sidedInventory = (ISidedInventory)inventory;
            int[] slots = sidedInventory.getSlotsForFace( side );
            return takeItems( count, inventory, slots, side );
        }

        // No ISidedInventory, store into any slot
        int[] slots = makeSlotList( 0, inventory.getSizeInventory(), 0 );
        return takeItems( count, inventory, slots, side );
    }
    
    // Private methods

    private static int[] makeSlotList( int start, int range, int begin )
    {
        if( start < 0 || range == 0 )
        {
            return null;
        }
        
        int[] slots = new int[range];
        for( int n=0; n<slots.length; ++n )
        {
            slots[n] = start + ( (n + (begin - start)) % range );
        }
        return slots;
    }
        
    private static ItemStack storeItems( ItemStack stack, IInventory inventory, int[] slots, EnumFacing face )
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
            if( canPlaceItemThroughFace( inventory, slot, remainder, face ) )
            {
                ItemStack slotContents = inventory.getStackInSlot( slot );
                if( slotContents == null )
                {
                    // Slot is empty
                    int space = inventory.getInventoryStackLimit();
                    if( space >= remainder.stackSize )
                    {
                        // Items fit completely in slot
                        inventory.setInventorySlotContents( slot, remainder );
                        inventory.markDirty();
                        return null;
                    }
                    else
                    {
                        // Items fit partially in slot
                        remainder = remainder.copy();
                        inventory.setInventorySlotContents( slot, remainder.splitStack( space ) );
                    }
                }
                else if( areItemsStackable( slotContents, remainder ) )
                {
                    // Slot is occupied, but matching
                    int space = Math.min( slotContents.getMaxStackSize(), inventory.getInventoryStackLimit() ) - slotContents.stackSize;
                    if( space >= remainder.stackSize )
                    {
                        // Items fit completely in slot
                        slotContents.stackSize += remainder.stackSize;
                        inventory.setInventorySlotContents( slot, slotContents );
                        inventory.markDirty();
                        return null;
                    }
                    else if( space > 0 )
                    {
                        // Items fit partially in slot
                        remainder = remainder.copy();
                        remainder.stackSize -= space;
                        slotContents.stackSize += space;
                        inventory.setInventorySlotContents( slot, slotContents );
                    }
                }
            }
        }

        // If the output isn't the input, inform the change
        if( remainder != stack )
        {
            inventory.markDirty();
        }
        return remainder;
    }

    private static boolean canPlaceItemThroughFace( IInventory inventory, int slot, ItemStack itemstack, EnumFacing face )
    {
        if( inventory.isItemValidForSlot( slot, itemstack ) )
        {
            if( face != null && inventory instanceof ISidedInventory )
            {
                ISidedInventory sided = (ISidedInventory)inventory;
                return sided.canInsertItem( slot, itemstack, face );
            }
            return true;
        }
        return false;
    }

    private static ItemStack takeItems( int count, IInventory inventory, int[] slots, EnumFacing face )
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
            if( countRemaining > 0 )
            {
                ItemStack stack = inventory.getStackInSlot( slot );
                if( stack != null && canTakeItemThroughFace( inventory, slot, stack, face ) )
                {
                    if( partialStack == null || areItemsStackable( stack, partialStack ) )
                    {
                        // Found a matching thing
                        if( countRemaining >= stack.stackSize )
                        {
                            // Eat the thing whole
                            inventory.setInventorySlotContents( slot, null );
                            if( partialStack == null )
                            {
                                partialStack = stack;
                                countRemaining = Math.min( countRemaining, partialStack.getItem().getItemStackLimit( partialStack ) ) - stack.stackSize;
                            }
                            else
                            {
                                partialStack.stackSize += stack.stackSize;
                                countRemaining -= stack.stackSize;
                            }
                        }
                        else
                        {
                            // Eat part of the thing
                            ItemStack splitStack = stack.splitStack( countRemaining );
                            if( partialStack == null )
                            {
                                partialStack = splitStack;
                                countRemaining = Math.min( countRemaining, partialStack.getItem().getItemStackLimit( partialStack ) ) - splitStack.stackSize;
                            }
                            else
                            {
                                partialStack.stackSize += splitStack.stackSize;
                                countRemaining -= splitStack.stackSize;
                            }
                        }
                    }
                }
            }
        }

        // Return the final stack
        if( partialStack != null )
        {
            inventory.markDirty();
            return partialStack;
        }
        return null;
    }

    private static boolean canTakeItemThroughFace( IInventory inventory, int slot, ItemStack itemstack, EnumFacing face )
    {
        if( face != null && inventory instanceof ISidedInventory )
        {
            ISidedInventory sided = (ISidedInventory)inventory;
            return sided.canExtractItem( slot, itemstack, face );
        }
        return true;
    }
}
