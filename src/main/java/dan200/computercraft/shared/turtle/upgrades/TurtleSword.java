/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.upgrades;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TurtleSword extends TurtleTool
{
	public TurtleSword( ResourceLocation id, int legacyId, String adjective, Item item )
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
				block.getMaterial() == Material.leaves ||
				block.getMaterial() == Material.vine ||
				block.getMaterial() == Material.cloth ||
				block.getMaterial() == Material.web;
		}
		return false;
	}

    @Override
	protected float getDamageMultiplier()
	{
		return 9.0f;
	}
}
