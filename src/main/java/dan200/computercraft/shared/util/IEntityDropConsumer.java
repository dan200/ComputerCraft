/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.util;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IEntityDropConsumer
{
    void consumeDrop( Entity dropper, @Nonnull ItemStack drop );
}
