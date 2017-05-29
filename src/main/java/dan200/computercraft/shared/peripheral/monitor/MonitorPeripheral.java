/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.monitor;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.util.Palette;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;

import javax.annotation.Nonnull;

public class MonitorPeripheral implements IPeripheral
{
    private final TileMonitor m_monitor;

    public MonitorPeripheral( TileMonitor monitor )
    {
        m_monitor = monitor;
    }

    // IPeripheral implementation

    @Nonnull
    @Override
    public String getType()
    {
        return "monitor";
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "write",
            "scroll",
            "setCursorPos",
            "setCursorBlink",
            "getCursorPos",
            "getSize",
            "clear",
            "clearLine",
            "setTextScale",
            "setTextColour",
            "setTextColor",
            "setBackgroundColour",
            "setBackgroundColor",
            "isColour",
            "isColor",
            "getTextColour",
            "getTextColor",
            "getBackgroundColour",
            "getBackgroundColor",
            "blit",
            "setPaletteColour",
            "setPaletteColor",
            "getPaletteColour",
            "getPaletteColor"
        };
    }

    @Override
    public Object[] callMethod( @Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object args[] ) throws LuaException
    {
        switch( method )
        {
            case 0:
            {
                // write
                String text;
                if( args.length > 0 && args[0] != null ) {
                    text = args[0].toString();
                } else {
                    text = "";
                }
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                terminal.write( text );
                terminal.setCursorPos( terminal.getCursorX() + text.length(), terminal.getCursorY() );
                return null;
            }
            case 1:
            {
                // scroll
                if( args.length < 1 || !(args[0] instanceof Number) )
                {
                    throw new LuaException( "Expected number" );
                }
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                terminal.scroll( ((Number)(args[0])).intValue() );
                return null;
            }
            case 2:
            {
                // setCursorPos
                if( args.length < 2 || !(args[0] instanceof Number) || !(args[1] instanceof Number) )
                {
                    throw new LuaException( "Expected number, number" );
                }
                int x = ((Number)args[0]).intValue() - 1;
                int y = ((Number)args[1]).intValue() - 1;
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                terminal.setCursorPos( x, y );
                return null;
            }
            case 3:
            {
                // setCursorBlink
                if( args.length < 1 || !(args[0] instanceof Boolean) )
                {
                    throw new LuaException( "Expected boolean" );
                }
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                terminal.setCursorBlink( (Boolean) args[ 0 ] );
                return null;
            }
            case 4:
            {
                // getCursorPos
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                return new Object[] {
                    terminal.getCursorX() + 1,
                    terminal.getCursorY() + 1
                };
            }
            case 5:
            {
                // getSize
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                return new Object[] {
                    terminal.getWidth(),
                    terminal.getHeight()
                };
            }
            case 6:
            {
                // clear
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                terminal.clear();
                return null;
            }
            case 7:
            {
                // clearLine
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                terminal.clearLine();
                return null;
            }
            case 8:
            {
                // setTextScale
                if( args.length < 1 || !(args[0] instanceof Number) )
                {
                    throw new LuaException( "Expected number" );
                }
                int scale = (int)(((Number)args[0]).doubleValue() * 2.0);
                if( scale < 1 || scale > 10 )
                {
                    throw new LuaException( "Expected number in range 0.5-5" );
                }
                m_monitor.setTextScale( scale );
                return null;
            }
            case 9:
            case 10:
            {
                // setTextColour/setTextColor
                int colour = dan200.computercraft.core.apis.TermAPI.parseColour( args );
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                terminal.setTextColour( colour );
                return null;
            }
            case 11:
            case 12:
            {
                // setBackgroundColour/setBackgroundColor
                int colour = dan200.computercraft.core.apis.TermAPI.parseColour( args );
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                terminal.setBackgroundColour( colour );
                return null;
            }
            case 13:
            case 14:
            {
                // isColour/isColor
                return new Object[] {
                    m_monitor.getTerminal().isColour()
                };
            }
            case 15:
            case 16:
            {
                // getTextColour/getTextColor
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                return dan200.computercraft.core.apis.TermAPI.encodeColour( terminal.getTextColour() );
            }
            case 17:
            case 18:
            {
                // getBackgroundColour/getBackgroundColor
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                return dan200.computercraft.core.apis.TermAPI.encodeColour( terminal.getBackgroundColour() );
            }
            case 19:
            {
                // blit
                if( args.length < 3 || !(args[0] instanceof String) || !(args[1] instanceof String) || !(args[2] instanceof String) )
                {
                    throw new LuaException( "Expected string, string, string" );
                }

                String text = (String)args[0];
                String textColour = (String)args[1];
                String backgroundColour = (String)args[2];
                if( textColour.length() != text.length() || backgroundColour.length() != text.length() )
                {
                    throw new LuaException( "Arguments must be the same length" );
                }

                Terminal terminal = m_monitor.getTerminal().getTerminal();
                terminal.blit( text, textColour, backgroundColour );
                terminal.setCursorPos( terminal.getCursorX() + text.length(), terminal.getCursorY() );
                return null;
            }
            case 20:
            case 21:
            {
                // setPaletteColour/setPaletteColor
                Terminal terminal = m_monitor.getTerminal().getTerminal();

                if(args.length == 2 && args[0] instanceof Double && args[1] instanceof Double)
                {
                    int colour = 15 - dan200.computercraft.core.apis.TermAPI.parseColour( args );
                    int hex = ((Double)args[1]).intValue();
                    double[] rgb = Palette.decodeRGB8( hex );
                    dan200.computercraft.core.apis.TermAPI.setColour( terminal, colour, rgb[0], rgb[1], rgb[2] );
                    return null;
                }

                if (args.length >= 4 && args[0] instanceof Double && args[1] instanceof Double && args[2] instanceof Double && args[3] instanceof Double)
                {
                    int colour = 15 - dan200.computercraft.core.apis.TermAPI.parseColour( args );
                    double r = (Double)args[1];
                    double g = (Double)args[2];
                    double b = (Double)args[3];
                    dan200.computercraft.core.apis.TermAPI.setColour( terminal, colour, r, g, b );
                    return null;
                }

                throw new LuaException( "Expected number, number, number, number" );
            }
            case 22:
            case 23:
            {
                // getPaletteColour/getPaletteColor
                Terminal terminal = m_monitor.getTerminal().getTerminal();
                Palette palette = terminal.getPalette();

                int colour = 15 - dan200.computercraft.core.apis.TermAPI.parseColour( args );

                if( palette != null )
                {
                    return ArrayUtils.toObject( palette.getColour( colour ) );
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public void attach( @Nonnull IComputerAccess computer )
    {
        m_monitor.addComputer( computer );
    }

    @Override
    public void detach( @Nonnull IComputerAccess computer )
    {
        m_monitor.removeComputer( computer );
    }

    @Override
    public boolean equals( IPeripheral other )
    {
        if( other != null && other instanceof MonitorPeripheral )
        {
            MonitorPeripheral otherMonitor = (MonitorPeripheral)other;
            if( otherMonitor.m_monitor == this.m_monitor )
            {
                return true;
            }
        }
        return false;
    }
}
