package dan200.computercraft.core.apis.handles;

import dan200.computercraft.api.lua.ICallContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;

import javax.annotation.Nonnull;
import java.io.*;

import static dan200.computercraft.core.apis.ArgumentHelper.optInt;

public class EncodedInputHandle extends HandleGeneric
{
    private final BufferedReader m_reader;

    public EncodedInputHandle( BufferedReader reader )
    {
        super( reader );
        this.m_reader = reader;
    }

    public EncodedInputHandle( InputStream stream )
    {
        this( stream, "UTF-8" );
    }

    public EncodedInputHandle( InputStream stream, String encoding )
    {
        this( makeReader( stream, encoding ) );
    }

    private static BufferedReader makeReader( InputStream stream, String encoding )
    {
        if( encoding == null ) encoding = "UTF-8";
        InputStreamReader streamReader;
        try
        {
            streamReader = new InputStreamReader( stream, encoding );
        }
        catch( UnsupportedEncodingException e )
        {
            streamReader = new InputStreamReader( stream );
        }
        return new BufferedReader( streamReader );
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "readLine",
            "readAll",
            "close",
            "read",
        };
    }

    @Nonnull
    @Override
    public MethodResult callMethod( @Nonnull ICallContext context, int method, @Nonnull Object[] args ) throws LuaException
    {
        switch( method )
        {
            case 0:
                // readLine
                checkOpen();
                try
                {
                    String line = m_reader.readLine();
                    if( line != null )
                    {
                        return MethodResult.of( line );
                    }
                    else
                    {
                        return MethodResult.empty();
                    }
                }
                catch( IOException e )
                {
                    return MethodResult.empty();
                }
            case 1:
                // readAll
                checkOpen();
                try
                {
                    StringBuilder result = new StringBuilder( "" );
                    String line = m_reader.readLine();
                    while( line != null )
                    {
                        result.append( line );
                        line = m_reader.readLine();
                        if( line != null )
                        {
                            result.append( "\n" );
                        }
                    }
                    return MethodResult.of( result.toString() );
                }
                catch( IOException e )
                {
                    return MethodResult.empty();
                }
            case 2:
                // close
                close();
                return MethodResult.empty();
            case 3:
                // read
                checkOpen();
                try
                {
                    int count = optInt( args, 0, 1 );
                    if( count <= 0 || count >= 1024 * 16 )
                    {
                        throw new LuaException( "Count out of range" );
                    }
                    char[] bytes = new char[ count ];
                    count = m_reader.read( bytes );
                    if( count < 0 ) return MethodResult.empty();
                    String str = new String( bytes, 0, count );
                    return MethodResult.of( str );
                }
                catch( IOException e )
                {
                    return MethodResult.empty();
                }
            default:
                return MethodResult.empty();
        }
    }
}
