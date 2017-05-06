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
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
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
            generic.getDroppedItems( drops, false );
        }
        return drops;
    }

    @Override
    public final IBlockState onBlockPlaced( World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int damage, EntityLivingBase placer )
    {
        return getDefaultBlockState( damage, side );
    }

    @Override
    public final boolean removedByPlayer( IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest )
    {
        if( !world.isRemote )
        {
            // Drop items
            boolean creative = player.capabilities.isCreativeMode;
            dropAllItems( world, pos, creative );
        }

        // Remove block
        return super.removedByPlayer( state, world, pos, player, willHarvest );
    }

    public final void dropAllItems( World world, BlockPos pos, boolean creative )
    {
        // Get items to drop
        List<ItemStack> drops = new ArrayList<ItemStack>( 1 );
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.getDroppedItems( drops, creative );
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
    public final ItemStack getPickBlock( IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player )
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
    public final boolean onBlockActivated( World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ )
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
    public final void neighborChanged( IBlockState state, World world, BlockPos pos, Block block )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.onNeighbourChange();
        }
    }

    @Override
    public final void onNeighborChange( IBlockAccess world, BlockPos pos, BlockPos neighbour )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile instanceof TileGeneric )
        {
            TileGeneric generic = (TileGeneric)tile;
            generic.onNeighbourTileEntityChange( neighbour );
        }
    }

    @Override
    public final boolean isSideSolid( IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side )
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
    public final boolean canBeReplacedByLeaves( IBlockState state, IBlockAccess world, BlockPos pos )
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

    @Override
    public final AxisAlignedBB getBoundingBox( IBlockState state, IBlockAccess world, BlockPos pos )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorldObj() )
        {
            TileGeneric generic = (TileGeneric)tile;
            return generic.getBounds();
        }
        return FULL_BLOCK_AABB;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox( IBlockState state, World worldIn, BlockPos pos )
    {
        return getBoundingBox( state, worldIn, pos ).offset( pos );
    }

    @Override
    public final AxisAlignedBB getCollisionBoundingBox( IBlockState state, World world, BlockPos pos )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorldObj() )
        {
            TileGeneric generic = (TileGeneric)tile;

            // Get collision bounds
            List<AxisAlignedBB> collision = new ArrayList<AxisAlignedBB>( 1 );
            generic.getCollisionBounds( collision );

            // Return the union of the collision bounds
            if( collision.size() > 0 )
            {
                AxisAlignedBB aabb = collision.get( 0 );
                for (int i=1; i<collision.size(); ++i )
                {
                    AxisAlignedBB other = collision.get( 1 );
                    aabb = aabb.union( other );
                }
                return aabb;
            }
        }
        return FULL_BLOCK_AABB;
    }

    @Override
    public final void addCollisionBoxToList( IBlockState state, World world, BlockPos pos, AxisAlignedBB bigBox, List<AxisAlignedBB> list, Entity entity )
    {
        TileEntity tile = world.getTileEntity( pos );
        if( tile != null && tile instanceof TileGeneric && tile.hasWorldObj() )
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
                    addCollisionBoxToList( pos, bigBox, list, localBounds );
                }
            }
        }
    }

    @Override
    public final boolean canProvidePower( IBlockState state )
    {
        return true;
    }

    @Override
    public final boolean canConnectRedstone( IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side )
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
    public final int getStrongPower( IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing oppositeSide )
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
    public final int getWeakPower( IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing oppositeSide )
    {
        return getStrongPower( state, world, pos, oppositeSide );
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
    public boolean eventReceived( IBlockState state, World world, BlockPos pos, int eventID, int eventParameter )
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
