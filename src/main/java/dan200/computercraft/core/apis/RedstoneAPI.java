/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ICallContext;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.core.computer.Computer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static dan200.computercraft.core.apis.ArgumentHelper.*;

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

    @Nonnull
    @Override
    public MethodResult callMethod( @Nonnull ICallContext context, int method, @Nonnull Object[] args ) throws LuaException
    {
        switch( method )
        {
            case 0:
            {
                // getSides
                Map<Object,Object> table = new HashMap<>();
                for(int i=0; i< Computer.s_sideNames.length; ++i )
                {
                    table.put( i+1, Computer.s_sideNames[i] );
                }
                return MethodResult.of( table );
            }
            case 1:
            {
                // setOutput
                int side = parseSide( args );
                boolean output = getBoolean( args, 1 );
                m_environment.setOutput( side, output ? 15 : 0 );
                return MethodResult.empty();
            }
            case 2:
            {
                // getOutput
                int side = parseSide( args );
                return MethodResult.of( m_environment.getOutput( side ) > 0 );
            }
            case 3:
            {
                // getInput
                int side = parseSide( args );
                return MethodResult.of( m_environment.getInput( side ) > 0 );
            }
            case 4:
            {
                // setBundledOutput
                int side = parseSide( args );
                int output = getInt( args, 1 );
                m_environment.setBundledOutput( side, output );
                return MethodResult.empty();
            }
            case 5:
            {
                // getBundledOutput
                int side = parseSide( args );
                return MethodResult.of( m_environment.getBundledOutput( side ) );
            }
            case 6:
            {
                // getBundledInput
                int side = parseSide( args );
                return MethodResult.of( m_environment.getBundledInput( side ) );
            }
            case 7:
            {
                // testBundledInput
                int side = parseSide( args );
                int mask = getInt( args, 1 );
                int input = m_environment.getBundledInput( side );
                return MethodResult.of( ((input & mask) == mask) );
            }
            case 8:
            case 9:
            {
                // setAnalogOutput/setAnalogueOutput
                int side = parseSide( args );
                int output = getInt( args, 1 );
                if( output < 0 || output > 15 )
                {
                    throw new LuaException( "Expected number in range 0-15" );
                }
                m_environment.setOutput( side, output );
                return MethodResult.empty();
            }
            case 10:
            case 11:
            {
                // getAnalogOutput/getAnalogueOutput
                int side = parseSide( args );
                return MethodResult.of( m_environment.getOutput( side ) );
            }
            case 12:
            case 13:
            {
                // getAnalogInput/getAnalogueInput
                int side = parseSide( args );
                return MethodResult.of( m_environment.getInput( side ) );
            }
            default:
            {
                return MethodResult.empty();
            }
        }
    }

    @Nullable
    @Override
    @Deprecated
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments ) throws LuaException, InterruptedException
    {
        return callMethod( (ICallContext) context, method, arguments ).evaluate( context );
    }
    
    private int parseSide( Object[] args ) throws LuaException
    {
        String side = getString( args, 0 );
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
