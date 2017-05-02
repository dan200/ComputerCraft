/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.diskdrive;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockPeripheral;
import dan200.computercraft.shared.peripheral.common.TilePeripheralBase;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.Sound;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TileDiskDrive extends TilePeripheralBase
    implements IInventory, ITickable
{
    // Statics

    private static final int BLOCKEVENT_PLAY_RECORD = 0;
    private static final int BLOCKEVENT_STOP_RECORD = 1;

    private static class MountInfo
    {
        public String mountPath;
    }

    // Members

    private final Map<IComputerAccess, MountInfo> m_computers;

    private ItemStackHandler inventory;
    private IMount m_diskMount;

    private boolean m_recordQueued;
    private boolean m_recordPlaying;
    private boolean m_restartRecord;
    private boolean m_ejectQueued;

    public TileDiskDrive()
    {
        m_computers = new HashMap<IComputerAccess, MountInfo>();

        inventory = new ItemStackHandler( 1 );
        m_diskMount = null;

        m_recordQueued = false;
        m_recordPlaying = false;
        m_restartRecord = false;
    }

    @Override
    public void destroy()
    {
        ejectContents( true );
        synchronized( this )
        {
            if( m_recordPlaying )
            {
                sendBlockEvent( BLOCKEVENT_STOP_RECORD );
            }
        }
    }

    @Override
    public boolean onActivate( EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ )
    {
        if( player.isSneaking() )
        {
            // Try to put a disk into the drive
            if( !getWorld().isRemote )
            {
                ItemStack disk = player.getHeldItem( EnumHand.MAIN_HAND );
                if( disk != null && getStackInSlot(0) == null )
                {
                    if( ComputerCraft.getMedia( disk ) != null )
                    {
                        setInventorySlotContents( 0, disk );
                        player.setHeldItem( EnumHand.MAIN_HAND, null );
                        return true;
                    }
                }
            }
        }
        else
        {
            // Open the GUI
            if( !getWorld().isRemote )
            {
                ComputerCraft.openDiskDriveGUI( player, this );
            }
            return true;
        }
        return false;
    }

    @Override
    public EnumFacing getDirection()
    {
        IBlockState state = getBlockState();
        return (EnumFacing)state.getValue( BlockPeripheral.Properties.FACING );
    }

    @Override
    public void setDirection( EnumFacing dir )
    {
        if( dir.getAxis() == EnumFacing.Axis.Y )
        {
            dir = EnumFacing.NORTH;
        }
        setBlockState( getBlockState().withProperty( BlockPeripheral.Properties.FACING, dir ) );
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT( nbttagcompound );
        if (nbttagcompound.hasKey( "inventory" ))
        {
            inventory.deserializeNBT( nbttagcompound.getCompoundTag( "inventory" ) );
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound = super.writeToNBT( nbttagcompound );
        if (inventory != null)
        {
            nbttagcompound.setTag( "inventory", inventory.serializeNBT() );
        }
        return nbttagcompound;
    }

    @Override
    public void update()
    {
        super.update();

        // Ejection
        synchronized( this )
        {
            if( m_ejectQueued )
            {
                ejectContents( false );
                m_ejectQueued = false;
            }
        }

        // Music
        synchronized( this )
        {
            if( m_recordPlaying != m_recordQueued || m_restartRecord )
            {
                m_restartRecord = false;
                if( m_recordQueued )
                {
                    IMedia contents = getDiskMedia();
                    SoundEvent record = (contents != null) ? contents.getAudio( inventory.getStackInSlot( 0 ) ) : null;
                    if( record != null )
                    {
                        m_recordPlaying = true;
                        sendBlockEvent( BLOCKEVENT_PLAY_RECORD );
                    }
                    else
                    {
                        m_recordQueued = false;
                    }
                }
                else
                {
                    sendBlockEvent( BLOCKEVENT_STOP_RECORD );
                    m_recordPlaying = false;
                }
            }
        }
    }

    // IInventory implementation

    @Override
    public int getSizeInventory()
    {
        return inventory.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return inventory.getStackInSlot( i );
    }

    @Override
    public ItemStack removeStackFromSlot(int i)
    {
        ItemStack result = inventory.getStackInSlot( i );
        inventory.setStackInSlot( i,ItemStack.EMPTY );
        m_diskMount = null;

        return result;
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        if (inventory.getStackInSlot( 0 ) == ItemStack.EMPTY)
        {
            return ItemStack.EMPTY;
        }

        if (inventory.getStackInSlot( 0 ).getCount() <= j)
        {
            ItemStack disk = inventory.getStackInSlot( 0 );
            setInventorySlotContents( 0, null );
            return disk;
        }

        ItemStack part = inventory.getStackInSlot( 0 ).splitStack(j);
        if (inventory.getStackInSlot( 0 ).getCount() == 0)
        {
            setInventorySlotContents( 0, null );
        }
        else
        {
            setInventorySlotContents( 0, inventory.getStackInSlot( 0 ) );
        }
        return part;
    }

    @Override
    public void setInventorySlotContents( int i, ItemStack itemStack )
    {
        if( getWorld().isRemote )
        {
            inventory.setStackInSlot( i,itemStack );
            m_diskMount = null;
            markDirty();
            return;
        }

        synchronized( this )
        {
            if( InventoryUtil.areItemsStackable( itemStack, inventory.getStackInSlot( 0 ) ) )
            {
                inventory.setStackInSlot( i,itemStack );
                return;
            }

            // Unmount old disk
            if( !inventory.getStackInSlot( 0 ).isEmpty() )
            {
                Set<IComputerAccess> computers = m_computers.keySet();
                Iterator<IComputerAccess> it = computers.iterator();
                while( it.hasNext() )
                {
                    IComputerAccess computer = it.next();
                    unmountDisk( computer );
                }
            }

            // Stop music
            if( m_recordPlaying )
            {
                sendBlockEvent( BLOCKEVENT_STOP_RECORD );
                m_recordPlaying = false;
                m_recordQueued = false;
            }

            // Swap disk over
            inventory.setStackInSlot( i,itemStack );
            m_diskMount = null;
            markDirty();

            // Update contents
            updateAnim();

            // Mount new disk
            if( !inventory.getStackInSlot( 0 ).isEmpty() )
            {
                Set<IComputerAccess> computers = m_computers.keySet();
                for (IComputerAccess computer : computers)
                {
                    mountDisk( computer );
                }
            }
        }
    }

    @Override
    public boolean hasCustomName()
    {
        return getLabel() != null;
    }

    @Override
    public String getName()
    {
        String label = getLabel();
        if( label != null )
        {
            return label;
        }
        else
        {
            return "tile.computercraft:drive.name";
        }
    }

    @Override
    public ITextComponent getDisplayName()
    {
        if( hasCustomName() )
        {
            return new TextComponentString( getName() );
        }
        else
        {
            return new TextComponentTranslation( getName() );
        }
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public void openInventory( EntityPlayer player )
    {
    }

    @Override
    public void closeInventory( EntityPlayer player )
    {
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return true;
    }

    @Override
    public boolean isUsableByPlayer( EntityPlayer player )
    {
        return isUsable( player, false );
    }

    @Override
    public void clear()
    {
        synchronized( this )
        {
            setInventorySlotContents( 0, ItemStack.EMPTY );
        }
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public boolean isEmpty()
    {
        return inventory.getStackInSlot( 0 ).isEmpty();
    }
    // IPeripheralTile implementation

    @Override
    public IPeripheral getPeripheral( EnumFacing side )
    {
        return new DiskDrivePeripheral( this );
    }

    public ItemStack getDiskStack()
    {
        synchronized( this )
        {
            return getStackInSlot( 0 );
        }
    }

    public void setDiskStack( ItemStack stack )
    {
        synchronized( this )
        {
            setInventorySlotContents( 0, stack );
        }
    }

    public IMedia getDiskMedia()
    {
        return ComputerCraft.getMedia( getDiskStack() );
    }

    public String getDiskMountPath( IComputerAccess computer )
    {
        synchronized( this )
        {
            if( m_computers.containsKey( computer ) )
            {
                MountInfo info = m_computers.get( computer );
                return info.mountPath;
            }
        }
        return null;
    }

    public void mount( IComputerAccess computer )
    {
        synchronized( this )
        {
            m_computers.put( computer, new MountInfo() );
            mountDisk( computer );
        }
    }

    public void unmount( IComputerAccess computer )
    {
        synchronized( this )
        {
            unmountDisk( computer );
            m_computers.remove( computer );
        }
    }

    public void playDiskAudio()
    {
        synchronized( this )
        {
            IMedia media = getDiskMedia();
            if( media != null && media.getAudioTitle( inventory.getStackInSlot( 0 ) ) != null )
            {
                m_recordQueued = true;
                m_restartRecord = m_recordPlaying;
            }
        }
    }

    public void stopDiskAudio()
    {
        synchronized( this )
        {
            m_recordQueued = false;
            m_restartRecord = false;
        }
    }

    public void ejectDisk()
    {
        synchronized( this )
        {
            if( !m_ejectQueued )
            {
                m_ejectQueued = true;
            }
        }
    }

    // private methods

    private synchronized void mountDisk( IComputerAccess computer )
    {
        if( !inventory.getStackInSlot( 0 ).isEmpty() )
        {
            MountInfo info = m_computers.get( computer );
            IMedia contents = getDiskMedia();
            if( contents != null )
            {
                if( m_diskMount == null )
                {
                    m_diskMount = contents.createDataMount( inventory.getStackInSlot( 0 ), getWorld() );
                }
                if( m_diskMount != null )
                {
                    if( m_diskMount instanceof IWritableMount)
                    {
                        // Try mounting at the lowest numbered "disk" name we can
                        int n = 1;
                        while( info.mountPath == null )
                        {
                            info.mountPath = computer.mountWritable( (n==1) ? "disk" : ("disk" + n), (IWritableMount)m_diskMount );
                            n++;
                        }
                    }
                    else
                    {
                        // Try mounting at the lowest numbered "disk" name we can
                        int n = 1;
                        while( info.mountPath == null )
                        {
                            info.mountPath = computer.mount( (n==1) ? "disk" : ("disk" + n), m_diskMount );
                            n++;
                        }
                    }
                }
                else
                {
                    info.mountPath = null;
                }
            }
            computer.queueEvent( "disk", new Object[] { computer.getAttachmentName() } );
        }
    }

    private synchronized void unmountDisk( IComputerAccess computer )
    {
        if( !inventory.getStackInSlot( 0 ).isEmpty() )
        {
            MountInfo info = m_computers.get( computer );
            assert( info != null );
            if( info.mountPath != null )
            {
                computer.unmount( info.mountPath );
                info.mountPath = null;
            }
            computer.queueEvent( "disk_eject", new Object[] { computer.getAttachmentName() } );
        }
    }

    private synchronized void updateAnim()
    {
        if( !inventory.getStackInSlot( 0 ).isEmpty() )
        {
            IMedia contents = getDiskMedia();
            if( contents != null ) {
                setAnim( 2 );
            } else {
                setAnim( 1 );
            }
        }
        else
        {
            setAnim( 0 );
        }
    }

    private synchronized void ejectContents( boolean destroyed )
    {
        if( getWorld().isRemote )
        {
            return;
        }

        if( !inventory.getStackInSlot( 0 ).isEmpty() )
        {
            // Remove the disks from the inventory
            ItemStack disks = inventory.getStackInSlot( 0 );
            setInventorySlotContents( 0, null );

            // Spawn the item in the world
            int xOff = 0;
            int zOff = 0;
            if( !destroyed )
            {
                EnumFacing dir = getDirection();
                xOff = dir.getFrontOffsetX();
                zOff = dir.getFrontOffsetZ();
            }

            BlockPos pos = getPos();
            double x = (double)pos.getX() + 0.5 + ((double)xOff * 0.5);
            double y = (double)pos.getY() + 0.75;
            double z = (double)pos.getZ() + 0.5 + ((double)zOff * 0.5);
            EntityItem entityitem = new EntityItem( getWorld(), x, y, z, disks );
            entityitem.motionX = (double)xOff * 0.15;
            entityitem.motionY = 0.0;
            entityitem.motionZ = (double)zOff * 0.15;

            getWorld().spawnEntity(entityitem);
            if( !destroyed )
            {
                getWorld().playBroadcastSound(1000, getPos(), 0);
            }
        }
    }

    @Override
    public final void readDescription( NBTTagCompound nbttagcompound )
    {
        super.readDescription( nbttagcompound );
        if( nbttagcompound.hasKey( "inventory" ) )
        {
            inventory.deserializeNBT( nbttagcompound.getCompoundTag( "inventory" ) );
        }
        else
        {
            inventory.setStackInSlot( 0, ItemStack.EMPTY);
        }
        updateBlock();
    }

    @Override
    public void writeDescription( NBTTagCompound nbttagcompound )
    {
        super.writeDescription( nbttagcompound );
        if( !inventory.getStackInSlot( 0 ).isEmpty() )
        {
            nbttagcompound.setTag( "inventory",inventory.serializeNBT() );
        }
    }

    @Override
    public void onBlockEvent( int eventID, int eventParameter )
    {
        super.onBlockEvent( eventID, eventParameter );
        switch( eventID )
        {
            case BLOCKEVENT_PLAY_RECORD:
            {
                playRecord();
                break;
            }
            case BLOCKEVENT_STOP_RECORD:
            {
                stopRecord();
                break;
            }
        }
    }

    @Override
    public boolean shouldRefresh( World world, BlockPos pos, IBlockState oldState, IBlockState newState )
    {
        return super.shouldRefresh( world, pos, oldState, newState ) || ComputerCraft.Blocks.peripheral.getPeripheralType( newState ) != PeripheralType.DiskDrive;
    }

    // Private methods

    private void playRecord()
    {
        IMedia contents = getDiskMedia();
        SoundEvent record = (contents != null) ? contents.getAudio( inventory.getStackInSlot( 0 ) ) : null;
        if( record != null )
        {
            ComputerCraft.playRecord( record, contents.getAudioTitle( inventory.getStackInSlot( 0 ) ), getWorld(), getPos() );
        }
        else
        {
            ComputerCraft.playRecord( null, null, getWorld(), getPos() );
        }
    }

    private void stopRecord()
    {
        ComputerCraft.playRecord( null, null, getWorld(), getPos() );
    }
}