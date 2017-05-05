package dan200.computercraft.shared.util;

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

    public void resetColours()
    {
        for(int i = 0; i < Colour.values().length; ++i)
        {
            Colour c = Colour.values()[ i ];
            colours[i] = new PaletteColour( c.getR(), c.getG(), c.getB() );
        }
    }
}
