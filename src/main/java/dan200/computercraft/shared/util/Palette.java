package dan200.computercraft.shared.util;

import net.minecraft.nbt.NBTTagCompound;

public class Palette
{
    private static final int PALETTE_SIZE = 16;
    private final float[][] colours = new float[PALETTE_SIZE][3];

    public Palette()
    {
        // Get the default palette
        resetColours();
    }

    public void setColour(int i, float r, float g, float b)
    {
        if( i >= 0 && i < colours.length )
        {
            colours[i][0] = r;
            colours[i][1] = g;
            colours[i][2] = b;
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
            return colours[i];
        }
        return null;
    }

    public void resetColour( int i )
    {
        if( i >= 0 && i < colours.length )
        {
            setColour( i, Colour.values()[i] );
        }
    }

    public void resetColours()
    {
        for(int i = 0; i < Colour.values().length; ++i)
        {
            resetColour( i );
        }
    }

    private static int encodeRGB8( float[] rgb )
    {
        int r = (int)( rgb[0] * 255 ) & 0xFF;
        int g = (int)( rgb[1] * 255 ) & 0xFF;
        int b = (int)( rgb[2] * 255 ) & 0xFF;

        return ( r << 16 ) | ( g << 8 ) | b;
    }

    private static float[] decodeRGB8( int rgb )
    {
        return new float[]
        {
            (( rgb >> 16 ) & 0xFF) / 255.0f,
            (( rgb >> 8 ) & 0xFF) / 255.0f,
            ( rgb & 0xFF ) / 255.0f
        };
    }

    public NBTTagCompound writeToNBT( NBTTagCompound nbt )
    {
        int[] rgb8 = new int[colours.length];

        for(int i = 0; i < colours.length; ++i)
        {
            rgb8[i] = encodeRGB8( colours[i] );
        }

        nbt.setIntArray( "term_palette", rgb8 );
        return nbt;
    }

    public void readFromNBT( NBTTagCompound nbt )
    {
        int[] rgb8 = nbt.getIntArray( "term_palette" );

        for(int i = 0; i < colours.length; ++i)
        {
            colours[i] = decodeRGB8( rgb8[i] );
        }
    }
}
