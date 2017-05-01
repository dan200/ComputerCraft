/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.blocks;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.common.IDirectionalTile;
import dan200.computercraft.shared.computer.blocks.IComputerTile;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public interface ITurtleTile extends IComputerTile, IDirectionalTile
{
    public Colour getColour();
    public ResourceLocation getOverlay();
    public ITurtleUpgrade getUpgrade( TurtleSide side );
    public ITurtleAccess getAccess();

    public Vec3d getRenderOffset( float f );
    public float getRenderYaw( float f );
    public float getToolRenderAngle( TurtleSide side, float f );
}
