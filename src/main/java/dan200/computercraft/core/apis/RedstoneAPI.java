/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.computer.Computer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class RedstoneAPI implements ILuaAPI
{
    private IAPIEnvironment m_environment;

    public RedstoneAPI( IAPIEnvironment environment )
    {
        m_environment = environment;
    }
    
    @Override
    public String[] getNames()
    {
        return new String[] {
            "rs", "redstone"
        };
    }

    @Override
    public void startup( )
    {
    }

    @Override
    public void advance( double _dt )
    {
    }
    
    @Override
    public void shutdown( )
    {
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "getSides",
            "setOutput",
            "getOutput",
            "getInput",
            "setBundledOutput",
            "getBundledOutput",
            "getBundledInput",
            "testBundledInput",
            "setAnalogOutput",
            "setAnalogueOutput",
            "getAnalogOutput",
            "getAnalogueOutput",
            "getAnalogInput",
            "getAnalogueInput",
        };
    }

    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException
    {
        switch( method )
        {
            case 0:
            {
                // getSides
                Map<Object,Object> table = new HashMap<Object,Object>();
                for(int i=0; i< Computer.s_sideNames.length; ++i )
                {
                    table.put( i+1, Computer.s_sideNames[i] );
                }
                return new Object[] { table };
            }
            case 1:
            {
                // setOutput
                if( args.length < 2 || args[0] == null || !(args[0] instanceof String) || args[1] == null || !(args[1] instanceof Boolean) )
                {
                    throw new LuaException( "Expected string, boolean" );
                }
                int side = parseSide( args );
                boolean output = (Boolean) args[ 1 ];
                m_environment.setOutput( side, output ? 15 : 0 );
                return null;
            }
            case 2:
            {
                // getOutput
                int side = parseSide( args );
                return new Object[] { m_environment.getOutput( side ) > 0 };
            }
            case 3:
            {
                // getInput
                int side = parseSide( args );
                return new Object[] { m_environment.getInput( side ) > 0 };
            }
            case 4:
            {
                // setBundledOutput
                if( args.length < 2 || args[0] == null || !(args[0] instanceof String) || args[1] == null || !(args[1] instanceof Double) )
                {
                    throw new LuaException( "Expected string, number" );
                }
                int side = parseSide( args );
                int output = ((Double)args[1]).intValue();
                m_environment.setBundledOutput( side, output );
                return null;
            }
            case 5:
            {
                // getBundledOutput
                int side = parseSide( args );
                return new Object[] { m_environment.getBundledOutput( side ) };
            }
            case 6:
            {
                // getBundledInput
                int side = parseSide( args );
                return new Object[] { m_environment.getBundledInput( side ) };
            }
            case 7:
            {
                // testBundledInput
                if( args.length < 2 || args[0] == null || !(args[0] instanceof String) || args[1] == null || !(args[1] instanceof Double) )
                {
                    throw new LuaException( "Expected string, number" );
                }
                int side = parseSide( args );
                int mask = ((Double)args[1]).intValue();
                int input = m_environment.getBundledInput( side );
                return new Object[] { ((input & mask) == mask) };
            }
            case 8:
            case 9:
            {
                // setAnalogOutput/setAnalogueOutput
                if( args.length < 2 || args[0] == null || !(args[0] instanceof String) || args[1] == null || !(args[1] instanceof Double) )
                {
                    throw new LuaException( "Expected string, number" );
                }
                int side = parseSide( args );
                int output = ((Double)args[1]).intValue();
                if( output < 0 || output > 15 )
                {
                    throw new LuaException( "Expected number in range 0-15" );
                }
                m_environment.setOutput( side, output );
                return null;
            }
            case 10:
            case 11:
            {
                // getAnalogOutput/getAnalogueOutput
                int side = parseSide( args );
                return new Object[] { m_environment.getOutput( side ) };
            }
            case 12:
            case 13:
            {
                // getAnalogInput/getAnalogueInput
                int side = parseSide( args );
                return new Object[] { m_environment.getInput( side ) };
            }
            default:
            {
                return null;
            }
        }
    }
    
    private int parseSide( Object[] args ) throws LuaException
    {
        if( args.length < 1 || args[0] == null || !(args[0] instanceof String) )
        {
            throw new LuaException( "Expected string" );
        }
        String side = (String)args[0];
        for( int n=0; n<Computer.s_sideNames.length; ++n )
        {
            if( side.equals( Computer.s_sideNames[n] ) )
            {
                return n;
            }
        }
        throw new LuaException( "Invalid side." );
    }
}
