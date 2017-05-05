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

    public NBTTagCompound writeToNBT( NBTTagCompound nbt )
    {
        for(int i = 0; i < colours.length; ++i)
        {
            String prefix = "term_palette_colour_" + i;
            nbt.setFloat( prefix + "_r", colours[i][0] );
            nbt.setFloat( prefix + "_g", colours[i][1] );
            nbt.setFloat( prefix + "_b", colours[i][2] );
        }
        return nbt;
    }

    public void readFromNBT( NBTTagCompound nbt )
    {
        for(int i = 0; i < colours.length; ++i)
        {
            String prefix = "term_palette_colour_" + i;
            colours[i][0] = nbt.getFloat( prefix + "_r" );
            colours[i][1] = nbt.getFloat( prefix + "_g" );
            colours[i][2] = nbt.getFloat( prefix + "_b" );
        }
    }
}
