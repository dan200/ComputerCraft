/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.upgrades;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleVerb;
import dan200.computercraft.shared.turtle.core.TurtlePlaceCommand;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TurtleHoe extends TurtleTool
{
    public TurtleHoe( ResourceLocation id, int legacyId, String adjective, Item item )
    {
        super( id, legacyId, adjective, item );
    }

    @Override
	protected boolean canBreakBlock( World world, BlockPos pos )
	{
		if( super.canBreakBlock( world, pos ) )
		{
			Block block = world.getBlockState( pos ).getBlock();
			return
				block.getMaterial() == Material.plants ||
				block.getMaterial() == Material.cactus ||
				block.getMaterial() == Material.gourd ||
				block.getMaterial() == Material.leaves ||
				block.getMaterial() == Material.vine;
		}
		return false;
	}

    @Override
	public TurtleCommandResult useTool( ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, EnumFacing direction )
	{
		if( verb == TurtleVerb.Dig )
        {
            ItemStack hoe = m_item.copy();
            ItemStack remainder = TurtlePlaceCommand.deploy( hoe, turtle, direction, null, null );
            if( remainder != hoe )
            {
                return TurtleCommandResult.success();
            }
		}
		return super.useTool( turtle, side, verb, direction );
	}
}
