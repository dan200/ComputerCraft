/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.lua;
import dan200.computercraft.core.apis.ILuaAPI;

import java.io.InputStream;
import java.io.OutputStream;

public interface ILuaMachine
{
    public void addAPI( ILuaAPI api );
    
    public void loadBios( InputStream bios );
    public void handleEvent( String eventName, Object[] arguments );
    public void softAbort( String abortMessage );
    public void hardAbort( String abortMessage );
    
    public boolean saveState( OutputStream output );
    public boolean restoreState( InputStream input );
    
    public boolean isFinished();
    
    public void unload();
}
