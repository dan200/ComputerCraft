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
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;

import java.util.List;

public interface ICCTurtleProxy
{
    void preInit();
    void init();
    void remap( FMLMissingMappingsEvent mappings);

    void registerTurtleUpgrade( ITurtleUpgrade upgrade );
    ITurtleUpgrade getTurtleUpgrade( String id );
    ITurtleUpgrade getTurtleUpgrade( int legacyId );
    ITurtleUpgrade getTurtleUpgrade( ItemStack item );
    void addAllUpgradedTurtles( List<ItemStack> list );

    void setEntityDropConsumer( Entity entity, IEntityDropConsumer consumer );
    void clearEntityDropConsumer( Entity entity );
}
