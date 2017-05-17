package dan200.computercraft.core.apis.handles;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import java.io.*;

public class EncodedOutputHandle extends HandleGeneric
{
    private final BufferedWriter m_writer;

    public EncodedOutputHandle( BufferedWriter writer )
    {
        super( writer );
        this.m_writer = writer;
    }

    public EncodedOutputHandle( OutputStream stream )
    {
        this( stream, "UTF-8" );
    }

    public EncodedOutputHandle( OutputStream stream, String encoding )
    {
        this( makeWriter( stream, encoding ) );
    }

    private static BufferedWriter makeWriter( OutputStream stream, String encoding )
    {
        if( encoding == null ) encoding = "UTF-8";
        OutputStreamWriter streamWriter;
        try
        {
            streamWriter = new OutputStreamWriter( stream, encoding );
        }
        catch( UnsupportedEncodingException e )
        {
            streamWriter = new OutputStreamWriter( stream );
        }
        return new BufferedWriter( streamWriter );
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "write",
            "writeLine",
            "flush",
            "close",
        };
    }

    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException
    {
        switch( method )
        {
            case 0:
            {
                // write
                checkOpen();
                String text;
                if( args.length > 0 && args[ 0 ] != null )
                {
                    text = args[ 0 ].toString();
                }
                else
                {
                    text = "";
                }
                try
                {
                    m_writer.write( text, 0, text.length() );
                    return null;
                }
                catch( IOException e )
                {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 1:
            {
                // writeLine
                checkOpen();
                String text;
                if( args.length > 0 && args[ 0 ] != null )
                {
                    text = args[ 0 ].toString();
                }
                else
                {
                    text = "";
                }
                try
                {
                    m_writer.write( text, 0, text.length() );
                    m_writer.newLine();
                    return null;
                }
                catch( IOException e )
                {
                    throw new LuaException( e.getMessage() );
                }
            }
            case 2:
                // flush
                checkOpen();
                try
                {
                    m_writer.flush();
                    return null;
                }
                catch( IOException e )
                {
                    return null;
                }
            case 3:
                // close
                close();
                return null;
            default:
                return null;
        }
    }
}
