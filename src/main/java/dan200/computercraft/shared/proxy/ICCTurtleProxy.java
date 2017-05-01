/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.proxy;

import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.shared.util.IEntityDropConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ICCTurtleProxy
{
    public void preInit();
    public void init();

    public void registerTurtleUpgrade( ITurtleUpgrade upgrade );
    public ITurtleUpgrade getTurtleUpgrade( String id );
    public ITurtleUpgrade getTurtleUpgrade( int legacyId );
    public ITurtleUpgrade getTurtleUpgrade( ItemStack item );
    public void addAllUpgradedTurtles( List<ItemStack> list );

    public void setEntityDropConsumer( Entity entity, IEntityDropConsumer consumer );
    public void clearEntityDropConsumer( Entity entity );
}
