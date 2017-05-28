/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.util.Palette;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public class TermAPI implements ILuaAPI
{
    private final Terminal m_terminal;
    private final IComputerEnvironment m_environment;

    public TermAPI( IAPIEnvironment _environment )
    {
        m_terminal = _environment.getTerminal();
        m_environment = _environment.getComputerEnvironment();
    }
    
    @Override
    public String[] getNames()
    {
        return new String[] {
            "term"
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
            "write",
            "scroll",
            "setCursorPos",
            "setCursorBlink",
            "getCursorPos",
            "getSize",
            "clear",
            "clearLine",
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
    
    public static int parseColour( Object[] args ) throws LuaException
    {
        if( args.length < 1 || args[0] == null || !(args[0] instanceof Double) )
        {
            throw new LuaException( "Expected number" );
        }            
        int colour = (int)((Double)args[0]).doubleValue();
        if( colour <= 0 )
        {
            throw new LuaException( "Colour out of range" );
        }
        colour = getHighestBit( colour ) - 1;
        if( colour < 0 || colour > 15 )
        {
            throw new LuaException( "Colour out of range" );
        }
        return colour;
    }

    public static Object[] encodeColour( int colour ) throws LuaException
    {
        return new Object[] {
            1 << colour
        };
    }

    public static void setColour( Terminal terminal, int colour, double r, double g, double b )
    {
        if( terminal.getPalette() != null )
        {
            terminal.getPalette().setColour( colour, r, g, b );
            terminal.setChanged();
        }
    }

    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException
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
                
                synchronized( m_terminal )
                {
                    m_terminal.write( text );
                    m_terminal.setCursorPos( m_terminal.getCursorX() + text.length(), m_terminal.getCursorY() );
                }
                return null;
            }
            case 1:
            {
                // scroll
                if( args.length != 1 || args[0] == null || !(args[0] instanceof Double) )
                {
                    throw new LuaException( "Expected number" );
                }
                
                int y = (int)((Double)args[0]).doubleValue();
                synchronized( m_terminal )
                {
                    m_terminal.scroll(y);
                }
                return null;
            }
            case 2:
            {
                // setCursorPos
                if( args.length != 2 || args[0] == null || !(args[0] instanceof Double) || args[1] == null || !(args[1] instanceof Double) )
                {
                    throw new LuaException( "Expected number, number" );
                }
                int x = (int)((Double)args[0]).doubleValue() - 1;
                int y = (int)((Double)args[1]).doubleValue() - 1;
                synchronized( m_terminal )
                {
                    m_terminal.setCursorPos( x, y );
                }
                return null;
            }
            case 3:
            {
                // setCursorBlink
                if( args.length != 1 || args[0] == null || !(args[0] instanceof Boolean) )
                {
                    throw new LuaException( "Expected boolean" );
                }
                boolean b = (Boolean) args[ 0 ];
                synchronized( m_terminal )
                {
                    m_terminal.setCursorBlink( b );
                }
                return null;
            }
            case 4:
            {
                // getCursorPos
                int x, y;
                synchronized( m_terminal )
                {
                    x = m_terminal.getCursorX();
                    y = m_terminal.getCursorY();
                }
                return new Object[] { x + 1, y + 1 };
            }
            case 5:
            {
                // getSize
                int width, height;
                synchronized( m_terminal )
                {
                    width = m_terminal.getWidth();
                    height = m_terminal.getHeight();
                }                
                return new Object[] { width, height };
            }
            case 6:
            {
                // clear
                synchronized( m_terminal )
                {
                    m_terminal.clear();
                }
                return null;
            }
            case 7:
            {
                // clearLine
                synchronized( m_terminal )
                {
                    m_terminal.clearLine();
                }
                return null;
            }
            case 8:
            case 9:
            {
                // setTextColour/setTextColor
                int colour = parseColour( args );
                synchronized( m_terminal )
                {
                    m_terminal.setTextColour( colour );
                }
                return null;
            }
            case 10:
            case 11:
            {
                // setBackgroundColour/setBackgroundColor
                int colour = parseColour( args );
                synchronized( m_terminal )
                {
                    m_terminal.setBackgroundColour( colour );
                }
                return null;
            }
            case 12:
            case 13:
            {
                // isColour/isColor
                return new Object[] { m_environment.isColour() };
            }
            case 14:
            case 15:
            {
                // getTextColour/getTextColor
                return encodeColour( m_terminal.getTextColour() );
            }
            case 16:
            case 17:
            {
                // getBackgroundColour/getBackgroundColor
                return encodeColour( m_terminal.getBackgroundColour() );
            }
            case 18:
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

                synchronized( m_terminal )
                {
                    m_terminal.blit( text, textColour, backgroundColour );
                    m_terminal.setCursorPos( m_terminal.getCursorX() + text.length(), m_terminal.getCursorY() );
                }
                return null;
            }
            case 19:
            case 20:
            {
                // setPaletteColour/setPaletteColor
                if(args.length == 2 && args[0] instanceof Double && args[1] instanceof Double)
                {
                    int colour = 15 - parseColour( args );
                    int hex = ((Double)args[1]).intValue();
                    double[] rgb = Palette.decodeRGB8( hex );
                    setColour( m_terminal, colour, rgb[0], rgb[1], rgb[2] );
                    return null;
                }

                if(args.length >= 4 && args[0] instanceof Double && args[1] instanceof Double && args[2] instanceof Double && args[3] instanceof Double)
                {
                    int colour = 15 - parseColour( args );
                    double r = (Double)args[1];
                    double g = (Double)args[2];
                    double b = (Double)args[3];
                    setColour( m_terminal, colour, r, g, b );
                    return null;
                }

                throw new LuaException( "Expected number, number or number, number, number, number" );
            }
            case 21:
            case 22:
            {
                // getPaletteColour/getPaletteColor
                if(args.length < 1 || !(args[0] instanceof Double))
                {
                    throw new LuaException( "Expected number" );
                }

                int colour = 15 - parseColour( args );
                synchronized( m_terminal )
                {
                    if ( m_terminal.getPalette() != null )
                    {
                        return ArrayUtils.toObject( m_terminal.getPalette().getColour( colour ) );
                    }
                }
                return null;
            }
            default:
            {
                return null;
            }
        }
    }
    
    private static int getHighestBit( int group )
    {
        int bit = 0;
        while( group > 0 )
        {
            group >>= 1;
            bit++;
        }
        return bit;
    }
}
