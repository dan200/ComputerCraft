/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import dan200.computercraft.ComputerCraft;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPRequest
{
    public static URL checkURL( String urlString ) throws HTTPRequestException
    {
        URL url;
        try
        {
            url = new URL( urlString );
        }
        catch( MalformedURLException e )
        {
            throw new HTTPRequestException( "URL malformed" );
        }

        // Validate the URL
        String protocol = url.getProtocol().toLowerCase();
        if( !protocol.equals("http") && !protocol.equals("https") )
        {
            throw new HTTPRequestException( "URL not http" );
        }

        // Compare the URL to the whitelist
        if( !ComputerCraft.http_whitelist.matches( url.getHost() ) || ComputerCraft.http_blacklist.matches( url.getHost() ) )
        {
            throw new HTTPRequestException( "Domain not permitted" );
        }

        return url;
    }

    public HTTPRequest( String url, final String postText, final Map<String, String> headers, boolean binary ) throws HTTPRequestException
    {
        // Parse the URL
        m_urlString = url;
        m_url = checkURL( m_urlString );
        m_binary = binary;

        // Start the thread
        m_cancelled = false;
        m_complete = false;
        m_success = false;
        m_result = null;
        m_responseCode = -1;

        Thread thread = new Thread( () ->
        {
            try
            {
                // Connect to the URL
                HttpURLConnection connection = (HttpURLConnection)m_url.openConnection();

                if( postText != null )
                {
                    connection.setRequestMethod( "POST" );
                    connection.setDoOutput( true );
                }
                else
                {
                    connection.setRequestMethod( "GET" );
                }

                // Set headers
                connection.setRequestProperty( "accept-charset", "UTF-8" );
                if( postText != null )
                {
                    connection.setRequestProperty( "content-type", "application/x-www-form-urlencoded; charset=utf-8" );
                    connection.setRequestProperty( "content-encoding", "UTF-8" );
                }
                if( headers != null )
                {
                    for( Map.Entry<String, String> header : headers.entrySet() )
                    {
                        connection.setRequestProperty( header.getKey(), header.getValue() );
                    }
                }

                // Send POST text
                if( postText != null )
                {
                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw;
                    try
                    {
                        osw = new OutputStreamWriter( os, "UTF-8" );
                    }
                    catch( UnsupportedEncodingException e )
                    {
                        osw = new OutputStreamWriter( os );
                    }
                    BufferedWriter writer = new BufferedWriter( osw );
                    writer.write( postText, 0, postText.length() );
                    writer.close();
                }

                // Read response
                InputStream is;
                int code = connection.getResponseCode();
                boolean responseSuccess;
                if (code >= 200 && code < 400) {
                    is = connection.getInputStream();
                    responseSuccess = true;
                } else {
                    is = connection.getErrorStream();
                    responseSuccess = false;
                }
                
                byte[] result = ByteStreams.toByteArray( is );
                is.close();
                
                synchronized( m_lock )
                {
                    if( m_cancelled )
                    {
                        // We cancelled
                        m_complete = true;
                        m_success = false;
                        m_result = null;
                    }
                    else
                    {
                        // We completed
                        m_complete = true;
                        m_success = responseSuccess;
                        m_result = result;
                        m_responseCode = connection.getResponseCode();
                        m_encoding = connection.getContentEncoding();

                        Joiner joiner = Joiner.on( ',' );
                        Map<String, String> headers1 = m_responseHeaders = new HashMap<>();
                        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                            headers1.put(header.getKey(), joiner.join( header.getValue() ));
                        }
                    }
                }

                connection.disconnect(); // disconnect

            }
            catch( IOException e )
            {
                synchronized( m_lock )
                {
                    // There was an error
                    m_complete = true;
                    m_success = false;
                    m_result = null;
                }
            }
        } );
        thread.setDaemon(true);
        thread.start();
    }
    
    public String getURL() {
        return m_urlString;
    }
    
    public void cancel()
    {
        synchronized(m_lock) {
            m_cancelled = true;
        }
    }
    
    public boolean isComplete()
    {
        synchronized(m_lock) {
            return m_complete;
        }
    }

    public int getResponseCode() {
        synchronized(m_lock) {
            return m_responseCode;
        }
    }

    public Map<String, String> getResponseHeaders() {
        synchronized (m_lock) {
            return m_responseHeaders;
        }
    }

    public boolean wasSuccessful()
    {
        synchronized(m_lock) {
            return m_success;
        }
    }

    public boolean isBinary()
    {
        return m_binary;
    }
    
    public InputStream getContents()
    {
        byte[] result;
        synchronized(m_lock) {
            result = m_result;
        }
        
        if( result != null ) {
            return new ByteArrayInputStream( result );
        }
        return null;
    }
    
    public String getEncoding() {
        return m_encoding;
    }
    
    private final Object m_lock = new Object();
    private final URL m_url;
    private final String m_urlString;
    
    private boolean m_complete;
    private boolean m_cancelled;
    private boolean m_success;
    private String m_encoding;
    private byte[] m_result;
    private boolean m_binary;
    private int m_responseCode;
    private Map<String, String> m_responseHeaders;
}
