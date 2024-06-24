package dan200.computercraft.api.turtle.event;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An event fired when a turtle is performing a known action.
 */
@Cancelable
public class TurtleActionEvent extends TurtleEvent
{
    private final TurtleAction action;
    private String failureMessage;

    public TurtleActionEvent( @Nonnull ITurtleAccess turtle, @Nonnull TurtleAction action )
    {
        super( turtle );

        Preconditions.checkNotNull( action, "action cannot be null" );
        this.action = action;
    }

    public TurtleAction getAction()
    {
        return action;
    }

    /**
     * Sets the cancellation state of this action.
     *
     * If {@code cancel} is {@code true}, this action will not be carried out.
     *
     * @param cancel The new canceled value.
     * @see TurtleCommandResult#failure()
     * @deprecated Use {@link #setCanceled(boolean, String)} instead.
     */
    @Override
    @Deprecated
    public void setCanceled( boolean cancel )
    {
        setCanceled( cancel, null );
    }

    /**
     * Set the cancellation state of this action, setting a failure message if required.
     *
     * If {@code cancel} is {@code true}, this action will not be carried out.
     *
     * @param cancel         The new canceled value.
     * @param failureMessage The message to return to the user explaining the failure.
     * @see TurtleCommandResult#failure(String)
     */
    public void setCanceled( boolean cancel, @Nullable String failureMessage )
    {
        super.setCanceled( cancel );
        this.failureMessage = cancel ? failureMessage : null;
    }

    /**
     * Get the message with which this will fail.
     *
     * @return The failure message.
     * @see TurtleCommandResult#failure()
     * @see #setCanceled(boolean, String)
     */
    @Nullable
    public String getFailureMessage()
    {
        return failureMessage;
    }
}
