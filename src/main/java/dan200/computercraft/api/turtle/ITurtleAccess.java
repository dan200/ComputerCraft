/**
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.turtle;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The interface passed to turtle by turtles, providing methods that they can call.
 *
 * This should not be implemented by your classes. Do not interact with turtles except via this interface and
 * {@link ITurtleUpgrade}.
 */
public interface ITurtleAccess
{
    /**
     * Returns the world in which the turtle resides.
     *
     * @return the world in which the turtle resides.
     */
    public World getWorld();

    /**
     * Returns a vector containing the integer co-ordinates at which the turtle resides.
     *
     * @return a vector containing the integer co-ordinates at which the turtle resides.
     */
    public BlockPos getPosition();

    /**
     * Attempt to move this turtle to a new position.
     *
     * This will preserve the turtle's internal state, such as it's inventory, computer and upgrades. It should
     * be used before playing a movement animation using {@link #playAnimation(TurtleAnimation)}.
     *
     * @param world The new world to move it to
     * @param pos   The new position to move it to.
     * @return Whether the movement was successful. It may fail if the block was not loaded or the block placement
     * was cancelled. Note this will not check
     * {@link dan200.computercraft.api.permissions.ITurtlePermissionProvider#isBlockEnterable(World, BlockPos)}.
     * @throws UnsupportedOperationException When attempting to teleport on the client side.
     */
    public boolean teleportTo( World world, BlockPos pos );

    /**
     * Returns a vector containing the floating point co-ordinates at which the turtle is rendered.
     * This will shift when the turtle is moving.
     *
     * @param f The subframe fraction.
     * @return A vector containing the floating point co-ordinates at which the turtle resides.
     * @see #getVisualYaw(float)
     */
    public Vec3d getVisualPosition( float f );

    /**
     * Returns the yaw the turtle is facing when it is rendered.
     *
     * @param f The subframe fraction.
     * @return The yaw the turtle is facing.
     * @see #getVisualPosition(float)
     */
    public float getVisualYaw( float f );

    /**
     * Returns the world direction the turtle is currently facing.
     *
     * @return The world direction the turtle is currently facing.
     * @see #setDirection(EnumFacing)
     */
    public EnumFacing getDirection();

    /**
     * Set the direction the turtle is facing. Note that this will not play a rotation animation, you will also need to
     * call {@link #playAnimation(TurtleAnimation)} to do so.
     *
     * @param dir The new direction to set. This should be on either the x or z axis (so north, south, east or west).
     * @see #getDirection()
     */
    public void setDirection( EnumFacing dir );

    /**
     * Get the currently selected slot in the turtle's inventory.
     *
     * @return An integer representing the current slot.
     * @see #getInventory()
     * @see #setSelectedSlot(int)
     */
    public int getSelectedSlot();

    /**
     * Set the currently selected slot in the turtle's inventory.
     *
     * @param slot The slot to set. This must be greater or equal to 0 and less than the inventory size. Otherwise no
     *             action will be taken.
     * @throws UnsupportedOperationException When attempting to change the slot on the client side.
     * @see #getInventory()
     * @see #getSelectedSlot()
     */
    public void setSelectedSlot( int slot );

    /**
     * Sets the colour of the turtle, as if the player had dyed it with a dye item.
     *
     * @param dyeColour 0-15 to dye the turtle one of the 16 standard Minecraft colours, or -1 to remove the dye from the turtle.
     * @see #getDyeColour()
     */
    public void setDyeColour( int dyeColour );

    /**
     * Gets the colour the turtle has been dyed.
     *
     * @return 0-15 if the turtle has been dyed one of the 16 standard Minecraft colours, -1 if the turtle is clean.
     * @see #getDyeColour()
     */
    public int getDyeColour();

    /**
     * Get the inventory of this turtle
     *
     * @return This turtle's inventory
     */
    public IInventory getInventory();

    /**
     * Determine whether this turtle will require fuel when performing actions.
     *
     * @return Whether this turtle needs fuel.
     * @see #getFuelLevel()
     * @see #setFuelLevel(int)
     */
    public boolean isFuelNeeded();

    /**
     * Get the current fuel level of this turtle.
     *
     * @return The turtle's current fuel level.
     * @see #isFuelNeeded()
     * @see #setFuelLevel(int)
     */
    public int getFuelLevel();

    /**
     * Set the fuel level to a new value. It is generally preferred to use {@link #consumeFuel(int)}} or {@link #addFuel(int)}
     * instead.
     *
     * @param fuel The new amount of fuel. This must be between 0 and the fuel limit.
     * @see #getFuelLevel()
     * @see #getFuelLimit()
     * @see #addFuel(int)
     * @see #consumeFuel(int)
     */
    public void setFuelLevel( int fuel );

    /**
     * Get the maximum amount of fuel a turtle can hold.
     *
     * @return The turtle's fuel limit.
     */
    public int getFuelLimit();

    /**
     * Removes some fuel from the turtles fuel supply. Negative numbers can be passed in to INCREASE the fuel level of the turtle.
     *
     * @param fuel The amount of fuel to consume.
     * @return Whether the turtle was able to consume the amount of fuel specified. Will return false if you supply a number
     * greater than the current fuel level of the turtle. No fuel will be consumed if {@code false} is returned.
     * @throws UnsupportedOperationException When attempting to consume fuel on the client side.
     */
    public boolean consumeFuel( int fuel );

    /**
     * Increase the turtle's fuel level by the given amount.
     *
     * @param fuel The amount to refuel with.
     * @throws UnsupportedOperationException When attempting to refuel on the client side.
     */
    public void addFuel( int fuel );

    /**
     * Adds a custom command to the turtles command queue. Unlike peripheral methods, these custom commands will be executed
     * on the main thread, so are guaranteed to be able to access Minecraft objects safely, and will be queued up
     * with the turtles standard movement and tool commands. An issued command will return an unique integer, which will
     * be supplied as a parameter to a "turtle_response" event issued to the turtle after the command has completed. Look at the
     * lua source code for "rom/apis/turtle" for how to build a lua wrapper around this functionality.
     *
     * @param command an object which will execute the custom command when its point in the queue is reached
     * @return the objects the command returned when executed. you should probably return these to the player
     * unchanged if called from a peripheral method.
     * @throws UnsupportedOperationException When attempting to execute a command on the client side.
     * @see ITurtleCommand
     */
    public Object[] executeCommand( ILuaContext context, ITurtleCommand command ) throws LuaException, InterruptedException;

    /**
     * Start playing a specific animation. This will prevent other turtle commands from executing until
     * it is finished.
     *
     * @param animation The animation to play.
     * @throws UnsupportedOperationException When attempting to execute play an animation on the client side.
     * @see TurtleAnimation
     */
    public void playAnimation( TurtleAnimation animation );

    /**
     * Returns the turtle on the specified side of the turtle, if there is one.
     *
     * @return The upgrade on the specified side of the turtle, if there is one.
     * @see #setUpgrade(TurtleSide, ITurtleUpgrade)
     */
    public ITurtleUpgrade getUpgrade( TurtleSide side );

    /**
     * Set the upgrade for a given side, resetting peripherals and clearing upgrade specific data.
     *
     * @param side    The side to set the upgrade on.
     * @param upgrade The upgrade to set, may be {@code null} to clear.
     * @see #getUpgrade(TurtleSide)
     */
    public void setUpgrade( TurtleSide side, ITurtleUpgrade upgrade );

    /**
     * Returns the peripheral created by the upgrade on the specified side of the turtle, if there is one.
     *
     * @return The peripheral created by the upgrade on the specified side of the turtle, {@code null} if none exists.
     */
    public IPeripheral getPeripheral( TurtleSide side );

    /**
     * Get an upgrade-specific NBT compound, which can be used to store arbitrary data.
     *
     * This will be persisted across turtle restarts and chunk loads, as well as being synced to the client. You must
     * call {@link #updateUpgradeNBTData(TurtleSide)} after modifying it.
     *
     * @param side The side to get the upgrade data for.
     * @return The upgrade-specific data.
     * @see #updateUpgradeNBTData(TurtleSide)
     */
    public NBTTagCompound getUpgradeNBTData( TurtleSide side );

    /**
     * Mark the upgrade-specific data as dirty on a specific side. This is required for the data to be synced to the
     * client and persisted.
     *
     * @param side The side to mark dirty.
     * @see #updateUpgradeNBTData(TurtleSide)
     */
    public void updateUpgradeNBTData( TurtleSide side );
}
