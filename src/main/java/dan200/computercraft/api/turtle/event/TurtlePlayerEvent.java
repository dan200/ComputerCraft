/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.turtle.event;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;

/**
 * An action done by a turtle which is normally done by a player.
 *
 * {@link #getPlayer()} may be used to modify the player's attributes or perform permission checks.
 */
public abstract class TurtlePlayerEvent extends TurtleActionEvent
{
    private final FakePlayer player;

    protected TurtlePlayerEvent( @Nonnull ITurtleAccess turtle, @Nonnull TurtleAction action, @Nonnull FakePlayer player )
    {
        super( turtle, action );

        Preconditions.checkNotNull( player, "player cannot be null" );
        this.player = player;
    }

    /**
     * A fake player, representing this turtle.
     *
     * This may be used for triggering permission checks.
     *
     * @return A {@link FakePlayer} representing this turtle.
     */
    @Nonnull
    public FakePlayer getPlayer()
    {
        return player;
    }
}
