/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.terminal.TextBuffer;

import javax.annotation.Nonnull;

public class BufferAPI implements ILuaAPI
{
    private static class BufferLuaObject implements ILuaObject
    {
        private TextBuffer m_buffer;

        public BufferLuaObject( TextBuffer buffer )
        {
            m_buffer = buffer;
        }

        @Nonnull
        @Override
        public String[] getMethodNames()
        {
            return new String[] {
                "len",
                "tostring",
                "read",
                "write",
                "fill"
            };
        }

        @Override
        public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments ) throws LuaException, InterruptedException
        {
            switch( method )
            {
                case 0:
                {
                    // len
                    return new Object[] { m_buffer.length() };
                }
                case 1:
                {
                    // tostring
                    return new Object[] { m_buffer.toString() };
                }
                case 2:
                {
                    // read
                    int start = 0;
                    if( arguments.length >= 1 && (arguments[0] != null) )
                    {
                        if( !(arguments[0] instanceof Number) )
                        {
                            throw new LuaException( "Expected number" );
                        }
                        start = ((Number)arguments[1]).intValue() - 1;
                    }
                    int end = m_buffer.length();
                    if( arguments.length >= 2 && (arguments[1] != null) )
                    {
                        if( !(arguments[1] instanceof Number) )
                        {
                            throw new LuaException( "Expected number, number" );
                        }
                        end = ((Number)arguments[1]).intValue();
                    }
                    return new Object[] { m_buffer.read( start, end ) };
                }
                case 3:
                {
                    // write
                    if( arguments.length < 1 || !(arguments[0] instanceof String) )
                    {
                        throw new LuaException( "Expected string" );
                    }
                    String text = (String)(arguments[0]);
                    int start = 0;
                    if( arguments.length >= 2 && (arguments[1] != null) )
                    {
                        if( !(arguments[1] instanceof Number) )
                        {
                            throw new LuaException( "Expected string, number" );
                        }
                        start = ((Number)arguments[1]).intValue() - 1;
                    }
                    int end = start + text.length();
                    if( arguments.length >= 3 && (arguments[2] != null) )
                    {
                        if( !(arguments[2] instanceof Number) )
                        {
                            throw new LuaException( "Expected string, number, number" );
                        }
                        end = ((Number)arguments[2]).intValue();
                    }
                    m_buffer.write( text, start, end );
                    return null;
                }
                case 4:
                {
                    // fill
                    if( arguments.length < 1 || !(arguments[0] instanceof String) )
                    {
                        throw new LuaException( "Expected string" );
                    }
                    String text = (String)(arguments[0]);
                    int start = 0;
                    if( arguments.length >= 2 && (arguments[1] != null) )
                    {
                        if( !(arguments[1] instanceof Number) )
                        {
                            throw new LuaException( "Expected string, number" );
                        }
                        start = ((Number)arguments[1]).intValue() - 1;
                    }
                    int end = m_buffer.length();
                    if( arguments.length >= 3 && (arguments[2] != null) )
                    {
                        if( !(arguments[2] instanceof Number) )
                        {
                            throw new LuaException( "Expected string, number, number" );
                        }
                        end = ((Number)arguments[2]).intValue();
                    }
                    m_buffer.fill( text, start, end );
                    return null;
                }
                default:
                {
                    return null;
                }
            }
        }
    }

    public BufferAPI( IAPIEnvironment _env )
    {
    }

    @Override
    public String[] getNames()
    {
        return new String[] {
            "buffer"
        };
    }

    @Override
    public void startup()
    {
    }

    @Override
    public void advance( double _dt )
    {
    }

    @Override
    public void shutdown()
    {
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "new"
        };
    }

    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments ) throws LuaException, InterruptedException
    {
        switch( method )
        {
            case 0:
            {
                if( arguments.length < 1 || !(arguments[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String text = (String)(arguments[0]);
                int repetitions = 1;
                if( arguments.length >= 2 && arguments[1] != null )
                {
                    if( !(arguments[1] instanceof Number) )
                    {
                        throw new LuaException( "Expected string, number" );
                    }
                    repetitions = ((Number)arguments[1]).intValue();
                    if( repetitions < 0 )
                    {
                        throw new LuaException( "Expected positive number" );
                    }
                }
                TextBuffer buffer = new TextBuffer( text, repetitions );
                return new Object[] { new BufferLuaObject( buffer ) };
            }
            default:
            {
                return null;
            }
        }
    }
}
