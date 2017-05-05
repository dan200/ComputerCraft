/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 *
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class SoundAPI implements ILuaAPI
{

    /**
     * Sound API for ComputerCraft. Provides an interface to Forge's API.
     * Possible use cases are notification apis, alert sounds, or playing music
     */

    private String[] methods;
    private ServerComputer computer;

    public SoundAPI(ServerComputer computer)
    {

        this.methods = new String[] {
                "play", // Plays a sound from given resource locator
        };

        this.computer = computer;

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
        return this.methods;
    }

    @Override
    public void startup()
    {
    }

    @Override
    public void advance(double _dt)
    {
    }

    @Override
    public void shutdown()
    {

    }

    @Override
    public Object[] callMethod(ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
    {

        switch(method)
        {

        // play
        case 0:

            float volume = 1f;
            float pitch = 1f;

            // If anyone can come up with a more concise way of doing this, *please* do

            // Check if arguments are correct
            if (arguments.length == 0) // Too few args
                throw new LuaException("Expected string, number (optional), number (optional)");

            else if (!(arguments[0] instanceof String)) // Arg wrong type
                throw new LuaException("Expected string, number (optional), number (optional)");


            else if (arguments.length == 2)
            {
                if (!(arguments[1] instanceof Double)) // Arg wrong type
                    throw new LuaException("Expected string, number (optional), number (optional)");
                volume = ((Double) arguments[1]).floatValue();

            }

            else if (arguments.length == 3)
            {
                if (!(arguments[2] instanceof Double)) // Arg wrong type
                    throw new LuaException("Expected string, number (optional), number (optional)");
                pitch = ((Double) arguments[2]).floatValue();
            }

            String resourceName = (String) arguments[0];

            this.computer.getWorld().playSound(null, this.computer.getPosition(), new SoundEvent(new ResourceLocation(resourceName)), SoundCategory.RECORDS, volume, pitch);

            return null;

        default:
            return null;

        }
    }
}