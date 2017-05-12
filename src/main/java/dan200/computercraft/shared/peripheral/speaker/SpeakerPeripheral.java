/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpeakerPeripheral implements IPeripheral {

    private TileSpeaker m_speaker;
    private long m_clock;
    private long m_lastPlayTime;
    private int m_notesThisTick;

    public SpeakerPeripheral()
    {
        m_clock = 0;
        m_lastPlayTime = 0;
        m_notesThisTick = 0;
    }

    public SpeakerPeripheral(TileSpeaker speaker)
    {
        this();
        m_speaker = speaker;
    }

    public void update() {
        m_clock++;
        m_notesThisTick = 0;
    }

    public World getWorld()
    {
        return m_speaker.getWorld();
    }

    public BlockPos getPos()
    {
        return m_speaker.getPos();
    }

    /* IPeripheral implementations */

    @Override
    public boolean equals(IPeripheral other)
    {
        if (other != null && other instanceof SpeakerPeripheral)
        {
            SpeakerPeripheral otherSpeaker = (SpeakerPeripheral) other;
            return otherSpeaker.m_speaker == m_speaker;
        }

        else
        {
            return false;
        }

    }


    @Override
    public void attach(IComputerAccess computerAccess)
    {
    }

    @Override
    public void detach(IComputerAccess computerAccess)
    {
    }

    @Override
    public String getType()
    {
        return "speaker";
    }

    @Override
    public String[] getMethodNames()
    {
        return new String[] {
                "playSound", // Plays sound at resourceLocator
                "playNote" // Plays note
        };
    }

    @Override
    public Object[] callMethod(IComputerAccess computerAccess, ILuaContext context, int methodIndex, Object[] args) throws LuaException
    {
        switch (methodIndex)
        {
            // playsound
            case 0: {
                return playSound(args, false);
            }

            // playnote
            case 1:
            {
                return playNote(args);
            }

            default:
            {
                throw new LuaException("Method index out of range!");
            }

        }
    }

    private Object[] playNote(Object[] arguments) throws LuaException
    {
        double volume = 1f;
        double pitch = 1f;

        // Check if arguments are correct
        if (arguments.length == 0) // Too few args
        {
            throw new LuaException("Expected string, number (optional), number (optional)");
        }

        if (!(arguments[0] instanceof String)) // Arg wrong type
        {
            throw new LuaException("Expected string, number (optional), number (optional)");
        }

        if (!SoundEvent.REGISTRY.containsKey(new ResourceLocation("block.note." + arguments[0])))
        {
            throw new LuaException("Invalid instrument, \"" + arguments[0] + "\"!");
        }

        if (arguments.length > 1)
        {
            if (!(arguments[1] instanceof Double))   // Arg wrong type
            {
                throw new LuaException("Expected string, number (optional), number (optional)");
            }
            volume = ((Double) arguments[1]).floatValue();

        }

        if (arguments.length > 2)
        {
            if (!(arguments[1] instanceof Double))  // Arg wrong type
            {
                throw new LuaException("Expected string, number (optional), number (optional)");
            }
            pitch = ((Double) arguments[2]).floatValue();
        }

        if (arguments.length > 3)
        {
            throw new LuaException("Expected string, number (optional), number (optional)");
        }

        // If the resource location for noteblock notes changes, this method call will need to be updated
        Object[] returnValue = playSound(new Object[] {"block.note." + arguments[0], volume, Math.pow(2d, (pitch - 12) / 12d)}, true);
        m_notesThisTick++;

        return returnValue;

    }

    private Object[] playSound(Object[] arguments, boolean isNote) throws LuaException
    {

        float volume = 1f;
        float pitch = 1f;

        // Check if arguments are correct
        if (arguments.length == 0) // Too few args
        {
            throw new LuaException("Expected string, number (optional), number (optional)");
        }

        if (!(arguments[0] instanceof String)) // Arg wrong type
        {
            throw new LuaException("Expected string, number (optional), number (optional)");
        }

        if (arguments.length > 1)
        {
            if (!(arguments[1] instanceof Double))  // Arg wrong type
            {
                throw new LuaException("Expected string, number (optional), number (optional)");
            }
            volume = ((Double) arguments[1]).floatValue();

        }

        if (arguments.length > 2)
        {
            if (!(arguments[1] instanceof Double))  // Arg wrong type
            {
                throw new LuaException("Expected string, number (optional), number (optional)");
            }
            pitch = ((Double) arguments[2]).floatValue();
        }

        if (arguments.length > 3)
        {
            throw new LuaException("Expected string, number (optional), number (optional)");
        }

        ResourceLocation resourceName = new ResourceLocation((String) arguments[0]);

        if (m_clock - m_lastPlayTime > TileSpeaker.MIN_TICKS_BETWEEN_SOUNDS || ((m_clock - m_lastPlayTime == 0) && (m_notesThisTick < ComputerCraft.maxNotesPerTick) && isNote))
        {

            if (SoundEvent.REGISTRY.containsKey(resourceName))
            {
               getWorld().playSound(null, getPos(), new SoundEvent(resourceName), SoundCategory.RECORDS, volume, pitch);
                m_lastPlayTime = m_clock;
                return new Object[]{true}; // Success, return true
            }

            else
            {
                return new Object[]{false}; // Failed - sound not existent, return false
            }

        }

        else
        {
            return new Object[]{false}; // Failed - rate limited, return false
        }
    }
}

