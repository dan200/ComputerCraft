package dan200.computercraft.shared.util;

import net.minecraft.nbt.NBTTagCompound;

public class Palette
{
    private static class PaletteColour
    {
        private float m_r, m_g, m_b;

        public PaletteColour(float r, float g, float b)
        {
            m_r = r;
            m_g = g;
            m_b = b;
        }

        @Override
        public boolean equals(Object o)
        {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            PaletteColour that = (PaletteColour) o;

            if(Float.compare( that.m_r, m_r ) != 0) return false;
            if(Float.compare( that.m_g, m_g ) != 0) return false;
            return Float.compare( that.m_b, m_b ) == 0;
        }

        @Override
        public int hashCode()
        {
            int result = (m_r != +0.0f ? Float.floatToIntBits( m_r ) : 0);
            result = 31 * result + (m_g != +0.0f ? Float.floatToIntBits( m_g ) : 0);
            result = 31 * result + (m_b != +0.0f ? Float.floatToIntBits( m_b ) : 0);
            return result;
        }
    }

    private static final int PALETTE_SIZE = 16;
    private final PaletteColour[] colours = new PaletteColour[ PALETTE_SIZE ];

    public Palette()
    {
        // Get the default palette
        resetColours();
    }

    public void setColour(int i, float r, float g, float b)
    {
        if( i >= 0 && i < colours.length )
        {
            colours[ i ] = new PaletteColour( r, g, b );
        }
    }

    public void setColour(int i, Colour colour)
    {
        setColour( i, colour.getR(), colour.getG(), colour.getB() );
    }

    public float[] getColour( int i )
    {
        if( i >= 0 && i < colours.length )
        {
            PaletteColour c = colours[ i ];
            return new float[] { c.m_r, c.m_g, c.m_b };
        }
        return null;
    }

    public void resetColour( int i )
    {
        if(i >= 0 && i < colours.length )
        {
            setColour( i, Colour.values()[ i ] );
        }
    }

    public void resetColours()
    {
        for(int i = 0; i < Colour.values().length; ++i)
        {
            resetColour( i );
        }
    }

    public NBTTagCompound writeToNBT( NBTTagCompound nbt )
    {
        for(int i = 0; i < colours.length; ++i)
        {
            PaletteColour c = colours[i];
            String prefix = "term_palette_colour_" + i;
            nbt.setFloat( prefix + "_r", c.m_r );
            nbt.setFloat( prefix + "_g", c.m_g );
            nbt.setFloat( prefix + "_b", c.m_b );
        }
        return nbt;
    }

    public void readFromNBT( NBTTagCompound nbt )
    {
        for(int i = 0; i < colours.length; ++i)
        {
            String prefix = "term_palette_colour_" + i;
            colours[i].m_r = nbt.getFloat( prefix + "_r" );
            colours[i].m_g = nbt.getFloat( prefix + "_g" );
            colours[i].m_b = nbt.getFloat( prefix + "_b" );
        }
    }
}
