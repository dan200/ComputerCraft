/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.core.apis.IAPIEnvironment;

import javax.annotation.Nonnull;

public class TurtleSetRedstoneCommand implements ITurtleCommand
{
    private final IAPIEnvironment m_environment;
    private final InteractDirection m_direction;
    private final int m_value;

    public TurtleSetRedstoneCommand( IAPIEnvironment environment, InteractDirection direction, int value )
    {
        m_environment = environment;
        m_direction = direction;
        m_value = value;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute( @Nonnull ITurtleAccess turtle )
    {
        // Do the setting
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
        m_environment.setOutput( redstoneSide, m_value );
        turtle.playAnimation( TurtleAnimation.ShortWait );
        return TurtleCommandResult.success();
    }
}
