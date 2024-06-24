/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.turtle.event;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;

/**
 * A base class for all events concerning a turtle. This will only ever constructed and fired on the server side,
 * so sever specific methods on {@link ITurtleAccess} are safe to use.
 *
 * You should generally not need to subscribe to this event, preferring one of the more specific classes.
 *
 * @see TurtleActionEvent
 */
public abstract class TurtleEvent extends Event
{
    private final ITurtleAccess turtle;

    protected TurtleEvent( @Nonnull ITurtleAccess turtle )
    {
        Preconditions.checkNotNull( turtle, "turtle cannot be null" );
        this.turtle = turtle;
    }

    /**
     * Get the turtle which is performing this action.
     *
     * @return The access for this turtle.
     */
    @Nonnull
    public ITurtleAccess getTurtle()
    {
        return turtle;
    }
}
