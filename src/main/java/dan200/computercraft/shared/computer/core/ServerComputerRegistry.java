/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.core;

import java.util.Iterator;

public class ServerComputerRegistry extends ComputerRegistry<ServerComputer>
{
    public ServerComputerRegistry()
    {
    }

    public void update()
    {
        Iterator<ServerComputer> it = getComputers().iterator();
        while( it.hasNext() )
        {
            ServerComputer computer = it.next();
            if( computer.hasTimedOut() )
            {
                //System.out.println( "TIMED OUT SERVER COMPUTER " + computer.getInstanceID() );
                computer.unload();
                computer.broadcastDelete();
                it.remove();
                //System.out.println( getComputers().size() + " SERVER COMPUTERS" );
            }
            else
            {
                computer.update();
                if( computer.hasTerminalChanged() || computer.hasOutputChanged() )
                {
                    computer.broadcastState();
                }
            }
        }
    }

    @Override
    public void add( int instanceID, ServerComputer computer )
    {
        //System.out.println( "ADD SERVER COMPUTER " + instanceID );
        super.add( instanceID, computer );
        computer.broadcastState();
        //System.out.println( getComputers().size() + " SERVER COMPUTERS" );
    }

    @Override
    public void remove( int instanceID )
    {
        //System.out.println( "REMOVE SERVER COMPUTER " + instanceID );
        ServerComputer computer = get( instanceID );
        if( computer != null )
        {
            computer.unload();
            computer.broadcastDelete();
        }
        super.remove( instanceID );
        //System.out.println( getComputers().size() + " SERVER COMPUTERS" );
    }

    @Override
    public void reset()
    {
        //System.out.println( "RESET SERVER COMPUTERS" );
        for( ServerComputer computer : getComputers() )
        {
            computer.unload();
        }
        super.reset();
        //System.out.println( getComputers().size() + " SERVER COMPUTERS" );
    }
}
