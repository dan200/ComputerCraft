/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.entity;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TurtleVisionCamera extends EntityLivingBase
{
    private ITurtleAccess m_turtle;
    private ItemStack[] m_inventory;

    public TurtleVisionCamera( World world, ITurtleAccess turtle )
    {
        super( world );
        m_turtle = turtle;
        m_inventory = new ItemStack[0];
        applyPos();
    }

    public ITurtleAccess getTurtle()
    {
        return m_turtle;
    }

    @Override
    public float getEyeHeight()
    {
        return 0.0f;
    }

    @Override
    public void onUpdate()
    {
        m_turtle = ((TurtleBrain)m_turtle).getFutureSelf();
        applyPos();
    }

    private void applyPos()
    {
        Vec3 prevPos = m_turtle.getVisualPosition( 0.0f );
        this.lastTickPosX = this.prevPosX = prevPos.xCoord;
        this.lastTickPosY = this.prevPosY = prevPos.yCoord;
        this.lastTickPosZ = this.prevPosZ = prevPos.zCoord;
        this.prevRotationPitch = 0.0f;
        this.prevRotationYaw = m_turtle.getVisualYaw( 0.0f );
        this.prevCameraPitch = 0.0f;

        Vec3 pos = m_turtle.getVisualPosition( 1.0f );
        this.posX = pos.xCoord;
        this.posY = pos.yCoord;
        this.posZ = pos.zCoord;
        this.rotationPitch = 0.0f;
        this.rotationYaw = m_turtle.getVisualYaw( 1.0f );
        this.cameraPitch = 0.0f;

        float yawDifference = this.rotationYaw - this.prevRotationYaw;
        if( yawDifference > 180.0f )
        {
            this.prevRotationYaw += 360.0f;
        }
        else if( yawDifference < -180.0f )
        {
            this.prevRotationYaw -= 360.0f;
        }
    }

    // EntityLivingBase overrides:

    @Override
    public ItemStack getHeldItem()
    {
        return null;
    }

    @Override
    public ItemStack getEquipmentInSlot( int slot )
    {
        return null;
    }

    @Override
    public ItemStack getCurrentArmor( int slotIn )
    {
        return null;
    }

    @Override
    public void setCurrentItemOrArmor( int slot, ItemStack stack )
    {
    }

    @Override
    public ItemStack[] getInventory()
    {
        return m_inventory;
    }
}
