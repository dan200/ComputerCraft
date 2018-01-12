package dan200.computercraft.shared.util;

/**
 * A reimplementation of the colour system in {@link net.minecraft.item.crafting.RecipesArmorDyes}, but
 * bundled together as an object.
 */
public class ColourTracker
{
    private int total;
    private int totalR;
    private int totalG;
    private int totalB;
    private int count;

    public void addColour( int r, int g, int b )
    {
        total += Math.max( r, Math.max( g, b ) );
        totalR += r;
        totalG += g;
        totalB += b;
        count++;
    }

    public void addColour( float r, float g, float b )
    {
        addColour( (int) (r * 255), (int) (g * 255), (int) (b * 255) );
    }

    public boolean hasColour()
    {
        return count > 0;
    }

    public int getColour()
    {
        int avgR = totalR / count;
        int avgG = totalG / count;
        int avgB = totalB / count;

        float avgTotal = (float) total / (float) count;
        float avgMax = Math.max( avgR, Math.max( avgG, avgB ) );
        avgR = (int) (avgR * avgTotal / avgMax);
        avgG = (int) (avgG * avgTotal / avgMax);
        avgB = (int) (avgB * avgTotal / avgMax);

        return (avgR << 16) | (avgG << 8) | avgB;
    }
}
