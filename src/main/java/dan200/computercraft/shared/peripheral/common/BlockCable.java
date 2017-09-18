/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.common;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockCable extends BlockPeripheralBase
{
    // Statics

    public static class Properties
    {
        public static final PropertyEnum<BlockCableModemVariant> MODEM = PropertyEnum.create( "modem", BlockCableModemVariant.class );
        public static final PropertyBool CABLE = PropertyBool.create( "cable" );
        public static final PropertyBool NORTH = PropertyBool.create( "north" );
        public static final PropertyBool SOUTH = PropertyBool.create( "south" );
        public static final PropertyBool EAST = PropertyBool.create( "east" );
        public static final PropertyBool WEST = PropertyBool.create( "west" );
        public static final PropertyBool UP = PropertyBool.create( "up" );
        public static final PropertyBool DOWN = PropertyBool.create( "down" );
    }

    public static boolean isCable( IBlockAccess world, BlockPos pos )
    {
        Block block = world.getBlockState( pos ).getBlock();
        if( block == ComputerCraft.Blocks.cable )
        {
            switch( ComputerCraft.Blocks.cable.getPeripheralType( world, pos ) )
            {
                case Cable:
                case WiredModemWithCable:
                {
                    return true;
                }
            }
        }
        return false;
    }

    // Members

    public BlockCable()
    {
        setHardness( 1.5f );
        setUnlocalizedName( "computercraft:cable" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
        setDefaultState( this.blockState.getBaseState()
            .withProperty( Properties.MODEM, BlockCableModemVariant.None )
            .withProperty( Properties.CABLE, true )
            .withProperty( Properties.NORTH, false )
            .withProperty( Properties.SOUTH, false )
            .withProperty( Properties.EAST, false )
            .withProperty( Properties.WEST, false )
            .withProperty( Properties.UP, false )
            .withProperty( Properties.DOWN, false )
        );
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer( this,
            Properties.MODEM,
            Properties.CABLE,
            Properties.NORTH,
            Properties.SOUTH,
            Properties.EAST,
            Properties.WEST,
            Properties.UP,
            Properties.DOWN
        );
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta( int meta )
    {
        IBlockState state = getDefaultState();
        if( meta < 6 )
        {
            state = state.withProperty( Properties.CABLE, false );
            state = state.withProperty( Properties.MODEM, BlockCableModemVariant.fromFacing( EnumFacing.getFront( meta ) ) );
        }
        else if( meta < 12 )
        {
            state = state.withProperty( Properties.CABLE, true );
            state = state.withProperty( Properties.MODEM, BlockCableModemVariant.fromFacing( EnumFacing.getFront( meta - 6 ) ) );
        }
        else if( meta == 13 )
        {
            state = state.withProperty( Properties.CABLE, true );
            state = state.withProperty( Properties.MODEM, BlockCableModemVariant.None );
        }
        return state;
    }

    @Override
    public int getMetaFromState( IBlockState state )
    {
        int meta = 0;
        boolean cable = state.getValue( Properties.CABLE );
        BlockCableModemVariant modem = state.getValue( Properties.MODEM );
        if( cable && modem != BlockCableModemVariant.None )
        {
            meta = 6 + modem.getFacing().getIndex();
        }
        else if( modem != BlockCableModemVariant.None )
        {
            meta = modem.getFacing().getIndex();
        }
        else if( cable )
        {
            meta = 13;
        }
        return meta;
    }

    @Override
    public IBlockState getDefaultBlockState( PeripheralType type, EnumFacing placedSide )
    {
        switch( type )
        {
            case Cable:
            {
                return getDefaultState()
                    .withProperty( Properties.CABLE, true )
                    .withProperty( Properties.MODEM, BlockCableModemVariant.None );
            }
            case WiredModem:
            default:
            {
                return getDefaultState()
                    .withProperty( Properties.CABLE, false )
                    .withProperty( Properties.MODEM, BlockCableModemVariant.fromFacing( placedSide.getOpposite() ) );
            }
            case WiredModemWithCable:
            {
                return getDefaultState()
                    .withProperty( Properties.CABLE, true )
                    .withProperty( Properties.MODEM, BlockCableModemVariant.fromFacing( placedSide.getOpposite() ) );
            }
        }
    }

    private boolean doesConnect( IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing dir )
    {
        if( !state.getValue( Properties.CABLE ) )
        {
            return false;
        }
        else if( state.getValue( Properties.MODEM ).getFacing() == dir )
        {
            return true;
        }
        else
        {
            return isCable( world, pos.offset( dir ) );
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState( @Nonnull IBlockState state, IBlockAccess world, BlockPos pos )
    {
        state = state.withProperty( Properties.NORTH, doesConnect( state, world, pos, EnumFacing.NORTH ) );
        state = state.withProperty( Properties.SOUTH, doesConnect( state, world, pos, EnumFacing.SOUTH ) );
        state = state.withProperty( Properties.EAST, doesConnect( state, world, pos, EnumFacing.EAST ) );
        state = state.withProperty( Properties.WEST, doesConnect( state, world, pos, EnumFacing.WEST ) );
        state = state.withProperty( Properties.UP, doesConnect( state, world, pos, EnumFacing.UP ) );
        state = state.withProperty( Properties.DOWN, doesConnect( state, world, pos, EnumFacing.DOWN ) );

        int anim;
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TilePeripheralBase )
        {
            TilePeripheralBase peripheral = (TilePeripheralBase)tile;
            anim = peripheral.getAnim();
        }
        else
        {
            anim = 0;
        }

        BlockCableModemVariant modem = state.getValue( Properties.MODEM );
        if( modem != BlockCableModemVariant.None )
        {
            modem = BlockCableModemVariant.values()[
                1 + 6 * anim + modem.getFacing().getIndex()
            ];
        }
        state = state.withProperty( Properties.MODEM, modem );

        return state;
    }

    @Override
    @Deprecated
    public boolean shouldSideBeRendered( IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side )
    {
        return true;
    }

    @Override
    public PeripheralType getPeripheralType( int damage )
    {
        return ((ItemCable) Item.getItemFromBlock( this )).getPeripheralType( damage );
    }

    @Override
    public PeripheralType getPeripheralType( IBlockState state )
    {
        boolean cable = state.getValue( Properties.CABLE );
        BlockCableModemVariant modem = state.getValue( Properties.MODEM );
        if( cable && modem != BlockCableModemVariant.None )
        {
            return PeripheralType.WiredModemWithCable;
        }
        else if( modem != BlockCableModemVariant.None )
        {
            return PeripheralType.WiredModem;
        }
        else
        {
            return PeripheralType.Cable;
        }
    }

    @Override
    public TilePeripheralBase createTile( PeripheralType type )
    {
        return new TileCable();
    }

    @Override
    @Deprecated
    public final boolean isOpaqueCube( IBlockState state )
    {
        return false;
    }

    @Override
    @Deprecated
    public final boolean isFullCube( IBlockState state )
    {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape( IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side )
    {
        return BlockFaceShape.UNDEFINED;
    }
}
