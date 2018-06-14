/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.core.apis.socket.*;
import dan200.computercraft.core.apis.IAPIEnvironment;
import javax.annotation.Nonnull;
import javax.net.ssl.*;
import java.security.cert.Certificate;
import java.io.*;
import java.net.*;
import java.util.*;

import static dan200.computercraft.core.apis.ArgumentHelper.*;

public class SocketAPI implements ILuaAPI, IAsyncObject
{
	private final IAPIEnvironment m_apiEnvironment;
	private final List<AsyncAction> m_threads;
	private final List<Socket> m_socks;
	
	public SocketAPI( IAPIEnvironment environment )
    {
        m_apiEnvironment = environment;
		m_threads = new ArrayList<AsyncAction>();
        m_socks = new ArrayList<Socket>();
    }
    
    @Override
    public String[] getNames()
    {
        return new String[] {
            "socket"
        };
    }

    @Override
    public void startup( )
    {
    }

    @Override
    public void advance( double _dt )
    {
		synchronized( m_threads )
        {
			Iterator<AsyncAction> iterator = m_threads.iterator();
            while( iterator.hasNext() )
            {
				AsyncAction curthread = iterator.next();
				if (curthread.isDone())
				{
					iterator.remove();
				}
            }
        }
    }
	
	@Override
    public void shutdown( )
    {
        synchronized( m_socks )
        {
            for( Socket sock : m_socks )
            {
                try {
                    sock.close();
                } catch (IOException e) {}
            }
            m_socks.clear();
        }
		synchronized( m_threads )
        {
            m_threads.clear();
        }
    }
	
	@Nonnull
    @Override
    public String[] getMethodNames()
    {
         return new String[] {
             "open",
             "lookup",
			 "checkHost"
        };
    }
	
	@Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException
    {
		switch (method)
		{
			
			default:
			{
				AsyncAction action = new AsyncAction(
					this, m_apiEnvironment, context, method, args
				);
				synchronized(m_threads)
				{
					m_threads.add(action);
				}
		
				return new Object[] {action.ID};
			}
			
		}
	}
	
	public Object[] realCallMeth( @Nonnull ILuaContext context, int method, @Nonnull Object[] args )
	{
		try {
			switch (method)
			{
				case 0:
				{
					//open
					String URL = getString( args, 0 );
                	int port = getInt( args, 1 );
					boolean useSSL = optBoolean( args, 2, false );
					
					try {
					    URIChecker.ezCheckHost(URL);
					} catch (Exception e) {
					    return new Object[] { false, e.getMessage() };
					}
                
					Socket sock;
					CertWrapper certs;
                    if (useSSL) {
                        SSLSocketFactory SSLCreator = (SSLSocketFactory) SSLSocketFactory.getDefault();
						SSLSocket mysock = (SSLSocket) SSLCreator.createSocket(URL, port);
						certs = new CertWrapper(mysock);
						sock = (Socket) mysock;
					} else {
						sock = new Socket(URL, port);
						certs = new CertWrapper();
					}
                    sock.setKeepAlive(true);
                    
                    synchronized( m_socks )
                    {
                        m_socks.add( sock );
                    }
                    return new Object[] { true, SocketWrapper.wrapSocket( sock, certs, m_threads, m_apiEnvironment ) };
				}
				
				case 1:
            	{	
					//lookup
					String myURL = getString( args, 0 );
					InetAddress address;
					try {
						address = URIChecker.ezCheckHost(myURL);
					} catch (Exception e) {
						return new Object[] { false, e.getMessage() };
					}
					String addr = address.getHostAddress();
					URL info = new URL(myURL);
					if (info == null){
						info = new URL( "http://" + myURL );
					}
					if (info == null){
						return new Object[] { true, addr };
					}
					return new Object[] { true, addr, info.getDefaultPort() };
				}
				
				case 2:
				{
					//checkHost
					String myURL = getString( args, 0 );
					try {
						URIChecker.ezCheckHost(myURL);
					} catch (Exception e) {
						return new Object[] { false, e.getMessage() };
					}
					return new Object[] { true };
				}
				
				default:
            	{
                	return new Object[] {false, "Unknown method of \"Socket\" called"};
            	}
			}
		} catch (UnknownHostException e) {
			return new Object[] { false, "Unknown host: "+e.getMessage() };
		} catch (Exception e){
			return new Object[] { false, e.getMessage() };
		}
	}
}