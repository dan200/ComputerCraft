/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.blocks;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.BlockComputerBase;
import dan200.computercraft.shared.computer.blocks.TileCommandComputer;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.util.DirectionUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTurtle extends BlockComputerBase
{
    // Statics

    public static class Properties
    {
        public static final PropertyDirection FACING = PropertyDirection.create( "facing", EnumFacing.Plane.HORIZONTAL );
    }

    public static BlockTurtle createTurtleBlock()
    {
        return new BlockTurtle();
    }

    // Members

    public BlockTurtle()
    {
        super( Material.IRON );
        setHardness( 2.5f );
        setUnlocalizedName( "computercraft:turtle" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
        setDefaultState( this.blockState.getBaseState()
            .withProperty( Properties.FACING, EnumFacing.NORTH )
        );
    }

    @Override
    public EnumBlockRenderType getRenderType( IBlockState state )
    {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube( IBlockState state )
    {
        return false;
    }

    @Override
    public boolean isFullCube( IBlockState state )
    {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, Properties.FACING );
    }

    @Override
    public IBlockState getStateFromMeta( int meta )
    {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState( IBlockState state )
    {
        return 0;
    }

    @Override
    public IBlockState getActualState( IBlockState state, IBlockAccess world, BlockPos pos )
    {
        return state.withProperty( Properties.FACING, getDirection( world, pos ) );
    }

    @Override
    protected IBlockState getDefaultBlockState( ComputerFamily family, EnumFacing placedSide )
    {
        return getDefaultState();
    }

    private ComputerFamily getFamily()
    {
        if( this == ComputerCraft.Blocks.turtleAdvanced )
        {
            return ComputerFamily.Advanced;
        }
        else
        {
            return ComputerFamily.Normal;
        }
    }

    @Override
    public ComputerFamily getFamily( int damage )
    {
        return getFamily();
    }

    @Override
    public ComputerFamily getFamily( IBlockState state )
    {
        return getFamily();
    }

    @Override
    protected TileComputerBase createTile( ComputerFamily family )
    {
        if( this == ComputerCraft.Blocks.turtleAdvanced )
        {
            return new TileTurtleAdvanced();
        }
        else if( this == ComputerCraft.Blocks.turtleExpanded )
        {
            return new TileTurtleExpanded();
        }
        else
        {
            return new TileTurtle();
        }
    }

    @Override
    public void onBlockPlacedBy( World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemstack )
    {
        // Not sure why this is necessary
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileTurtle )
        {
            tile.setWorldObj( world ); // Not sure why this is necessary
            tile.setPos( pos ); // Not sure why this is necessary
        }

        // Set direction
        EnumFacing dir = DirectionUtil.fromEntityRot( player );
        setDirection( world, pos, dir.getOpposite() );
    }
}
