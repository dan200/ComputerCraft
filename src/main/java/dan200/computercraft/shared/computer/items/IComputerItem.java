/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.items;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import net.minecraft.item.ItemStack;

public interface IComputerItem
{
    public int getComputerID( ItemStack stack );
    public String getLabel( ItemStack stack );
    public ComputerFamily getFamily( ItemStack stack );
}
