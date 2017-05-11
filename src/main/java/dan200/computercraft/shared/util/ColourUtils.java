package dan200.computercraft.shared.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

public class ColourUtils
{
    private static final String[] DYES = new String[] {
        "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown",
        "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray",
        "dyeGray", "dyePink", "dyeLime", "dyeYellow",
        "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite"
    };

    private static int[] ids;

    public static int getStackColour( ItemStack stack )
    {
        if( ids == null )
        {
            int ids[] = ColourUtils.ids = new int[ DYES.length ];
            for( int i = 0; i < DYES.length; i++ )
            {
                ids[ i ] = OreDictionary.getOreID( DYES[ i ] );
            }
        }

        for( int id : OreDictionary.getOreIDs( stack ) )
        {
            int index = ArrayUtils.indexOf( ids, id );
            if( index >= 0 ) return index;
        }

        return -1;
    }
}
