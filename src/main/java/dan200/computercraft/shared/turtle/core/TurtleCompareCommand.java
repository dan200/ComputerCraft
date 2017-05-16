/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

public class TurtleCompareCommand implements ITurtleCommand
{
    private final InteractDirection m_direction;

    public TurtleCompareCommand( InteractDirection direction )
    {
        m_direction = direction;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute( @Nonnull ITurtleAccess turtle )
    {
        // Get world direction from direction
        EnumFacing direction = m_direction.toWorldDir( turtle );

        // Get currently selected stack
        ItemStack selectedStack = turtle.getInventory().getStackInSlot( turtle.getSelectedSlot() );

        // Get stack representing thing in front
        World world = turtle.getWorld();
        BlockPos oldPosition = turtle.getPosition();
        BlockPos newPosition = oldPosition.offset( direction );

        ItemStack lookAtStack = null;
        if( WorldUtil.isBlockInWorld( world, newPosition ) )
        {
            if( !world.isAirBlock( newPosition ) )
            {
                IBlockState lookAtState = world.getBlockState( newPosition );
                Block lookAtBlock = lookAtState.getBlock();
                if( !lookAtBlock.isAir( lookAtState, world, newPosition ) )
                {
                    // Try createStackedBlock first
                    if( !lookAtBlock.hasTileEntity( lookAtState ) )
                    {
                        try
                        {
                            Method method = ReflectionHelper.findMethod(
                                Block.class, lookAtBlock,
                                new String[] { "func_180643_i", "createStackedBlock" },
                                IBlockState.class
                            );
                            lookAtStack = (ItemStack) method.invoke( lookAtBlock, lookAtState );
                        }
                        catch( Exception e )
                        {
                        }
                    }

                    // See if the block drops anything with the same ID as itself
                    // (try 5 times to try and beat random number generators)
                    for( int i=0; (i<5) && (lookAtStack == null); ++i )
                    {
                        java.util.List<ItemStack> drops = lookAtBlock.getDrops( world, newPosition, lookAtState, 0 );
                        if( drops != null && drops.size() > 0 )
                        {
                            for( ItemStack drop : drops )
                            {
                                if( drop.getItem() == Item.getItemFromBlock( lookAtBlock ) )
                                {
                                    lookAtStack = drop;
                                    break;
                                }
                            }
                        }
                    }

                    // Last resort: roll our own (which will probably be wrong)
                    if( lookAtStack == null )
                    {
                        Item item = Item.getItemFromBlock( lookAtBlock );
                        if( item != null && item.getHasSubtypes() )
                        {
                            lookAtStack = new ItemStack( item, 1, lookAtBlock.getMetaFromState( lookAtState ) );
                        }
                        else
                        {
                            lookAtStack = new ItemStack( item, 1, 0 );
                        }
                    }
                }
            }
        }

        // Compare them
        if( selectedStack == null && lookAtStack == null )
        {
            return TurtleCommandResult.success();
        }
        else if( selectedStack != null && lookAtStack != null )
        {
            if( selectedStack.getItem() == lookAtStack.getItem() )
            {
                if( !selectedStack.getHasSubtypes() )
                {
                    return TurtleCommandResult.success();
                }
                else if( selectedStack.getItemDamage() == lookAtStack.getItemDamage() )
                {
                    return TurtleCommandResult.success();
                }
                else if( selectedStack.getUnlocalizedName().equals( lookAtStack.getUnlocalizedName() ) )
                {
                    return TurtleCommandResult.success();
                }
            }
        }

        return TurtleCommandResult.failure();
    }
}
