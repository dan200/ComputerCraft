/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.entity;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class TurtleVisionCamera extends EntityLivingBase
{
    private ITurtleAccess m_turtle;
    private ArrayList<ItemStack> m_armor;

    public TurtleVisionCamera( World world, ITurtleAccess turtle )
    {
        super( world );
        m_turtle = turtle;
        m_armor = new ArrayList<ItemStack>();
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

    @Nonnull
    @Override
    public EnumHandSide getPrimaryHand()
    {
        return EnumHandSide.RIGHT;
    }

    @Override
    public void onUpdate()
    {
        m_turtle = ((TurtleBrain)m_turtle).getFutureSelf();
        applyPos();
    }

    private void applyPos()
    {
        Vec3d prevPos = m_turtle.getVisualPosition( 0.0f );
        this.lastTickPosX = this.prevPosX = prevPos.xCoord;
        this.lastTickPosY = this.prevPosY = prevPos.yCoord;
        this.lastTickPosZ = this.prevPosZ = prevPos.zCoord;
        this.prevRotationPitch = 0.0f;
        this.prevRotationYaw = m_turtle.getVisualYaw( 0.0f );
        this.prevCameraPitch = 0.0f;

        Vec3d pos = m_turtle.getVisualPosition( 1.0f );
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
    public ItemStack getHeldItem( EnumHand hand )
    {
        return null;
    }

    @Override
    public void setItemStackToSlot( @Nonnull EntityEquipmentSlot slot, ItemStack stack)
    {
    }

    @Override
    public ItemStack getItemStackFromSlot( @Nonnull EntityEquipmentSlot slot)
    {
        return null;
    }

    @Nonnull
    @Override
    public Iterable<ItemStack> getArmorInventoryList()
    {
        return m_armor;
    }
}
