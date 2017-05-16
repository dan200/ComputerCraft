/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.core.apis.IAPIEnvironment;

import javax.annotation.Nonnull;

public class TurtleCheckRedstoneCommand implements ITurtleCommand
{
    private final IAPIEnvironment m_environment;
    private final InteractDirection m_direction;

    public TurtleCheckRedstoneCommand( IAPIEnvironment environment, InteractDirection direction )
    {
        m_environment = environment;
        m_direction = direction;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute( @Nonnull ITurtleAccess turtle )
    {
        // Do the checking
        int redstoneSide;
        switch( m_direction )
        {
            case Forward:
            default:
            {
                redstoneSide = 3;
                break;
            }
            case Up:
            {
                redstoneSide = 1;
                break;
            }
            case Down:
            {
                redstoneSide = 2;
                break;
            }
        }
        int power = m_environment.getInput( redstoneSide );
        if( power > 0 )
        {
            return TurtleCommandResult.success();
        }
        else
        {
            return TurtleCommandResult.failure();
        }
    }
}
