package dan200.computercraft.api.pocket;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Wrapper class for pocket computers
 */
public interface IPocketAccess
{
    /**
     * Gets the entity holding this item.
     *
     * @return The holding entity. This may be {@code null}.
     */
    @Nullable
    Entity getEntity();

    /**
     * Get the colour of the pocket computer's light.
     *
     * See {@link #setLight(int)} for the values this may return.
     *
     * @return The colour of the pocket computer's light.
     * @see #setLight(int)
     */
    int getLight();

    /**
     * Set the colour of the pocket computer's light. Use {@link 0} to turn it off.
     *
     * Colours take the form of an integer between 0 and 15, using the opposite order to those in
     * {@link <a href="http://www.computercraft.info/wiki/Colors_(API)#Colors">The colors API</a>}  - so 0 being black,
     * 1 representing red, 2 representing green all the way up to 15 for white.
     *
     * @param value The colour the light should have.
     * @see #getLight()
     */
    void setLight( int value );

    /**
     * Get the upgrade-specific NBT.
     *
     * This is persisted between computer reboots and chunk loads.
     *
     * @return The upgrade's NBT.
     * @see #updateUpgradeNBTData()
     */
    @Nonnull
    NBTTagCompound getUpgradeNBTData();

    /**
     * Mark the upgrade-specific NBT as dirty.
     *
     * @see #getUpgradeNBTData()
     */
    void updateUpgradeNBTData();

    /**
     * Remove the current peripheral and create a new one. You may wish to do this if the methods available change.
     */
    void invalidatePeripheral();

    /**
     * Get a list of all upgrades for the pocket computer.
     *
     * @return A collection of all upgrade names.
     */
    @Nonnull
    Map<ResourceLocation, IPeripheral> getUpgrades();
}
