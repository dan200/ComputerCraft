/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.ReflectionUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class TurtleItemFactory
{
    public static ItemStack create( ITurtleTile turtle )
    {
        ITurtleUpgrade leftUpgrade = turtle.getAccess().getUpgrade( TurtleSide.Left );
        ITurtleUpgrade rightUpgrade = turtle.getAccess().getUpgrade( TurtleSide.Right );

        IComputer computer = turtle.getComputer();
        if( computer != null )
        {
            String label = computer.getLabel();
            if( label != null )
            {
                if( turtle.getFamily() != ComputerFamily.Beginners )
                {
                    int id = computer.getID();
                    int fuelLevel = turtle.getAccess().getFuelLevel();
                    return create( id, label, turtle.getColour(), turtle.getFamily(), leftUpgrade, rightUpgrade, fuelLevel, turtle.getOverlay() );
                }
                else
                {
                    return create( -1, label, turtle.getColour(), turtle.getFamily(), leftUpgrade, rightUpgrade, 0, turtle.getOverlay() );
                }
            }
        }
        return create( -1, null, turtle.getColour(), turtle.getFamily(), leftUpgrade, rightUpgrade, 0, turtle.getOverlay() );
    }

    public static ItemStack create( int id, String label, Colour colour, ComputerFamily family, ITurtleUpgrade leftUpgrade, ITurtleUpgrade rightUpgrade, int fuelLevel, ResourceLocation overlay )
    {
        switch( family )
        {
            case Normal:
            {
                ItemTurtleBase legacy = ((ItemTurtleBase)Item.getItemFromBlock( ComputerCraft.Blocks.turtle ));
                ItemTurtleBase normal = ((ItemTurtleBase)Item.getItemFromBlock( ComputerCraft.Blocks.turtleExpanded ));
                ItemStack legacyStack = legacy.create( id, label, colour, leftUpgrade, rightUpgrade, fuelLevel, overlay );
                return (legacyStack != null) ? legacyStack : normal.create( id, label, colour, leftUpgrade, rightUpgrade, fuelLevel, overlay );
            }
            case Advanced:
            {
                ItemTurtleBase advanced = ((ItemTurtleBase)Item.getItemFromBlock( ComputerCraft.Blocks.turtleAdvanced ));
                return advanced.create( id, label, colour, leftUpgrade, rightUpgrade, fuelLevel, overlay );
            }
            case Beginners:
            {
                Block beginnersBlock = ReflectionUtil.safeGet(
                    ReflectionUtil.getOptionalField(
                        ReflectionUtil.getOptionalInnerClass(
                            ReflectionUtil.getOptionalClass( "dan200.computercraftedu.ComputerCraftEdu" ),
                            "Blocks"
                        ),
                        "turtleJunior"
                    ),
                    null,
                    Block.class
                );
                if( beginnersBlock != null )
                {
                    ItemTurtleBase beginnersItem = ((ItemTurtleBase)Item.getItemFromBlock( beginnersBlock ));
                    return beginnersItem.create( id, label, colour, leftUpgrade, rightUpgrade, fuelLevel, overlay );
                }
                return null;
            }
        }
        return null;
    }
}
