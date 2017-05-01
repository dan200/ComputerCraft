/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.common;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BlockGeneric extends Block implements
    ITileEntityProvider
{
    protected BlockGeneric( Material material )
    {
        super( material );
        this.isBlockContainer = true;
    }

    protected abstract IBlockState getDefaultBlockState( int damage, EnumFacing placedSide );
    protected abstract TileGeneric createTile( IBlockState state );
    protected abstract TileGeneric createTile( int damage );

    @Override
    public final void dropBlockAsItemWithChance( World world, BlockPos pos, IBlockState state, float chance, int fortune )
    {
    }
        
    @Override
    public final List<ItemStack> getDrops( IBlockAccess world, BlockPos pos, IBlockState state, int fortune )
    {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>( 1 );
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.getDroppedItems( drops, fortune, false, false );
        }
        return drops;
    }

    @Override
    public final IBlockState onBlockPlaced( World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int damage, EntityLivingBase placer )
    {
        return getDefaultBlockState( damage, side );
    }

    @Override
    public final boolean removedByPlayer( World world, BlockPos pos, EntityPlayer player, boolean willHarvest )
    {
    	if( !world.isRemote )
    	{
            // Drop items
            int fortune = EnchantmentHelper.getFortuneModifier( player );
            boolean creative = player.capabilities.isCreativeMode;
            boolean silkTouch = EnchantmentHelper.getSilkTouchModifier( player );
            dropAllItems( world, pos, fortune, creative, silkTouch );
        }

        // Remove block
        return super.removedByPlayer( world, pos, player, willHarvest );
    }

    public final void dropAllItems( World world, BlockPos pos, int fortune, boolean creative, boolean silkTouch )
    {
        // Get items to drop
        List<ItemStack> drops = new ArrayList<ItemStack>( 1 );
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.getDroppedItems( drops, fortune, creative, silkTouch );
        }

        // Drop items
        if( drops.size() > 0 )
        {
            Iterator<ItemStack> it = drops.iterator();
            while( it.hasNext() )
            {
                ItemStack item = it.next();
                dropItem( world, pos, item );
            }
        }
    }

    public final void dropItem( World world, BlockPos pos, ItemStack stack )
    {
        Block.spawnAsEntity( world, pos, stack );
    }
    
    @Override
    public final void breakBlock( World world, BlockPos pos, IBlockState newState )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.destroy();
        }
    	super.breakBlock( world, pos, newState );
        world.removeTileEntity( pos );
    }

	@Override
    public final ItemStack getPickBlock( MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getPickedItem();
        }
        return null;
    }

    @Override
    protected final ItemStack createStackedBlock( IBlockState state )
    {
        return null;
    }

    @Override
    public final boolean onBlockActivated( World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.onActivate( player, side, hitX, hitY, hitZ );
        }
        return false;
    }

    @Override
    public final void onNeighborBlockChange( World world, BlockPos pos, IBlockState state, Block neighbour )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.onNeighbourChange();
        }
    }

    @Override
    public final boolean isSideSolid( IBlockAccess world, BlockPos pos, EnumFacing side )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.isSolidOnSide( side.ordinal() );
        }
        return false;
    }

    @Override
    public final boolean canBeReplacedByLeaves( IBlockAccess world, BlockPos pos )
    {
        return false; // Generify me if anyone ever feels the need to change this
    }

    @Override
    public float getExplosionResistance( World world, BlockPos pos, Entity exploder, Explosion explosion )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorldObj() )
        {
            TileGeneric generic = (TileGeneric)tile;
            if( generic.isImmuneToExplosion( exploder ) )
            {
                return 2000.0f;
            }
        }
        return super.getExplosionResistance( exploder );
    }

    private void setBlockBounds( AxisAlignedBB bounds )
    {
        setBlockBounds(
            (float)bounds.minX, (float)bounds.minY, (float)bounds.minZ,
            (float)bounds.maxX, (float)bounds.maxY, (float)bounds.maxZ
        );
    }

    @Override
    public final void setBlockBoundsBasedOnState( IBlockAccess world, BlockPos pos )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorldObj() )
        {
            TileGeneric generic = (TileGeneric)tile;
            setBlockBounds( generic.getBounds() );
        }
    }

    @Override
    public final AxisAlignedBB getCollisionBoundingBox( World world, BlockPos pos, IBlockState state )
    {
        setBlockBoundsBasedOnState( world, pos );
        return super.getCollisionBoundingBox( world, pos, state );
    }

    @Override
    public final void addCollisionBoxesToList( World world, BlockPos pos, IBlockState state, AxisAlignedBB bigBox, List list, Entity entity )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;

            // Get collision bounds
            List<AxisAlignedBB> collision = new ArrayList<AxisAlignedBB>( 1 );
            generic.getCollisionBounds( collision );

            // Add collision bounds to list
            if( collision.size() > 0 )
            {
                Iterator<AxisAlignedBB> it = collision.iterator();
                while( it.hasNext() )
                {
                    AxisAlignedBB localBounds = it.next();
                    setBlockBounds( localBounds );

                    AxisAlignedBB bounds = super.getCollisionBoundingBox( world, pos, state );
                    if( bounds != null && bigBox.intersectsWith(bounds) )
                    {
                        list.add( bounds );
                    }
                }
            }
        }
    }

    @Override
    public final boolean canProvidePower()
    {
        return true;
    }

    @Override
    public final boolean canConnectRedstone( IBlockAccess world, BlockPos pos, EnumFacing side )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getRedstoneConnectivity( side );
        }
        return false;
    }

    @Override
    public final int getStrongPower( IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing oppositeSide )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorldObj() )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getRedstoneOutput( oppositeSide.getOpposite() );
        }
        return 0;
    }

    @Override
    public final int getWeakPower( IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing oppositeSide )
    {
        return getStrongPower( world, pos, state, oppositeSide );
    }

    public boolean getBundledRedstoneConnectivity( World world, BlockPos pos, EnumFacing side )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getBundledRedstoneConnectivity( side );
        }
        return false;
    }

    public int getBundledRedstoneOutput( World world, BlockPos pos, EnumFacing side )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorldObj() )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getBundledRedstoneOutput( side );
        }
        return 0;
    }

    @Override
    public boolean onBlockEventReceived( World world, BlockPos pos, IBlockState state, int eventID, int eventParameter )
    {
        if( world.isRemote )
        {
            TileEntity tile = world.getTileEntity( pos );
            if( tile != null && tile instanceof TileGeneric )
            {
                TileGeneric generic = (TileGeneric)tile;
                generic.onBlockEvent( eventID, eventParameter );
            }
        }
        return true;
    }

    @Override
    public final TileEntity createTileEntity( World world, IBlockState state )
    {
        return createTile( state );
    }

    @Override
    public final TileEntity createNewTileEntity( World world, int damage )
    {
        return createTile( damage );
    }
}
