package dan200.computercraft.shared.util;

import java.util.regex.Pattern;

public class StringUtil
{
    private static final Pattern INVALID_PATTERN = Pattern.compile( "[^ -~]" );

    public static String normaliseLabel( String label )
    {
        label = INVALID_PATTERN.matcher( label ).replaceAll( "" );

        if( label.length() > 32 )
        {
            label = label.substring( 0, 32 );
        }

        return label;
    }
}
