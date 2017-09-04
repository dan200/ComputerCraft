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
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.Palette;

import javax.annotation.Nonnull;

import static dan200.computercraft.core.apis.ArgumentHelper.*;

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
            "getPaletteColor",
            "nativePaletteColor",
            "nativePaletteColour"
        };
    }
    
    public static int parseColour( Object[] args ) throws LuaException
    {
        int colour = getInt( args, 0 );
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
                int y = getInt( args, 0 );
                synchronized( m_terminal )
                {
                    m_terminal.scroll(y);
                }
                return null;
            }
            case 2:
            {
                // setCursorPos
                int x = getInt( args, 0 ) - 1;
                int y = getInt( args, 1 ) - 1;
                synchronized( m_terminal )
                {
                    m_terminal.setCursorPos( x, y );
                }
                return null;
            }
            case 3:
            {
                // setCursorBlink
                boolean b = getBoolean( args, 0 );
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
                String text = getString( args, 0 );
                String textColour = getString( args, 1 );
                String backgroundColour = getString( args, 2 );
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
                int colour = 15 - parseColour( args );
                if( args.length == 2 )
                {
                    int hex = getInt( args, 1 );
                    double[] rgb = Palette.decodeRGB8( hex );
                    setColour( m_terminal, colour, rgb[ 0 ], rgb[ 1 ], rgb[ 2 ] );
                }
                else
                {
                    int r = getInt( args, 1 );
                    int g = getInt( args, 2 );
                    int b = getInt( args, 3 );
                    setColour( m_terminal, colour, r / 255.0, g / 255.0, b / 255.0 );
                }
                return null;
            }
            case 21:
            case 22:
            {
                // getPaletteColour/getPaletteColor
                int colour = 15 - parseColour( args );
                synchronized( m_terminal )
                {
                    if ( m_terminal.getPalette() != null )
                    {
                        Palette palette = m_terminal.getPalette();
                        double[] colours = palette.getColour( colour );
                        Object[] colours8 = new Object[ colours.length ];

                        for ( int i = 0; i < colours8.length; ++i )
                        {
                            colours8[ i ] = (int) ( colours[ i ] * 255.0 );
                        }

                        return colours8;
                    }
                }
                return null;
            }
            case 23:
            case 24:
            {
                // nativePaletteColour/nativePaletteColor
                int colour = 15 - parseColour( args );
                Colour c = Colour.fromInt( colour );
                if ( c != null )
                {
                    float[] rgb = c.getRGB();
                    Object[] rgb8 = new Object[ rgb.length ];

                    for ( int i = 0; i < rgb8.length; ++i )
                    {
                        rgb8[i] = (int) ( rgb[i] * 255.0f );
                    }

                    return rgb8;
                }
                else
                {
                    return null;
                }
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
