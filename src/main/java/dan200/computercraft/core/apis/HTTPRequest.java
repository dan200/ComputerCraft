/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.ComputerCraft;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

class HTTPRequestException extends Exception {
    public HTTPRequestException( String s ) {
        super( s );
    }
}

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
        boolean allowed = false;
        String whitelistString = ComputerCraft.http_whitelist;
        String[] allowedURLs = whitelistString.split( ";" );
        for( int i=0; i<allowedURLs.length; ++i )
        {
            String allowedURL = allowedURLs[i];
            Pattern allowedURLPattern = Pattern.compile( "^\\Q" + allowedURL.replaceAll( "\\*", "\\\\E.*\\\\Q" ) + "\\E$" );
            if( allowedURLPattern.matcher( url.getHost() ).matches() )
            {
                allowed = true;
                break;
            }
        }
        if( !allowed )
        {
            throw new HTTPRequestException( "Domain not permitted" );
        }

        return url;
    }

    public HTTPRequest( String url, final String postText, final Map<String, String> headers ) throws HTTPRequestException
    {
        // Parse the URL
        m_urlString = url;
        m_url = checkURL( m_urlString );

        // Start the thread
        m_cancelled = false;
        m_complete = false;
        m_success = false;
        m_result = null;
        m_responseCode = -1;

        Thread thread = new Thread( new Runnable() {
            @Override
            public void run()
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
                    InputStream is = connection.getInputStream();
                    InputStreamReader isr;
                    try
                    {
                        String contentEncoding = connection.getContentEncoding();
                        if( contentEncoding != null )
                        {
                            try
                            {
                                isr = new InputStreamReader( is, contentEncoding );
                            }
                            catch( UnsupportedEncodingException e )
                            {
                                isr = new InputStreamReader( is, "UTF-8" );
                            }
                        }
                        else
                        {
                            isr = new InputStreamReader( is, "UTF-8" );
                        }
                    }
                    catch( UnsupportedEncodingException e )
                    {
                        isr = new InputStreamReader( is );
                    }

                    // Download the contents
                    BufferedReader reader = new BufferedReader( isr );
                    StringBuilder result = new StringBuilder();
                    while( true )
                    {
                        synchronized( m_lock )
                        {
                            if( m_cancelled )
                            {
                                break;
                            }
                        }

                        String line = reader.readLine();
                        if( line == null )
                        {
                            break;
                        }
                        result.append( line );
                        result.append( '\n' );
                    }
                    reader.close();

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
                            m_success = true;
                            m_result = result.toString();
                            m_responseCode = connection.getResponseCode();
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
            }
        } );
        
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

    public boolean wasSuccessful()
    {
        synchronized(m_lock) {
            return m_success;
        }
    }
    
    public BufferedReader getContents()
    {
        String result = null;
        synchronized(m_lock) {
            result = m_result;
        }
        
        if( result != null ) {
            return new BufferedReader( new StringReader( result ) );
        }
        return null;
    }
    
    private Object m_lock = new Object();
    private URL m_url;
    private final String m_urlString;
    
    private boolean m_complete;
    private boolean m_cancelled;
    private boolean m_success;
    private String m_result;
    private int m_responseCode;
}
