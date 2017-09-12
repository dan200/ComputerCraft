/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.http.HTTPCheck;
import dan200.computercraft.core.apis.http.HTTPRequest;
import dan200.computercraft.core.apis.http.HTTPTask;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.*;

import static dan200.computercraft.core.apis.ArgumentHelper.*;

public class HTTPAPI implements ILuaAPI
{
    private final IAPIEnvironment m_apiEnvironment;
    private final List<HTTPTask> m_httpTasks;
    
    public HTTPAPI( IAPIEnvironment environment )
    {
        m_apiEnvironment = environment;
        m_httpTasks = new ArrayList<>();
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
        synchronized( m_httpTasks )
        {
            Iterator<HTTPTask> it = m_httpTasks.iterator();
            while( it.hasNext() )
            {
                final HTTPTask h = it.next();
                if( h.isFinished() )
                {
                    h.whenFinished( m_apiEnvironment );
                    it.remove();
                }
            }
        }
    }
    
    @Override
    public void shutdown( )
    {
        synchronized( m_httpTasks )
        {
            for( HTTPTask r : m_httpTasks )
            {
                r.cancel();
            }
            m_httpTasks.clear();
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
                    headers = new HashMap<>( table.size() );
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
                    URL url = HTTPRequest.checkURL( urlString );
                    HTTPRequest request = new HTTPRequest( urlString, url, postString, headers, binary );
                    synchronized( m_httpTasks )
                    {
                        m_httpTasks.add( HTTPTask.submit( request ) );
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
                    URL url = HTTPRequest.checkURL( urlString );
                    HTTPCheck check = new HTTPCheck( urlString, url );
                    synchronized( m_httpTasks ) {
                        m_httpTasks.add( HTTPTask.submit( check ) );
                    }
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
