package dan200.computercraft.core.apis.handles;

import com.google.common.io.ByteStreams;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static dan200.computercraft.core.apis.ArgumentHelper.getInt;

public class BinaryInputHandle extends HandleGeneric
{
    private final InputStream m_stream;

    public BinaryInputHandle( InputStream reader )
    {
        super( reader );
        this.m_stream = reader;
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "read",
            "readAll",
            "close",
        };
    }

    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException
    {
        switch( method )
        {
            case 0:
                // read
                checkOpen();
                try
                {
                    if( args.length > 0 && args[ 0 ] != null )
                    {
                        int count = getInt( args, 0 );
                        if( count <= 0 || count >= 1024 * 16 )
                        {
                            throw new LuaException( "Count out of range" );
                        }

                        byte[] bytes = new byte[ count ];
                        count = m_stream.read( bytes );
                        if( count < 0 ) return null;
                        if( count < bytes.length ) bytes = Arrays.copyOf( bytes, count );
                        return new Object[] { bytes };
                    }
                    else
                    {
                        int b = m_stream.read();
                        return b == -1 ? null : new Object[] { b };
                    }
                }
                catch( IOException e )
                {
                    return null;
                }
            case 1:
                // readAll
                checkOpen();
                try
                {
                    byte[] out = ByteStreams.toByteArray( m_stream );
                    return out == null ? null : new Object[] { out };
                }
                catch( IOException e )
                {
                    return null;
                }
            case 2:
                //close
                close();
                return null;
            default:
                return null;
        }
    }
}
