/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.commandblock;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class CommandBlockPeripheral implements IPeripheral
{
    private final TileEntityCommandBlock m_commandBlock;

    public CommandBlockPeripheral( TileEntityCommandBlock commandBlock )
    {
        m_commandBlock = commandBlock;
    }

    // IPeripheral methods

    @Nonnull
    @Override
    public String getType()
    {
        return "command";
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "getCommand",
            "setCommand",
            "runCommand",
        };
    }

    @Override
    public Object[] callMethod( @Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull final Object[] arguments ) throws LuaException, InterruptedException
    {
        switch (method)
        {
            case 0:
            {
                // getCommand
                return context.executeMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        return new Object[] {
                            m_commandBlock.getCommandBlockLogic().getCommand()
                        };
                    }
                } );
            }
            case 1:
            {
                // setCommand
                if( arguments.length < 1 || !(arguments[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }

                final String command = (String) arguments[ 0 ];
                context.issueMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        BlockPos pos = m_commandBlock.getPos();
                        m_commandBlock.getCommandBlockLogic().setCommand( command );
                        m_commandBlock.getWorld().markBlockRangeForRenderUpdate( pos, pos );
                        return null;
                    }
                } );
                return null;
            }
            case 2:
            {
                // runCommand
                return context.executeMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        m_commandBlock.getCommandBlockLogic().trigger( m_commandBlock.getWorld() );
                        int result = m_commandBlock.getCommandBlockLogic().getSuccessCount();
                        if( result > 0 )
                        {
                            return new Object[] { true };
                        }
                        else
                        {
                            return new Object[] { false, "Command failed" };
                        }
                    }
                } );
            }
        }
        return null;
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
