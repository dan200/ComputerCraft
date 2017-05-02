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
     * Gets the holding entity of this item
     *
     * @return The holding entity, may be {@code null}.
     */
    @Nullable
    Entity getEntity();

    /**
     * Get if the modem light is turned on
     *
     * @return If the modem light is turned on
     */
    boolean getModemLight();

    /**
     * Turn on/off the modem light
     *
     * @param value If the light should be on
     */
    void setModemLight( boolean value );

    /**
     * Get the upgrade specific NBT
     *
     * @return The upgrade's NBT
     */
    @Nonnull
    NBTTagCompound getUpgradeNBTData();

    /**
     * Mark the upgrade specific NBT as dirty
     */
    void updateUpgradeNBTData();

    /**
     * Remove the current peripheral and create a new one. You
     * may wish to do this if the methods available change.
     */
    void invalidatePeripheral();

    /**
     * Get a list of all upgrades for the pocket computer
     *
     * @return A collection of all upgrade names
     */
    @Nonnull
    Map<ResourceLocation, IPeripheral> getUpgrades();
}
