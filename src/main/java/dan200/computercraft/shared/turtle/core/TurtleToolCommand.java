/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import com.google.common.base.Optional;
import dan200.computercraft.api.turtle.*;

public class TurtleToolCommand implements ITurtleCommand
{
    private final TurtleVerb m_verb;
    private final InteractDirection m_direction;
    private final Optional<TurtleSide> m_side;

    public TurtleToolCommand( TurtleVerb verb, InteractDirection direction, Optional<TurtleSide> side )
    {
        m_verb = verb;
        m_direction = direction;
        m_side = side;
    }

    @Override
    public TurtleCommandResult execute( ITurtleAccess turtle )
    {
        TurtleCommandResult firstFailure = null;
        for( TurtleSide side : TurtleSide.values() )
        {
            if( !m_side.isPresent() || m_side.get() == side )
            {
                ITurtleUpgrade upgrade = turtle.getUpgrade( side );
                if( upgrade != null && upgrade.getType().isTool() )
                {
                    TurtleCommandResult result = upgrade.useTool( turtle, side, m_verb, m_direction.toWorldDir( turtle ) );
                    if( result.isSuccess() )
                    {
                        switch( side )
                        {
                            case Left:
                            {
                                turtle.playAnimation( TurtleAnimation.SwingLeftTool );
                                break;
                            }
                            case Right:
                            {
                                turtle.playAnimation( TurtleAnimation.SwingRightTool );
                                break;
                            }
                            default:
                            {
                                turtle.playAnimation( TurtleAnimation.Wait );
                                break;
                            }
                        }
                        return result;
                    }
                    else if( firstFailure == null )
                    {
                        firstFailure = result;
                    }
                }
            }
        }
        if( firstFailure != null )
        {
            return firstFailure;
        }
        else
        {
            return TurtleCommandResult.failure( "No tool to " + m_verb.toString().toLowerCase() + " with" );
        }
    }
}
