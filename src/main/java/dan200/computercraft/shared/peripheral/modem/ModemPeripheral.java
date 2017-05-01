/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class ModemPeripheral
	implements IPeripheral
{	
	private static class SingleChannelReceiver implements IReceiver
	{
		private ModemPeripheral m_owner;
		private int m_channel;
		
		public SingleChannelReceiver( ModemPeripheral owner, int channel )
		{
			m_owner = owner;
			m_channel = channel;
		}
		
		// IReceiver implementation
		
		@Override
		public int getChannel()
		{
			return m_channel;
		}

        @Override
        public World getWorld()
        {
            return m_owner.getWorld();
        }

		@Override
		public Vec3 getWorldPosition()
		{
			return m_owner.getWorldPosition();
		}
		
		@Override
		public boolean isInterdimensional()
		{
			return m_owner.isInterdimensional();
		}

        @Override
        public double getReceiveRange()
        {
            return m_owner.getReceiveRange();
        }

        @Override
		public void receiveSameDimension( int replyChannel, Object payload, double distance, Object senderObject )
		{
			if( senderObject != m_owner )
			{
				m_owner.receiveSameDimension( m_channel, replyChannel, payload, distance );
			}
		}

        @Override
        public void receiveDifferentDimension( int replyChannel, Object payload, Object senderObject )
        {
            if( senderObject != m_owner )
            {
                m_owner.receiveDifferentDimension( m_channel, replyChannel, payload );
            }
        }
	}
	
	private INetwork m_network;
	private IComputerAccess m_computer;
	private Map<Integer, IReceiver> m_channels;

	private boolean m_open;
	private boolean m_changed;

    public ModemPeripheral()
    {
        m_network = null;
        m_computer = null;
        
        m_channels = new HashMap<Integer, IReceiver>();
        m_open = false;
        m_changed = true;
    }

    private synchronized void setNetwork( INetwork network )
    {
        if( m_network != network )
        {
            // Leave old network
            if( m_network != null )
            {
                Iterator<IReceiver> it = m_channels.values().iterator();
                while( it.hasNext() )
                {
                    m_network.removeReceiver( it.next() );
                }
            }

            // Set new network
            m_network = network;

            // Join new network
            if( m_network != null )
            {
                Iterator<IReceiver> it = m_channels.values().iterator();
                while( it.hasNext() )
                {
                    m_network.addReceiver( it.next() );
                }
            }
        }
    }

    protected void switchNetwork()
    {
        setNetwork( getNetwork() );
    }

    protected abstract World getWorld();

    protected abstract Vec3 getPosition();
        
    public synchronized void destroy()
    {
        setNetwork( null );
        m_channels.clear();
        m_open = false;
    }
    
    public synchronized boolean pollChanged()
    {
		if( m_changed )
		{
			m_changed = false;
			return true;
		}
		return false;
    }
    
    protected abstract double getTransmitRange();

    protected abstract boolean isInterdimensional();

    public synchronized boolean isActive()
	{
		return (m_computer != null) && m_open;
	}

	public synchronized Vec3 getWorldPosition()
	{
		return getPosition();
	}
	
	public synchronized double getReceiveRange()
	{
		return getTransmitRange();
	}
	
	public void receiveSameDimension( int channel, int replyChannel, Object payload, double distance )
	{
		synchronized (m_channels)
		{
			if( m_computer != null && m_channels.containsKey( channel ) )
			{
				m_computer.queueEvent( "modem_message", new Object[] {
					m_computer.getAttachmentName(), channel, replyChannel, payload, distance
				} );
			}
		}
	}

    public void receiveDifferentDimension( int channel, int replyChannel, Object payload )
    {
        synchronized (m_channels)
        {
            if( m_computer != null && m_channels.containsKey( channel ) )
            {
                m_computer.queueEvent( "modem_message", new Object[] {
                        m_computer.getAttachmentName(), channel, replyChannel, payload
                } );
            }
        }
    }

    protected abstract INetwork getNetwork();
    
	// IPeripheral implementation

	@Override
    public String getType()
    {
    	return "modem";
    }
	   
	@Override
    public String[] getMethodNames()
    {
    	return new String[] {
			"open",
			"isOpen",
			"close",
			"closeAll",
			"transmit",
			"isWireless",
    	};
    }
    
    private static int parseChannel( Object[] arguments, int index ) throws LuaException
    {
		if( arguments.length <= index || !(arguments[index] instanceof Double) )
		{
			throw new LuaException( "Expected number" );
		}
		int channel = (int)((Double)arguments[index]).doubleValue();
		if( channel < 0 || channel > 65535 )
		{
			throw new LuaException( "Expected number in range 0-65535" );
		}
		return channel;
    }
    
	@Override
    public Object[] callMethod( IComputerAccess computer, ILuaContext context, int method, Object[] arguments ) throws LuaException, InterruptedException
    {
		switch( method )
		{
			case 0:
			{
				// open
				int channel = parseChannel( arguments, 0 );
				synchronized( this )
				{
					if( !m_channels.containsKey( channel ) )
					{
						if( m_channels.size() >= 128 )
						{
							throw new LuaException( "Too many open channels" );
						}
					
						IReceiver receiver = new SingleChannelReceiver( this, channel );
						m_channels.put( channel, receiver );
                        if( m_network != null )
                        {
    						m_network.addReceiver( receiver );
                        }
						if( !m_open )
						{
							m_open = true;
							m_changed = true;
						}
					}
				}
				return null;
			}
			case 1:
			{
				// isOpen
				int channel = parseChannel( arguments, 0 );
				synchronized( this )
				{
					boolean open = m_channels.containsKey( channel );
					return new Object[] { open };
				}
			}
			case 2:
			{
				// close
				int channel = parseChannel( arguments, 0 );
				synchronized( this )
				{
					if( m_channels.containsKey( channel ) )
					{
						IReceiver receiver = m_channels.get( channel );
                        if( m_network != null )
                        {
    						m_network.removeReceiver( receiver );
                        }
						m_channels.remove( channel );
						
						if( m_channels.size() == 0 )
						{
							m_open = false;
							m_changed = true;
						}
					}
				}
				return null;
			}
			case 3:
			{
				// closeAll
				synchronized( this )
				{
					if( m_channels.size() > 0 )
					{
                        if( m_network != null )
                        {
                            Iterator<IReceiver> it = m_channels.values().iterator();
                            while( it.hasNext() )
                            {
                                m_network.removeReceiver( it.next() );
                            }
                        }
						m_channels.clear();
						
						if( m_open )
						{
							m_open = false;
							m_changed = true;
						}
					}
				}
				return null;
			}
			case 4:
			{
				// transmit
				int channel = parseChannel( arguments, 0 );
				int replyChannel = parseChannel( arguments, 1 );
				Object payload = (arguments.length >= 3) ? arguments[2] : null;
				synchronized( this )
				{
                    World world = getWorld();
					Vec3 position = getPosition();
                    if( world != null && position != null && m_network != null)
                    {
    					m_network.transmit( channel, replyChannel, payload, world, position, getTransmitRange(), isInterdimensional(), this );
                    }
				}
				return null;
			}
			case 5:
			{
				// isWireless
                synchronized( this )
                {
                    if( m_network != null )
                    {
                        return new Object[] { m_network.isWireless() };
                    }
                }
                return new Object[] { false };
			}
			default:
			{
				return null;
			}
		}
    }
    
	@Override
    public synchronized void attach( IComputerAccess computer )
    {
    	m_computer = computer;
    	setNetwork( getNetwork() );
    	m_open = !m_channels.isEmpty();
    }
    
	@Override
    public synchronized void detach( IComputerAccess computer )
    {
    	if( m_network != null )
    	{
    		Iterator<IReceiver> it = m_channels.values().iterator();
    		while( it.hasNext() )
    		{
		    	m_network.removeReceiver( it.next() );
		    }
		    m_channels.clear();
    		m_network = null;
    	}

    	m_computer = null;
    		
		if( m_open )
		{
			m_open = false;		
			m_changed = true;
		}
    }

    @Override
    public abstract boolean equals( IPeripheral other );

    public IComputerAccess getComputer()
    {
    	return m_computer;
    }
}
