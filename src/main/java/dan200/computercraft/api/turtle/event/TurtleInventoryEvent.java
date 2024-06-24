package dan200.computercraft.api.turtle.event;

import com.google.common.base.Preconditions;
import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fired when a turtle attempts to interact with an inventory.
 */
@Cancelable
public abstract class TurtleInventoryEvent extends TurtleBlockEvent
{
    private final IItemHandler handler;

    protected TurtleInventoryEvent( @Nonnull ITurtleAccess turtle, @Nonnull TurtleAction action, @Nonnull FakePlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nullable IItemHandler handler )
    {
        super( turtle, action, player, world, pos );
        this.handler = handler;
    }

    /**
     * Get the inventory being interacted with
     *
     * @return The inventory being interacted with, {@code null} if the item will be dropped to/sucked from the world.
     */
    @Nullable
    public IItemHandler getItemHandler()
    {
        return handler;
    }

    /**
     * Fired when a turtle attempts to suck from an inventory.
     *
     * @see TurtleAction#SUCK
     */
    @Cancelable
    public static class Suck extends TurtleInventoryEvent
    {
        public Suck( @Nonnull ITurtleAccess turtle, @Nonnull FakePlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nullable IItemHandler handler )
        {
            super( turtle, TurtleAction.SUCK, player, world, pos, handler );
        }
    }

    /**
     * Fired when a turtle attempts to drop an item into an inventory.
     *
     * @see TurtleAction#DROP
     */
    @Cancelable
    public static class Drop extends TurtleInventoryEvent
    {
        private final ItemStack stack;

        public Drop( @Nonnull ITurtleAccess turtle, @Nonnull FakePlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nullable IItemHandler handler, @Nonnull ItemStack stack )
        {
            super( turtle, TurtleAction.DROP, player, world, pos, handler );

            Preconditions.checkNotNull( stack, "stack cannot be null" );
            this.stack = stack;
        }

        /**
         * The item which will be inserted into the inventory/dropped on the ground.
         *
         * Note that this is a copy of the original stack, and so should not be modified, as that will have no effect.
         *
         * @return The item stack which will be dropped.
         */
        public ItemStack getStack()
        {
            return stack.copy();
        }
    }
}
