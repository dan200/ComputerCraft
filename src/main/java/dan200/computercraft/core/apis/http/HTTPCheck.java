package dan200.computercraft.core.apis.http;

import dan200.computercraft.core.apis.HTTPRequestException;
import dan200.computercraft.core.apis.IAPIEnvironment;

import java.net.URL;

public class HTTPCheck implements HTTPTask.IHTTPTask
{
    private final String urlString;
    private final URL url;
    private String error;

    public HTTPCheck( String urlString, URL url )
    {
        this.urlString = urlString;
        this.url = url;
    }

    @Override
    public void run()
    {
        try
        {
            HTTPRequest.checkHost( url );
        }
        catch( HTTPRequestException e )
        {
            error = e.getMessage();
        }
    }

    @Override
    public void whenFinished( IAPIEnvironment environment )
    {
        if( error == null )
        {
            environment.queueEvent( "http_check", new Object[] { urlString, true } );
        }
        else
        {
            environment.queueEvent( "http_check", new Object[] { urlString, false, error } );
        }
    }
}
