/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.proxy;

import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.shared.util.IEntityDropConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public interface ICCTurtleProxy
{
    void preInit();
    void init();

    void registerTurtleUpgrade( ITurtleUpgrade upgrade );
    ITurtleUpgrade getTurtleUpgrade( String id );
    ITurtleUpgrade getTurtleUpgrade( int legacyId );
    ITurtleUpgrade getTurtleUpgrade( @Nonnull ItemStack item );
    void addAllUpgradedTurtles( NonNullList<ItemStack> list );

    void setEntityDropConsumer( Entity entity, IEntityDropConsumer consumer );
    void clearEntityDropConsumer( Entity entity );
}
