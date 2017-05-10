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
import dan200.computercraft.shared.util.DirectionUtil;

import javax.annotation.Nonnull;

public class TurtleTurnCommand implements ITurtleCommand
{
    private final TurnDirection m_direction;

    public TurtleTurnCommand( TurnDirection direction )
    {
        m_direction = direction;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute( @Nonnull ITurtleAccess turtle )
    {
        switch( m_direction )
        {
            case Left:
            {
                turtle.setDirection( DirectionUtil.rotateLeft( turtle.getDirection() ) );
                turtle.playAnimation( TurtleAnimation.TurnLeft );
                return TurtleCommandResult.success();
            }
            case Right:
            {
                turtle.setDirection( DirectionUtil.rotateRight( turtle.getDirection() ) );
                turtle.playAnimation( TurtleAnimation.TurnRight );
                return TurtleCommandResult.success();
            }
            default:
            {
                return TurtleCommandResult.failure( "Unknown direction" );
            }
        }
    }
}
