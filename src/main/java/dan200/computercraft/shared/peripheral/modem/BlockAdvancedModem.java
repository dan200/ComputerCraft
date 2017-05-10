/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockPeripheralBase;
import dan200.computercraft.shared.peripheral.common.TilePeripheralBase;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockAdvancedModem extends BlockPeripheralBase
{
    public static class Properties
    {
        public static final PropertyDirection FACING = PropertyDirection.create( "facing" );
        public static final PropertyBool ON = PropertyBool.create( "on" );
    }

    public BlockAdvancedModem()
    {
        setHardness( 2.0f );
        setUnlocalizedName( "computercraft:advanced_modem" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
        setDefaultState( this.blockState.getBaseState()
            .withProperty( Properties.FACING, EnumFacing.NORTH )
            .withProperty( Properties.ON, false )
        );
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, Properties.FACING, Properties.ON );
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta( int meta )
    {
        IBlockState state = getDefaultState();
        state = state.withProperty( Properties.FACING, EnumFacing.getFront( meta ) );
        state = state.withProperty( Properties.ON, false );
        return state;
    }

    @Override
    public int getMetaFromState( IBlockState state )
    {
        EnumFacing dir = state.getValue( Properties.FACING );
        return dir.getIndex();
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState( @Nonnull IBlockState state, IBlockAccess world, BlockPos pos )
    {
        int anim;
        EnumFacing dir;
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TilePeripheralBase )
        {
            TilePeripheralBase peripheral = (TilePeripheralBase) tile;
            anim = peripheral.getAnim();
            dir = peripheral.getDirection();
        }
        else
        {
            anim = 0;
            dir = state.getValue( Properties.FACING );
        }

        state = state.withProperty( Properties.FACING, dir );
        state = state.withProperty( Properties.ON, anim > 0 );
        return state;
    }

    @Override
    public IBlockState getDefaultBlockState( PeripheralType type, EnumFacing placedSide )
    {
        EnumFacing dir = placedSide.getOpposite();
        return getDefaultState().withProperty( Properties.FACING, dir );
    }

    @Override
    public PeripheralType getPeripheralType( int damage )
    {
        return PeripheralType.AdvancedModem;
    }

    @Override
    public PeripheralType getPeripheralType( IBlockState state )
    {
        return PeripheralType.AdvancedModem;
    }

    @Override
    public TilePeripheralBase createTile( PeripheralType type )
    {
        return new TileAdvancedModem();
    }
}
