package dan200.computercraft.shared.util;

public class StringUtil
{
    public static String normaliseLabel( String label )
    {
        if( label == null ) return null;

        int length = Math.min( 32, label.length() );
        StringBuilder builder = new StringBuilder( length );
        for (int i = 0; i < length; i++)
        {
            char c = label.charAt( i );
            if( (c >= ' ' && c <= '~') || (c >= 161 && c <= 172) || (c >= 174 && c <= 255) )
            {
                builder.append( c );
            }
            else
            {
                builder.append( '?' );
            }
        }

        return builder.toString();
    }

    /**
     * Translates a Stat name
     */
    @SuppressWarnings("deprecation")
    public static String translateToLocal( String key )
    {
        return net.minecraft.util.text.translation.I18n.translateToLocal( key );
    }

    /**
     * Translates a Stat name with format args
     */
    @SuppressWarnings("deprecation")
    public static String translateToLocalFormatted( String key, Object... format )
    {
        return net.minecraft.util.text.translation.I18n.translateToLocalFormatted( key, format );
    }

}
