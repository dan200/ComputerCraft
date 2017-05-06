/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;

import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.common.BlockGeneric;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.common.BlockCableModemVariant;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.util.IDAssigner;
import dan200.computercraft.shared.util.PeripheralUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import java.io.File;
import java.util.*;

public class TileCable extends TileModemBase
    implements INetwork
{
    private static final double MIN = 0.375;
    private static final double MAX = 1 - MIN;

    private static final AxisAlignedBB BOX_CENTRE = new AxisAlignedBB( MIN, MIN, MIN, MAX, MAX, MAX );
    private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[]{
        new AxisAlignedBB( MIN, 0, MIN, MAX, MIN, MAX ),   // Down
        new AxisAlignedBB( MIN, MAX, MIN, MAX, 1, MAX ),   // Up
        new AxisAlignedBB( MIN, MIN, 0, MAX, MAX, MIN ),   // North
        new AxisAlignedBB( MIN, MIN, MAX, MAX, MAX, 1 ),   // South
        new AxisAlignedBB( 0, MIN, MIN, MIN, MAX, MAX ),   // West
        new AxisAlignedBB( MAX, MIN, MIN, 1, MAX, MAX ),   // East
    };

    // Statics

    private static class Peripheral extends ModemPeripheral
    {
        private TileCable m_entity;
        
        public Peripheral( TileCable entity )
        {
            m_entity = entity;
        }

        @Override
        protected boolean isInterdimensional()
        {
            return false;
        }

        @Override
        protected double getTransmitRange()
        {
            return 256.0;
        }

        @Override
        protected INetwork getNetwork()
        {
            return m_entity;
        }

        @Override
        protected World getWorld()
        {
            return m_entity.getWorld();
        }

        @Override
        protected Vec3d getPosition()
        {
            EnumFacing direction = m_entity.getDirection();
            BlockPos pos = m_entity.getPos().offset( direction );
            return new Vec3d( (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5 );
        }

        @Override
        public String[] getMethodNames()
        {
            String[] methods = super.getMethodNames();
            String[] newMethods = new String[ methods.length + 5 ];
            System.arraycopy( methods, 0, newMethods, 0, methods.length );
            newMethods[ methods.length ] = "getNamesRemote";
            newMethods[ methods.length + 1 ] = "isPresentRemote";
            newMethods[ methods.length + 2 ] = "getTypeRemote";
            newMethods[ methods.length + 3 ] = "getMethodsRemote";
            newMethods[ methods.length + 4 ] = "callRemote";
            return newMethods;
        }

        private String parseString( Object[] arguments, int index ) throws LuaException
        {
            if( arguments.length < (index + 1) || !(arguments[index] instanceof String) )
            {
                throw new LuaException( "Expected string" );
            }
            return (String)arguments[index];
        }

        @Override
        public Object[] callMethod( IComputerAccess computer, ILuaContext context, int method, Object[] arguments ) throws LuaException, InterruptedException
        {
            String[] methods = super.getMethodNames();
            switch( method - methods.length )
            {
                case 0:
                {
                    // getNamesRemote
                    synchronized( m_entity.m_peripheralsByName )
                    {
                        int idx = 1;
                        Map<Object,Object> table = new HashMap<Object,Object>();
                        Iterator<String> it = m_entity.m_peripheralWrappersByName.keySet().iterator();
                        while( it.hasNext() )
                        {
                            String name = it.next();
                            table.put( idx++, name );
                        }
                        return new Object[] { table };
                    }
                }
                case 1:
                {
                    // isPresentRemote
                    String type = m_entity.getTypeRemote( parseString( arguments, 0 ) );
                    return new Object[] { type != null };
                }
                case 2:
                {
                    // getTypeRemote
                    String type = m_entity.getTypeRemote( parseString( arguments, 0 ) );
                    if( type != null )
                    {
                        return new Object[] { type };
                    }
                    return null;
                }
                case 3:
                {
                    // getMethodsRemote
                    String[] methodNames = m_entity.getMethodNamesRemote( parseString( arguments, 0 ) );
                    if( methodNames != null )
                    {
                        Map<Object,Object> table = new HashMap<Object,Object>();
                        for(int i=0; i<methodNames.length; ++i ) {
                            table.put( i+1, methodNames[i] );
                        }
                        return new Object[] { table };
                    }
                    return null;
                }
                case 4:
                {
                    // callRemote
                    String remoteName = parseString( arguments, 0 );
                    String methodName = parseString( arguments, 1 );
                    Object[] methodArgs = new Object[ arguments.length - 2 ];
                    System.arraycopy( arguments, 2, methodArgs, 0, arguments.length - 2 );
                    return m_entity.callMethodRemote( remoteName, context, methodName, methodArgs );
                }
                default:
                {
                    // The regular modem methods
                    return super.callMethod( computer, context, method, arguments );
                }
            }
        }

        @Override
        public void attach( IComputerAccess computer )
        {
            super.attach( computer );
            synchronized( m_entity.m_peripheralsByName )
            {
                Iterator<String> it = m_entity.m_peripheralsByName.keySet().iterator();
                while( it.hasNext() )
                {
                    String periphName = it.next();
                    IPeripheral peripheral = m_entity.m_peripheralsByName.get( periphName );
                    if( peripheral != null )
                    {
                        m_entity.attachPeripheral( periphName, peripheral );
                    }
                }
            }
        }

        @Override
        public synchronized void detach( IComputerAccess computer )
        {
            synchronized( m_entity.m_peripheralsByName )
            {
                Iterator<String> it = m_entity.m_peripheralsByName.keySet().iterator();
                while( it.hasNext() )
                {
                    String periphName = it.next();
                    m_entity.detachPeripheral( periphName );
                }
            }
            super.detach( computer );
        }

        @Override
        public boolean equals( IPeripheral other )
        {
            if( other instanceof Peripheral )
            {
                Peripheral otherModem = (Peripheral)other;
                return otherModem.m_entity == m_entity;
            }
            return false;
        }
    }

    private static int s_nextUniqueSearchID = 1;

    // Members

    private Map<Integer, Set<IReceiver>> m_receivers;
    private Queue<Packet> m_transmitQueue;
    
    private boolean m_peripheralAccessAllowed;
    private int m_attachedPeripheralID;
    
    private Map<String, IPeripheral> m_peripheralsByName;
    private Map<String, RemotePeripheralWrapper> m_peripheralWrappersByName;
    private boolean m_peripheralsKnown;
    private boolean m_destroyed;
    
    private int m_lastSearchID;
    
    public TileCable()
    {
        m_receivers = new HashMap<Integer, Set<IReceiver>>();
        m_transmitQueue = new LinkedList<Packet>();
        
        m_peripheralAccessAllowed = false;
        m_attachedPeripheralID = -1;
        
        m_peripheralsByName = new HashMap<String, IPeripheral>();
        m_peripheralWrappersByName = new HashMap<String, RemotePeripheralWrapper>();
        m_peripheralsKnown = false;
        m_destroyed = false;
        
        m_lastSearchID = 0;
    }

    @Override
    public void destroy()
    {
        if( !m_destroyed )
        {
            m_destroyed = true;
            networkChanged();
        }
        super.destroy();
    }

    @Override
    public EnumFacing getDirection()
    {
        IBlockState state = getBlockState();
        BlockCableModemVariant modem = (BlockCableModemVariant)state.getValue( BlockCable.Properties.MODEM );
        if( modem != BlockCableModemVariant.None )
        {
            return modem.getFacing();
        }
        else
        {
            return EnumFacing.NORTH;
        }
    }

    @Override
    public void setDirection( EnumFacing dir )
    {
        IBlockState state = getBlockState();
        BlockCableModemVariant modem = ( BlockCableModemVariant )state.getValue( BlockCable.Properties.MODEM );
        if( modem != BlockCableModemVariant.None )
        {
            setBlockState( state.withProperty( BlockCable.Properties.MODEM, BlockCableModemVariant.fromFacing( dir ) ) );
        }
    }

    @Override
    public void getDroppedItems( List<ItemStack> drops, boolean creative )
    {
        if( !creative )
        {
            PeripheralType type = getPeripheralType();
            switch( type )
            {
                case Cable:
                case WiredModem:
                {
                    drops.add( PeripheralItemFactory.create( type, getLabel(), 1 ) );
                    break;
                }
                case WiredModemWithCable:
                {
                    drops.add( PeripheralItemFactory.create( PeripheralType.WiredModem, getLabel(), 1 ) );
                    drops.add( PeripheralItemFactory.create( PeripheralType.Cable, null, 1 ) );
                    break;
                }
            }
        }
    }

    @Override
    public ItemStack getPickedItem()
    {
        if( getPeripheralType() == PeripheralType.WiredModemWithCable )
        {
            return PeripheralItemFactory.create( PeripheralType.WiredModem, getLabel(), 1 );
        }
        else
        {
            return super.getPickedItem();
        }
    }

    @Override
    public void onNeighbourChange()
    {
        EnumFacing dir = getDirection();
        if( !worldObj.isSideSolid(
            getPos().offset( dir ),
            dir.getOpposite()
        ) )
        {
            switch( getPeripheralType() )
            {
                case WiredModem:
                {
                    // Drop everything and remove block
                    ((BlockGeneric)getBlockType()).dropAllItems( worldObj, getPos(), false );
                    worldObj.setBlockToAir( getPos() );
                    break;
                }
                case WiredModemWithCable:
                {
                    // Drop the modem and convert to cable
                    ((BlockGeneric)getBlockType()).dropItem( worldObj, getPos(), PeripheralItemFactory.create( PeripheralType.WiredModem, getLabel(), 1 ) );
                    setLabel( null );
                    setBlockState( getBlockState().withProperty( BlockCable.Properties.MODEM, BlockCableModemVariant.None ) );
                    break;
                }
            }
        }
    }

    public AxisAlignedBB getModemBounds()
    {
        return super.getBounds();    
    }
    
    public AxisAlignedBB getCableBounds()
    {
        double xMin = 0.375;
        double yMin = 0.375;
        double zMin = 0.375;
        double xMax = 0.625;
        double yMax = 0.625;
        double zMax = 0.625;
        BlockPos pos = getPos();
        if( BlockCable.isCable( worldObj, pos.west() ) )
        {
            xMin = 0.0;
        }
        if( BlockCable.isCable( worldObj, pos.east() ) )
        {
            xMax = 1.0;
        }
        if( BlockCable.isCable( worldObj, pos.down() ) )
        {
            yMin = 0.0;
        }
        if( BlockCable.isCable( worldObj, pos.up() ) )
        {
            yMax = 1.0;
        }
        if( BlockCable.isCable( worldObj, pos.north() )  )
        {
            zMin = 0.0;
        }
        if( BlockCable.isCable( worldObj, pos.south() ) )
        {
            zMax = 1.0;
        }
        return new AxisAlignedBB( xMin, yMin, zMin, xMax, yMax, zMax );
    }
    
    @Override
    public AxisAlignedBB getBounds()
    {
        PeripheralType type = getPeripheralType();
        switch( type )
        {
            case WiredModem:
            default:
            {
                return getModemBounds();
            }
            case Cable:
            {
                return getCableBounds();
            }
            case WiredModemWithCable:
            {
                AxisAlignedBB modem = getModemBounds();
                AxisAlignedBB cable = getCableBounds();
                return modem.union( cable );
            }
        }
    }

    @Override
    public void getCollisionBounds( List<AxisAlignedBB> bounds )
    {
        PeripheralType type = getPeripheralType();
        if( type == PeripheralType.WiredModem || type == PeripheralType.WiredModemWithCable )
        {
            bounds.add( getModemBounds() );
        }
        if( type == PeripheralType.Cable || type == PeripheralType.WiredModemWithCable )
        {
            bounds.add( BOX_CENTRE );
            BlockPos pos = getPos();
            for (EnumFacing facing : EnumFacing.VALUES)
            {
                if( BlockCable.isCable( worldObj, pos.offset( facing ) ) )
                {
                    bounds.add( BOXES[ facing.ordinal() ] );
                }
            }
        }
    }

    @Override
    public boolean onActivate( EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ )
    {
        if( getPeripheralType() == PeripheralType.WiredModemWithCable && !player.isSneaking() )
        {
            if( !worldObj.isRemote )
            {
                // On server, we interacted if a peripheral was found
                String oldPeriphName = getConnectedPeripheralName();
                togglePeripheralAccess();
                String periphName = getConnectedPeripheralName();

                if( !Objects.equal( periphName, oldPeriphName ) )
                {
                    if( oldPeriphName != null )
                    {
                        player.addChatMessage(
                            new TextComponentTranslation( "gui.computercraft:wired_modem.peripheral_disconnected", oldPeriphName )
                        );
                    }
                    if( periphName != null )
                    {
                        player.addChatMessage(
                            new TextComponentTranslation( "gui.computercraft:wired_modem.peripheral_connected", periphName )
                        );
                    }
                    return true;
                }
            }
            else
            {
                // On client, we can't know this, so we assume so to be safe
                // The server will correct us if we're wrong
                return true;
            }
        }
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        // Read properties
        super.readFromNBT(nbttagcompound);
        m_peripheralAccessAllowed = nbttagcompound.getBoolean( "peripheralAccess" );
        m_attachedPeripheralID = nbttagcompound.getInteger( "peripheralID" );
    }

    @Override    
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound)
    {
        // Write properties
        nbttagcompound = super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean( "peripheralAccess", m_peripheralAccessAllowed );
        nbttagcompound.setInteger( "peripheralID", m_attachedPeripheralID );
        return nbttagcompound;
    }
    
    @Override
    protected ModemPeripheral createPeripheral()
    {
        return new Peripheral( this );
    }

    @Override
    protected void updateAnim()
    {
        int anim = 0;
        if( m_modem.isActive() )
        {
            anim += 1;
        }
        if( m_peripheralAccessAllowed )
        {
            anim += 2;
        }
        setAnim( anim );
    }

    // IPeripheralTile

    @Override
    public IPeripheral getPeripheral( EnumFacing side )
    {
        if( getPeripheralType() != PeripheralType.Cable )
        {
            return super.getPeripheral( side );
        }
        return null;
    }

    @Override
    public void update()
    {
        super.update();
        if( !worldObj.isRemote )
        {        
            synchronized( m_peripheralsByName )
            {
                if( !m_peripheralsKnown )
                {
                    findPeripherals();
                    m_peripheralsKnown = true;
                }
            }
            synchronized( m_transmitQueue )
            {
                while( m_transmitQueue.peek() != null )
                {
                    Packet p = m_transmitQueue.remove();
                    if( p != null )
                    {
                        dispatchPacket( p );
                    }
                }
            }
        }
    }
    
    // INetwork implementation
    
    @Override
    public void addReceiver( IReceiver receiver )
    {
        synchronized( m_receivers )
        {
            int channel = receiver.getChannel();
            Set<IReceiver> receivers = m_receivers.get( channel );
            if( receivers == null )
            {
                receivers = new HashSet<IReceiver>();
                m_receivers.put( channel, receivers );
            }
            receivers.add( receiver );
        }
    }
    
    @Override
    public void removeReceiver( IReceiver receiver )
    {
        synchronized( m_receivers )
        {
            int channel = receiver.getChannel();
            Set<IReceiver> receivers = m_receivers.get( channel );
            if( receivers != null )
            {
                receivers.remove( receiver );
            }
        }
    }
    
    @Override
    public void transmit( int channel, int replyChannel, Object payload, World world, Vec3d pos, double range, boolean interdimensional, Object senderObject )
    {
        Packet p = new Packet();
        p.channel = channel;
        p.replyChannel = replyChannel;
        p.payload = payload;
        p.senderObject = senderObject;
        synchronized( m_transmitQueue )
        {
            m_transmitQueue.offer(p);
        }
    }
    
    @Override
    public boolean isWireless()
    {
        return false;
    }
        
    private void attachPeripheral( String periphName, IPeripheral peripheral )
    {
        if( !m_peripheralWrappersByName.containsKey( periphName ) )
        { 
            RemotePeripheralWrapper wrapper = new RemotePeripheralWrapper( peripheral, m_modem.getComputer(), periphName );
            m_peripheralWrappersByName.put( periphName, wrapper );
            wrapper.attach();
        }
    }

    private void detachPeripheral( String periphName )
    {
        if( m_peripheralWrappersByName.containsKey( periphName ) )
        { 
            RemotePeripheralWrapper wrapper = m_peripheralWrappersByName.get( periphName );
            m_peripheralWrappersByName.remove( periphName );
            wrapper.detach();
        }
    }

    private String getTypeRemote( String remoteName )
    {
        synchronized( m_peripheralsByName )
        {
            RemotePeripheralWrapper wrapper = m_peripheralWrappersByName.get( remoteName );
            if( wrapper != null )
            {
                return wrapper.getType();
            }
        }
        return null;
    }
    
    private String[] getMethodNamesRemote( String remoteName )
    {
        synchronized( m_peripheralsByName )
        {
            RemotePeripheralWrapper wrapper = m_peripheralWrappersByName.get( remoteName );
            if( wrapper != null )
            {
                return wrapper.getMethodNames();
            }
        }
        return null;
    }
    
    private Object[] callMethodRemote( String remoteName, ILuaContext context, String method, Object[] arguments ) throws LuaException, InterruptedException
    {
        RemotePeripheralWrapper wrapper;
        synchronized( m_peripheralsByName )
        {
            wrapper = m_peripheralWrappersByName.get( remoteName );
        }
        if( wrapper != null )
        {
            return wrapper.callMethod( context, method, arguments );
        }
        throw new LuaException( "No peripheral: "+remoteName );
    }

    public void networkChanged()
    {
        if( !worldObj.isRemote )
        {
            if( !m_destroyed )
            {
                // If this modem is alive, rebuild the network
                searchNetwork( new ICableVisitor() {
                    public void visit( TileCable modem, int distance )
                    {
                        synchronized( modem.m_peripheralsByName )
                        {
                            modem.m_peripheralsKnown = false;
                        }
                    }
                } );
            }
            else
            {
                // If this modem is dead, rebuild the neighbours' networks
                for( EnumFacing dir : EnumFacing.values() )
                {
                    BlockPos offset = getPos().offset( dir );
                    if( offset.getY() >= 0 && offset.getY() < worldObj.getHeight() && BlockCable.isCable( worldObj, offset ) )
                    {
                        TileEntity tile = worldObj.getTileEntity( offset );
                        if( tile != null && tile instanceof TileCable )
                        {
                            TileCable modem = (TileCable)tile;
                            modem.networkChanged();
                        }
                    }
                }
            }
        }
    }
        
    // private stuff
        
    // Packet sending
    
    private class Packet
    {
        public int channel;
        public int replyChannel;
        public Object payload;
        public Object senderObject;
    }
        
    private void dispatchPacket( final Packet packet )
    {
        searchNetwork( new ICableVisitor() {
            public void visit( TileCable modem, int distance )
            {
                modem.receivePacket( packet, distance );
            }
        } );
    }
    
    private void receivePacket( Packet packet, int distanceTravelled )
    {
        synchronized( m_receivers )
        {
            Set<IReceiver> receivers = m_receivers.get( packet.channel );
            if( receivers != null )
            {
                Iterator<IReceiver> it = receivers.iterator();
                while( it.hasNext() )
                {
                    IReceiver receiver = it.next();
                    receiver.receiveSameDimension( packet.replyChannel, packet.payload, (double)distanceTravelled, packet.senderObject );
                }
            }
        }
    }
    
    // Remote peripheral control
    
    private static class RemotePeripheralWrapper implements IComputerAccess
    {
        private IPeripheral m_peripheral;
        private IComputerAccess m_computer;
        private String m_name;
        
        private String m_type;
        private String[] m_methods;
        private Map<String, Integer> m_methodMap;
                
        public RemotePeripheralWrapper( IPeripheral peripheral, IComputerAccess computer, String name )
        {
            m_peripheral = peripheral;
            m_computer = computer;
            m_name = name;

            m_type = peripheral.getType();
            m_methods = peripheral.getMethodNames();
            assert( m_type != null );
            assert( m_methods != null );
            
            m_methodMap = new HashMap<String, Integer>();
            for( int i=0; i<m_methods.length; ++i ) {
                if( m_methods[i] != null ) {
                    m_methodMap.put( m_methods[i], i );
                }
            }
        }
        
        public void attach()
        {
            m_peripheral.attach( this );
            m_computer.queueEvent( "peripheral", new Object[] { getAttachmentName() } );
        }

        public void detach()
        {
            m_peripheral.detach( this );
            m_computer.queueEvent( "peripheral_detach", new Object[] { getAttachmentName() } );
        }

        public String getType()
        {
            return m_type;
        }

        public String[] getMethodNames()
        {
            return m_methods;
        }

        public Object[] callMethod( ILuaContext context, String methodName, Object[] arguments ) throws LuaException, InterruptedException
        {
            if( m_methodMap.containsKey( methodName ) )
            {
                int method = m_methodMap.get( methodName );
                return m_peripheral.callMethod( this, context, method, arguments );
            }
            throw new LuaException( "No such method " + methodName );
        }

        // IComputerAccess implementation

        @Override
        public String mount( String desiredLocation, IMount mount )
        {
            return m_computer.mount( desiredLocation, mount, m_name );
        }

        @Override
        public String mount( String desiredLocation, IMount mount, String driveName )
        {
            return m_computer.mount( desiredLocation, mount, driveName );
        }

        @Override
        public String mountWritable( String desiredLocation, IWritableMount mount )
        {
            return m_computer.mountWritable( desiredLocation, mount, m_name );
        }

        @Override
        public String mountWritable( String desiredLocation, IWritableMount mount, String driveName )
        {
            return m_computer.mountWritable( desiredLocation, mount, driveName );
        }

        @Override
        public void unmount( String location )
        {
            m_computer.unmount( location );
        }
    
        @Override
        public int getID()
        {
            return m_computer.getID();
        }
        
        @Override
        public void queueEvent( String event, Object[] arguments )
        {
            m_computer.queueEvent( event, arguments );
        }
        
        @Override
        public String getAttachmentName()
        {
            return m_name;
        }
    }

    private void findPeripherals( )
    {
        final TileCable origin = this;
        synchronized( m_peripheralsByName )
        {
            // Collect the peripherals
            final Map<String, IPeripheral> newPeripheralsByName = new HashMap<String, IPeripheral>();
            if( getPeripheralType() == PeripheralType.WiredModemWithCable )
            {
                searchNetwork( new ICableVisitor() {
                    public void visit( TileCable modem, int distance )
                    {
                    if( modem != origin )
                    {
                        IPeripheral peripheral = modem.getConnectedPeripheral();
                        String periphName = modem.getConnectedPeripheralName();
                        if( peripheral != null && periphName != null )
                        {
                            newPeripheralsByName.put( periphName, peripheral );
                        }
                    }
                    }
                } );
            }
            //System.out.println( newPeripheralsByName.size()+" peripherals discovered" );

            // Detach all the old peripherals
            Iterator<String> it = m_peripheralsByName.keySet().iterator();
            while( it.hasNext() )
            {
                String periphName = it.next();
                if( !newPeripheralsByName.containsKey( periphName ) )
                {                    
                    detachPeripheral( periphName );
                    it.remove();
                }
            }

            // Attach all the new peripherals
            Iterator<String> it2 = newPeripheralsByName.keySet().iterator();
            while( it2.hasNext() )
            {
                String periphName = it2.next();
                if( !m_peripheralsByName.containsKey( periphName ) )
                {
                    IPeripheral peripheral = newPeripheralsByName.get( periphName );
                    if( peripheral != null )
                    {
                        m_peripheralsByName.put( periphName, peripheral );
                        if( isAttached() )
                        {
                            attachPeripheral( periphName, peripheral );
                        }
                    }
                }
            }
            //System.out.println( m_peripheralsByName.size()+" connected" );
        }
    }
    
    public void togglePeripheralAccess()
    {
        if( !m_peripheralAccessAllowed )
        {
            m_peripheralAccessAllowed = true;
            if( getConnectedPeripheral() == null )
            {
                m_peripheralAccessAllowed = false;
                return;
            }
        }
        else
        {
            m_peripheralAccessAllowed = false;
        }
        updateAnim(); 
        networkChanged();
    }
    
    public String getConnectedPeripheralName()
    {
        IPeripheral periph = getConnectedPeripheral();
        if( periph != null )
        {
            String type = periph.getType();
            if( m_attachedPeripheralID < 0 )
            {
                m_attachedPeripheralID = IDAssigner.getNextIDFromFile(new File(
                    ComputerCraft.getWorldDir(worldObj),
                    "computer/lastid_" + type + ".txt"
                ));
            }
            return type + "_" + m_attachedPeripheralID;
        }
        return null;
    }
    
    private IPeripheral getConnectedPeripheral()
    {
        if( m_peripheralAccessAllowed )
        {
            if( getPeripheralType() == PeripheralType.WiredModemWithCable )
            {
                EnumFacing facing = getDirection();
                BlockPos neighbour = getPos().offset( facing );
                return PeripheralUtil.getPeripheral( worldObj, neighbour, facing.getOpposite() );
            }
        }
        return null;
    }
    
    // Generic network search stuff
    
    private static interface ICableVisitor
    {
        public void visit( TileCable modem, int distance );
    }
    
    private static class SearchLoc
    {
        public World world;
        public BlockPos pos;
        public int distanceTravelled;
    }
    
    private static void enqueue( Queue<SearchLoc> queue, World world, BlockPos pos, int distanceTravelled )
    {
        int y = pos.getY();
        if( y >= 0 && y < world.getHeight() && BlockCable.isCable( world, pos ) )
        {
            SearchLoc loc = new SearchLoc();
            loc.world = world;
            loc.pos = pos;
            loc.distanceTravelled = distanceTravelled;
            queue.offer( loc );
        }
    }
    
    private static void visitBlock( Queue<SearchLoc> queue, SearchLoc location, int searchID, ICableVisitor visitor )
    {
        if( location.distanceTravelled >= 256 )
        {
            return;
        }
        
        TileEntity tile = location.world.getTileEntity( location.pos );
        if( tile != null && tile instanceof TileCable )
        {
            TileCable modem = (TileCable)tile;
            if( !modem.m_destroyed && modem.m_lastSearchID != searchID )
            {
                modem.m_lastSearchID = searchID;
                visitor.visit( modem, location.distanceTravelled + 1 );
                
                enqueue( queue, location.world, location.pos.up(), location.distanceTravelled + 1 );
                enqueue( queue, location.world, location.pos.down(), location.distanceTravelled + 1 );
                enqueue( queue, location.world, location.pos.south(), location.distanceTravelled + 1 );
                enqueue( queue, location.world, location.pos.north(), location.distanceTravelled + 1 );
                enqueue( queue, location.world, location.pos.east(), location.distanceTravelled + 1 );
                enqueue( queue, location.world, location.pos.west(), location.distanceTravelled + 1 );
            }
        }
    }

    private void searchNetwork( ICableVisitor visitor )
    {
        int searchID = ++s_nextUniqueSearchID;
        Queue<SearchLoc> queue = new LinkedList<SearchLoc>();
        enqueue( queue, worldObj, getPos(), 1 );
        
        int visited = 0;
        while( queue.peek() != null )
        {
            SearchLoc loc = queue.remove();
            visitBlock( queue, loc, searchID, visitor );
            visited++;
        }
        //System.out.println( "Visited "+visited+" common" );
    }
}
