/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.upgrades;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleVerb;
import dan200.computercraft.shared.turtle.core.TurtlePlaceCommand;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TurtleHoe extends TurtleTool
{
    public TurtleHoe( ResourceLocation id, int legacyId, String adjective, Item item )
    {
        super( id, legacyId, adjective, item );
    }

    @Override
    protected boolean canBreakBlock( IBlockState state, World world, BlockPos pos, TurtlePlayer player )
    {
        if( !super.canBreakBlock( state, world, pos, player ) ) return false;

        Material material = state.getMaterial();
        return material == Material.PLANTS ||
            material == Material.CACTUS ||
            material == Material.GOURD ||
            material == Material.LEAVES ||
            material == Material.VINE;
    }

    @Nonnull
    @Override
    public TurtleCommandResult useTool( @Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, @Nonnull TurtleVerb verb, @Nonnull EnumFacing direction )
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
