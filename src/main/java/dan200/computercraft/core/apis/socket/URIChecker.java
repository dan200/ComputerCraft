/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis.socket;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.core.apis.HTTPRequestException;
import java.net.*;

public class URIChecker {
	public static InetAddress checkHost( String url ) throws HTTPRequestException
    {
        try
        {
			System.out.println(url);
            InetAddress resolved = InetAddress.getByName( url );
            if( !ComputerCraft.http_whitelist.matches( resolved ) || ComputerCraft.http_blacklist.matches( resolved ) )
            {
                throw new HTTPRequestException( "Domain not permitted" );
            }

            return resolved;
        }
        catch( UnknownHostException e )
        {
            throw new HTTPRequestException( "Unknown host" );
        }
    }
	public static InetAddress checkHost( URI url ) throws HTTPRequestException
	{
		return checkHost(url.getHost());
	}
	public static InetAddress ezCheckHost( String myURL ) throws HTTPRequestException
	{
		try {
			return checkHost(new URI(myURL));
		} catch (Exception b) {
			return checkHost(myURL);
		}
	}
}