/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.items;

import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface ITurtleItem extends IComputerItem
{
    public ITurtleUpgrade getUpgrade( ItemStack stack, TurtleSide side );
    public int getFuelLevel( ItemStack stack );
    public Colour getColour( ItemStack stack );
    public ResourceLocation getOverlay( ItemStack stack );
}
