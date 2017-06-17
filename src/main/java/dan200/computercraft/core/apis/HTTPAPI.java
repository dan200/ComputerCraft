/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.handles.BinaryInputHandle;
import dan200.computercraft.core.apis.handles.EncodedInputHandle;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.*;

import static dan200.computercraft.core.apis.ArgumentHelper.*;

public class HTTPAPI implements ILuaAPI
{
    private final IAPIEnvironment m_apiEnvironment;
    private final List<HTTPRequest> m_httpRequests;
    
    public HTTPAPI( IAPIEnvironment environment )
    {
        m_apiEnvironment = environment;
        m_httpRequests = new ArrayList<HTTPRequest>();
    }
    
    @Override
    public String[] getNames()
    {
        return new String[] {
            "http"
        };
    }

    @Override
    public void startup( )
    {
    }

    @Override
    public void advance( double _dt )
    {
        // Wait for all of our http requests 
        synchronized( m_httpRequests )
        {
            Iterator<HTTPRequest> it = m_httpRequests.iterator();
            while( it.hasNext() ) {
                final HTTPRequest h = it.next();
                if( h.isComplete() ) {
                    final String url = h.getURL();
                    if( h.wasSuccessful() ) {
                        // Queue the "http_success" event
                        InputStream contents = h.getContents();
                        Object result = wrapStream(
                            h.isBinary() ? new BinaryInputHandle( contents ) : new EncodedInputHandle( contents, h.getEncoding() ),
                            h.getResponseCode(), h.getResponseHeaders()
                        );
                        m_apiEnvironment.queueEvent( "http_success", new Object[] { url, result } );
                    } else {
                        // Queue the "http_failure" event
                        InputStream contents = h.getContents();
                        Object result = null;
                        if( contents != null ) {
                            result = wrapStream(
                                h.isBinary() ? new BinaryInputHandle( contents ) : new EncodedInputHandle( contents, h.getEncoding() ),
                                h.getResponseCode(), h.getResponseHeaders()
                            );
                        }
                        m_apiEnvironment.queueEvent( "http_failure", new Object[]{ url, "Could not connect", result } );
                    }
                    it.remove();
                }
            }
        }
    }

    private static ILuaObject wrapStream( final ILuaObject reader, final int responseCode, final Map<String, String> responseHeaders )
    {
        String[] oldMethods = reader.getMethodNames();
        final int methodOffset = oldMethods.length;

        final String[] newMethods = Arrays.copyOf( oldMethods, oldMethods.length + 2 );
        newMethods[ methodOffset + 0 ] = "getResponseCode";
        newMethods[ methodOffset + 1 ] = "getResponseHeaders";

        return new ILuaObject()
        {
            @Nonnull
            @Override
            public String[] getMethodNames()
            {
                return newMethods;
            }

            @Override
            public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException, InterruptedException
            {
                if( method < methodOffset )
                {
                    return reader.callMethod( context, method, args );
                }
                switch( method - methodOffset )
                {
                    case 0:
                    {
                        // getResponseCode
                        return new Object[] { responseCode };
                    }
                    case 1:
                    {
                        // getResponseHeaders
                        return new Object[] { responseHeaders };
                    }
                    default:
                    {
                        return null;
                    }
                }
            }
        };
    }
    
    @Override
    public void shutdown( )
    {
        synchronized( m_httpRequests )
        {
            for( HTTPRequest r : m_httpRequests )
            {
                r.cancel();
            }
            m_httpRequests.clear();
        }
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
         return new String[] {
            "request",
            "checkURL"
        };
    }

    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException
    {
        switch( method )
        {
            case 0:
            {
                // request
                // Get URL
                String urlString = getString( args, 0 );

                // Get POST
                String postString = optString( args, 1, null );

                // Get Headers
                Map<String, String> headers = null;
                Map<Object, Object> table = optTable( args, 2, null );
                if( table != null )
                {
                    headers = new HashMap<String, String>( table.size() );
                    for( Object key : table.keySet() )
                    {
                        Object value = table.get( key );
                        if( key instanceof String && value instanceof String )
                        {
                            headers.put( (String)key, (String)value );
                        }
                    }
                }
                
                // Get binary
                boolean binary = false;
                if( args.length >= 4 )
                {
                    binary = args[ 3 ] != null && !args[ 3 ].equals( Boolean.FALSE );
                }

                // Make the request
                try
                {
                    HTTPRequest request = new HTTPRequest( urlString, postString, headers, binary );
                    synchronized( m_httpRequests )
                    {
                        m_httpRequests.add( request );
                    }
                    return new Object[] { true };
                }
                catch( HTTPRequestException e )
                {
                    return new Object[] { false, e.getMessage() };
                }
            }
            case 1:
            {
                // checkURL
                // Get URL
                String urlString = getString( args, 0 );

                // Check URL
                try
                {
                    HTTPRequest.checkURL( urlString );
                    return new Object[] { true };
                }
                catch( HTTPRequestException e )
                {
                    return new Object[] { false, e.getMessage() };
                }
            }
            default:
            {
                return null;
            }
        }
    }
}
