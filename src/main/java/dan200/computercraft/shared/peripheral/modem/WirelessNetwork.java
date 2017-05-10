/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class WirelessNetwork implements INetwork
{
    private static WirelessNetwork s_universalNetwork = null;

    public static WirelessNetwork getUniversal()
    {
        if( s_universalNetwork == null )
        {
            s_universalNetwork = new WirelessNetwork();
        }
        return s_universalNetwork;
    }

    public static void resetNetworks()
    {
        s_universalNetwork = null;
    }

    private Map<Integer, Set<IReceiver>> m_receivers;

    private WirelessNetwork()
    {
        m_receivers = new HashMap<Integer, Set<IReceiver>>();
    }

    @Override
    public synchronized void addReceiver( IReceiver receiver )
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

    @Override
    public synchronized void removeReceiver( IReceiver receiver )
    {
        int channel = receiver.getChannel();
        Set<IReceiver> receivers = m_receivers.get( channel );
        if( receivers != null )
        {
            receivers.remove( receiver );
        }
    }

    @Override
    public synchronized void transmit( int channel, int replyChannel, Object payload, World world, Vec3d pos, double range, boolean interdimensional, Object senderObject )
    {
        Set<IReceiver> receivers = m_receivers.get( channel );
        if( receivers != null )
        {
            for( IReceiver receiver : receivers )
            {
                tryTransmit( receiver, replyChannel, payload, world, pos, range, interdimensional, senderObject );
            }
        }
    }

    private void tryTransmit( IReceiver receiver, int replyChannel, Object payload, World world, Vec3d pos, double range, boolean interdimensional, Object senderObject )
    {
        if( receiver.getWorld() == world )
        {
            Vec3d position = receiver.getWorldPosition();
            double receiveRange = Math.max( range, receiver.getReceiveRange() ); // Ensure range is symmetrical
            double distanceSq = position.squareDistanceTo( pos );
            if( interdimensional || receiver.isInterdimensional() || distanceSq <= ( receiveRange * receiveRange ) )
            {
                receiver.receiveSameDimension( replyChannel, payload, Math.sqrt( distanceSq ), senderObject );
            }
        }
        else
        {
            if( interdimensional || receiver.isInterdimensional() )
            {
                receiver.receiveDifferentDimension( replyChannel, payload, senderObject );
            }
        }
    }

    @Override
    public boolean isWireless()
    {
        return true;
    }
}
