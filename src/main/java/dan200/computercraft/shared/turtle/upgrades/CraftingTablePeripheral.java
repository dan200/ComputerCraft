/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.upgrades;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.core.TurtleCraftCommand;

import javax.annotation.Nonnull;

public class CraftingTablePeripheral
    implements IPeripheral
{
    private final ITurtleAccess m_turtle;

    public CraftingTablePeripheral( ITurtleAccess turtle )
    {
        m_turtle = turtle;
    }
            
    // IPeripheral implementation

    @Nonnull
    @Override
    public String getType()
    {
        return "workbench";
    }
       
    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "craft",
        };
    }
        
    private int parseCount( Object[] arguments ) throws LuaException
    {
        if( arguments.length < 1 )
        {
            return 64;
        }
        
        if( !(arguments[0] instanceof Number) )
        {
            throw new LuaException( "Expected number" );
        }
        int count = ((Number)arguments[0]).intValue();
        if( count < 0 || count > 64 )
        {
            throw new LuaException( "Crafting count " + count + " out of range" );
        }
        return count;
    }
    
    @Override
    public Object[] callMethod( @Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments ) throws LuaException, InterruptedException
    {
        switch( method )
        {
            case 0:
            {
                // craft
                final int limit = parseCount( arguments );
                return m_turtle.executeCommand( context, new TurtleCraftCommand( limit ) );
            }
            default:
            {
                return null;
            }
        }
    }
    
    @Override
    public void attach( @Nonnull IComputerAccess computer )
    {
    }
    
    @Override
    public void detach( @Nonnull IComputerAccess computer )
    {
    }

    @Override
    public boolean equals( IPeripheral other )
    {
        return (other != null && other.getClass() == this.getClass());
    }
}
