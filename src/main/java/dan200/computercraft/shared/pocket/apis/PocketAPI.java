/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ILuaAPI;

public class PocketAPI implements ILuaAPI
{
    public PocketAPI()
    {
    }

    @Override
    public String[] getNames()
    {
        return new String[] {
            "pocket"
        };
    }

    @Override
    public void startup()
    {
    }

    @Override
    public void advance( double dt )
    {
    }

    @Override
    public void shutdown()
    {
    }

    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            // TODO: Add some methods
        };
    }

    @Override
    public Object[] callMethod( ILuaContext context, int method, Object[] arguments ) throws LuaException
    {
        // TODO: Add some methods
        return null;
    }
}
