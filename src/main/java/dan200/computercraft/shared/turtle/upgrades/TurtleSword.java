/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.upgrades;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
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
            IBlockState state = world.getBlockState( pos );
            Material material = state.getMaterial( );
            return
                    material == Material.PLANTS ||
                    material == Material.LEAVES ||
                    material == Material.VINE ||
                    material == Material.CLOTH ||
                    material == Material.WEB;
        }
        return false;
    }

    @Override
    protected float getDamageMultiplier()
    {
        return 9.0f;
    }
}
