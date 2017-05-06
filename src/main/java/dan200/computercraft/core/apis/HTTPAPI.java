/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class HTTPAPI implements ILuaAPI
{
    private IAPIEnvironment m_apiEnvironment;
    private List<HTTPRequest> m_httpRequests;
    
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
                        final BufferedReader contents = h.getContents();
                        final Object result = wrapBufferedReader( contents, h.getResponseCode(), h.getResponseHeaders() );
                        m_apiEnvironment.queueEvent( "http_success", new Object[] { url, result } );
                    } else {
                        // Queue the "http_failure" event
                        BufferedReader contents = h.getContents();
                        Object result = null;
                        if( contents != null ) {
                            result = wrapBufferedReader( contents, h.getResponseCode(), h.getResponseHeaders() );
                        }
                        m_apiEnvironment.queueEvent( "http_failure", new Object[]{ url, "Could not connect", result } );
                    }
                    it.remove();
                }
            }
        }
    }
    
    private static ILuaObject wrapBufferedReader( final BufferedReader reader, final int responseCode, final Map<String, String> responseHeaders )
    {
        return new ILuaObject() {
            @Nonnull
            @Override
            public String[] getMethodNames()
            {
                return new String[] {
                    "readLine",
                    "readAll",
                    "close",
                    "getResponseCode",
                    "getResponseHeaders",
                };
            }
            
            @Override
            public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException
            {
                switch( method )
                {
                    case 0:
                    {
                        // readLine
                        try {
                            String line = reader.readLine();
                            if( line != null ) {
                                return new Object[] { line };
                            } else {
                                return null;
                            }
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    case 1:
                    {
                        // readAll
                        try {
                            StringBuilder result = new StringBuilder( "" );
                            String line = reader.readLine();
                            while( line != null ) {
                                result.append( line );
                                line = reader.readLine();
                                if( line != null ) {
                                    result.append( "\n" );
                                }
                            }
                            return new Object[] { result.toString() };
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    case 2:
                    {
                        // close
                        try {
                            reader.close();
                            return null;
                        } catch( IOException e ) {
                            return null;
                        }
                    }
                    case 3:
                    {
                        // getResponseCode
                        return new Object[] { responseCode };
                    }
                    case 4:
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
            Iterator<HTTPRequest> it = m_httpRequests.iterator();
            while( it.hasNext() ) {
                HTTPRequest r = it.next();
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
                if( args.length < 1 || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String urlString = args[0].toString();

                // Get POST
                String postString = null;
                if( args.length >= 2 && args[1] instanceof String )
                {
                    postString = args[1].toString();
                }

                // Get Headers
                Map<String, String> headers = null;
                if( args.length >= 3 && args[2] instanceof Map )
                {
                    Map table = (Map)args[2];
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

                // Make the request
                try
                {
                    HTTPRequest request = new HTTPRequest( urlString, postString, headers );
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
                if( args.length < 1 || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                String urlString = args[0].toString();

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
