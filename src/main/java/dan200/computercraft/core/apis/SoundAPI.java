/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 *
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

/**
 * Sound API for ComputerCraft. Provides an interface to Forge's API.
 * Possible use cases are notification apis, alert sounds, or playing music
 */
public class SoundAPI implements ILuaAPI
{

    private String[] m_methods;
    private ServerComputer m_computer;
    private long m_lastPlayTime;
    private long m_clock;

    public SoundAPI(ServerComputer computer)
    {

        m_methods = new String[] {
                "play", // Plays a sound from given resource locator
                        // Returns: Object[]{success}
                "getPlayTimeout" // Gets minTimeBetweenPlay
        };

        m_computer = computer;
        m_lastPlayTime = 0;

    }


    /* ILuaAPI implementations */

    @Override
    public String[] getNames()
    {
        return new String[] {
                "sound",
        };
    }

    @Override
    public String[] getMethodNames()
    {
        return m_methods;
    }

    @Override
    public void startup()
    {
        m_clock = 0;
    }

    @Override
    public void advance(double _dt)
    {
        m_clock++;
    }

    @Override
    public void shutdown()
    {

    }

    @Override
    public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
    {
        switch (method)
        {

            // play
            case 0:
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
                    if (!(arguments[2] instanceof Double)) // Arg wrong type
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

                if (m_clock - m_lastPlayTime > ComputerCraft.Config.minTimeBetweenSounds.getInt())
                {

                    if (SoundEvent.REGISTRY.containsKey(resourceName))
                    {
                        m_computer.getWorld().playSound(null, m_computer.getPosition(), new SoundEvent(resourceName), SoundCategory.RECORDS, volume, pitch);
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

            // getPlayTimeout
            case 1:
            {

                if (arguments.length > 0)
                {
                    throw new LuaException("Expected nil, got arguments");
                }

                return new Object[]{ComputerCraft.Config.minTimeBetweenSounds.getInt()};
            }

            // ??? Something weird happened. Makes IDEs happy
            default:
            {
                return null;
            }
        }
    }
}
