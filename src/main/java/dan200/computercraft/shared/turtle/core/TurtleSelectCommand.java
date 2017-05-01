/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleCommandResult;

public class TurtleSelectCommand implements ITurtleCommand
{
    private final int m_slot;

    public TurtleSelectCommand( int slot )
    {
        m_slot = slot;
    }

    @Override
    public TurtleCommandResult execute( ITurtleAccess turtle )
    {
        turtle.setSelectedSlot( m_slot );
        return TurtleCommandResult.success();
    }
}
