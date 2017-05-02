package dan200.computercraft.api.pocket;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Additional peripherals for pocket computers.
 * <p>
 * This is similar to {@link dan200.computercraft.api.turtle.ITurtleUpgrade}.
 */
public interface IPocketUpgrade
{

    /**
     * Gets a unique identifier representing this type of turtle upgrade. eg: "computercraft:wireless_modem" or "my_mod:my_upgrade".
     * You should use a unique resource domain to ensure this upgrade is uniquely identified.
     * The peripheral will fail registration if an already used ID is specified.
     *
     * @return The id
     * @see IPocketUpgrade#getUpgradeID()
     * @see ComputerCraftAPI#registerPocketUpgrade(IPocketUpgrade)
     */
    @Nonnull
    ResourceLocation getUpgradeID();

    /**
     * Return a String to describe this type of turtle in turtle item names.
     * An example of built-in adjectives is "Wireless".
     *
     * @return The unlocalised adjective
     * @see ITurtleUpgrade#getUnlocalisedAdjective()
     */
    @Nonnull
    String getUnlocalisedAdjective();

    /**
     * Return an item stack representing the type of item that a turtle must be crafted
     * with to create a turtle which holds this upgrade. This item stack is also used
     * to determine the upgrade given by turtle.equip()
     *
     * @return The item stack used for crafting. This can be {@code null} if crafting is disabled.
     * @see ITurtleUpgrade#getCraftingItem()
     */
    @Nullable
    ItemStack getCraftingItem();

    /**
     * Creates a peripheral for the pocket computer.
     * <p>
     * The peripheral created will be stored for the lifetime of the upgrade, will have update() called
     * once-per-tick, and will be attached, detached and have methods called in the same manner as a Computer peripheral.
     *
     * @param access The access object for the pocket item stack
     * @return The newly created peripheral.
     * @see ITurtleUpgrade#createPeripheral(ITurtleAccess, TurtleSide)
     */
    @Nullable
    IPeripheral createPeripheral( @Nonnull IPocketAccess access );

    /**
     * Called when the pocket computer item stack updates
     *
     * @param access     The access object for the pocket item stack
     * @param peripheral The peripheral for this upgrade
     */
    void update( @Nonnull IPocketAccess access, @Nullable IPeripheral peripheral );

    /**
     * Called when the pocket computer is right clicked on something
     *
     * @param world      The world the computer is in
     * @param access     The access object for the pocket item stack
     * @param peripheral The peripheral for this upgrade
     * @return {@code true} to stop the gui from opening, otherwise false.
     */
    boolean onRightClick( @Nonnull World world, @Nonnull IPocketAccess access, @Nullable IPeripheral peripheral );
}
