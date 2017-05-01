/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.blocks;

import dan200.computercraft.shared.util.Colour;
import net.minecraft.util.IStringSerializable;

public enum BlockTurtleDyeVariant implements IStringSerializable
{
    None( "none", null ),
    Black( "black", Colour.Black ),
    Red( "red", Colour.Red ),
    Green( "green", Colour.Green ),
    Brown( "brown", Colour.Brown ),
    Blue( "blue", Colour.Blue ),
    Purple( "purple", Colour.Purple ),
    Cyan( "cyan", Colour.Cyan ),
    LightGrey( "light_grey", Colour.LightGrey ),
    Grey( "grey", Colour.Grey ),
    Pink( "pink", Colour.Pink ),
    Lime( "lime", Colour.Lime ),
    Yellow( "yellow", Colour.Yellow ),
    LightBlue( "light_blue", Colour.LightBlue ),
    Magenta( "magenta", Colour.Magenta ),
    Orange( "orange", Colour.Orange ),
    White( "white", Colour.Orange );

    public static BlockTurtleDyeVariant fromColour( Colour colour )
    {
        if( colour != null )
        {
            switch( colour )
            {
                case Black: return Black;
                case Red: return Red;
                case Green: return Green;
                case Brown: return Brown;
                case Blue: return Blue;
                case Purple: return Purple;
                case Cyan: return Cyan;
                case LightGrey: return LightGrey;
                case Grey: return Grey;
                case Pink: return Pink;
                case Lime: return Lime;
                case Yellow: return Yellow;
                case LightBlue: return LightBlue;
                case Magenta: return Magenta;
                case Orange: return Orange;
                case White: return White;
            }
        }
        return None;
    }

    private String m_name;
    private Colour m_colour;

    private BlockTurtleDyeVariant( String name, Colour colour )
    {
        m_name = name;
        m_colour = colour;
    }

    @Override
    public String getName()
    {
        return m_name;
    }

    public Colour getColour()
    {
        return m_colour;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
