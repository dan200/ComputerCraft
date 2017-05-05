/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.common;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.List;

public abstract class TileGeneric extends TileEntity
{
    public TileGeneric()
    {
    }

    public void requestTileEntityUpdate()
    {
        if( worldObj.isRemote )
        {
            ComputerCraftPacket packet = new ComputerCraftPacket();
            packet.m_packetType = ComputerCraftPacket.RequestTileEntityUpdate;

            BlockPos pos = getPos();
            packet.m_dataInt = new int[]{ pos.getX(), pos.getY(), pos.getZ() };
            ComputerCraft.sendToServer( packet );
        }
    }

    public void destroy()
    {
    }

    public BlockGeneric getBlock()
    {
        Block block = worldObj.getBlockState( getPos() ).getBlock();
        if( block != null && block instanceof BlockGeneric )
        {
            return (BlockGeneric)block;
        }
        return null;
    }

    protected final IBlockState getBlockState()
    {
        return worldObj.getBlockState( getPos() );
    }

    public final void updateBlock()
    {
        markDirty();
        BlockPos pos = getPos();
        IBlockState state = worldObj.getBlockState( pos );
        worldObj.markBlockRangeForRenderUpdate( pos, pos );
        worldObj.notifyBlockUpdate( getPos(), state, state, 3 );
    }

    protected final void setBlockState( IBlockState newState )
    {
        worldObj.setBlockState( getPos(), newState, 3 );
    }

    public void getDroppedItems( List<ItemStack> drops, boolean creative )
    {
    }

    public ItemStack getPickedItem()
    {
        return null;
    }

    public boolean onActivate( EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ )
    {
        return false;
    }

    public void onNeighbourChange()
    {
    }

    public void onNeighbourTileEntityChange( BlockPos neighbour )
    {
    }

    public boolean isSolidOnSide( int side )
    {
        return true;
    }

    public boolean isImmuneToExplosion( Entity exploder )
    {
        return false;
    }

    public AxisAlignedBB getBounds()
    {
        return new AxisAlignedBB( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 );
    }

    public void getCollisionBounds( List<AxisAlignedBB> bounds )
    {
        bounds.add( getBounds() );
    }

    public boolean getRedstoneConnectivity( EnumFacing side )
    {
        return false;
    }

    public int getRedstoneOutput( EnumFacing side )
    {
        return 0;
    }

    public boolean getBundledRedstoneConnectivity( EnumFacing side )
    {
        return false;
    }

    public int getBundledRedstoneOutput( EnumFacing side )
    {
        return 0;
    }

    protected double getInteractRange( EntityPlayer player )
    {
        return 8.0;
    }

    public boolean isUsable( EntityPlayer player, boolean ignoreRange )
    {
        if( player != null && player.isEntityAlive() )
        {
            if( worldObj.getTileEntity( getPos() ) == this )
            {
                if( !ignoreRange )
                {
                    double range = getInteractRange( player );
                    BlockPos pos = getPos();
                    return player.getEntityWorld() == worldObj &&
                           player.getDistanceSq( (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5 ) <= ( range * range );
                }
                return true;
            }
        }
        return false;
    }

    protected void writeDescription( NBTTagCompound nbttagcompound )
    {
    }

    protected void readDescription( NBTTagCompound nbttagcompound )
    {
    }

    public final void sendBlockEvent( int eventID )
    {
        sendBlockEvent( eventID, 0 );
    }

    public final void sendBlockEvent( int eventID, int eventParameter )
    {
        worldObj.addBlockEvent( getPos(), worldObj.getBlockState( getPos() ).getBlock(), eventID, eventParameter );
    }

    public void onBlockEvent( int eventID, int eventParameter )
    {
    }

    @Override
    public boolean shouldRefresh( World world, BlockPos pos, IBlockState oldState, IBlockState newState )
    {
        return newState.getBlock() != oldState.getBlock();
    }

    @Override
    public final SPacketUpdateTileEntity getUpdatePacket()
    {
        // Communicate properties
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeDescription( nbttagcompound );
        return new SPacketUpdateTileEntity( getPos(), 0, nbttagcompound );
    }

    @Override
    public final void onDataPacket( NetworkManager net, SPacketUpdateTileEntity packet )
    {
        switch( packet.getTileEntityType() )
        {
            case 0:
            {
                // Receive properties
                NBTTagCompound nbttagcompound = packet.getNbtCompound();
                readDescription( nbttagcompound );
                break;
            }
        }
    }

    @Override
    public NBTTagCompound getUpdateTag ()
    {
        NBTTagCompound tag = super.getUpdateTag();
        writeDescription( tag );
        return tag;
    }

    @Override
    public void handleUpdateTag (NBTTagCompound tag)
    {
        super.handleUpdateTag(tag);
        readDescription( tag );
    }
}
