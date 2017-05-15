/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SoundTask implements ILuaTask
{

    SoundTask(World world, BlockPos pos, ResourceLocation resourceName, float volume, float pitch)
    {
        m_world = world;
        m_pos = pos;
        m_resourceName = resourceName;
        m_volume = volume;
        m_pitch = pitch;
    }

    // Fields
    private World m_world;
    private BlockPos m_pos;
    private ResourceLocation m_resourceName;
    private float m_volume;
    private float m_pitch;


    @Nullable
    @Override
    public Object[] execute() throws LuaException {
        m_world.playSound(null, m_pos, new SoundEvent(m_resourceName), SoundCategory.RECORDS, m_volume, m_pitch);
        return new Object[]{};
    }
}
