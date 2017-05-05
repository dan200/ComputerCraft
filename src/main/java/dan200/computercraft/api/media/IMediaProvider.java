/**
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.media;

import net.minecraft.item.ItemStack;

/**
 * This interface is used to provide {@link IMedia} implementations for {@link ItemStack}.
 *
 * @see dan200.computercraft.api.ComputerCraftAPI#registerMediaProvider(IMediaProvider)
 */
public interface IMediaProvider
{
    /**
     * Produce an IMedia implementation from an ItemStack.
     *
     * @param stack The stack from which to extract the media information.
     * @return An IMedia implementation, or null if the item is not something you wish to handle
     * @see dan200.computercraft.api.ComputerCraftAPI#registerMediaProvider(IMediaProvider)
     */
    public IMedia getMedia( ItemStack stack );
}
