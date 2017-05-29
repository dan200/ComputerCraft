/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    SpeakerPeripheral(TileSpeaker speaker)
    {
        this();
        m_speaker = speaker;
    }

    public synchronized void update()
    {
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

    /* IPeripheral implementation */

    @Override
    public boolean equals( IPeripheral other )
    {
        if( other != null && other instanceof SpeakerPeripheral )
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
    public void attach( @Nonnull IComputerAccess computerAccess )
    {
    }

    @Override
    public void detach( @Nonnull IComputerAccess computerAccess )
    {
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "speaker";
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
                "playSound", // Plays sound at resourceLocator
                "playNote" // Plays note
        };
    }

    @Override
    public Object[] callMethod( @Nonnull IComputerAccess computerAccess, @Nonnull ILuaContext context, int methodIndex, @Nonnull Object[] args ) throws LuaException
    {
        switch( methodIndex )
        {
            // playSound
            case 0:
            {
                return playSound(args, context, false);
            }

            // playNote
            case 1:
            {
                return playNote(args, context);
            }

            default:
            {
                throw new LuaException("Method index out of range!");
            }

        }
    }

    @Nonnull
    private synchronized Object[] playNote( Object[] arguments, ILuaContext context ) throws LuaException
    {
        float volume = 1.0f;
        float pitch = 1.0f;

        // Check if arguments are correct
        if( arguments.length == 0 ) // Too few args
        {
            throw new LuaException( "Expected string, number (optional), number (optional)" );
        }

        if( !(arguments[0] instanceof String) ) // Arg wrong type
        {
            throw new LuaException("Expected string, number (optional), number (optional)");
        }

        if ( !SoundEvent.REGISTRY.containsKey( new ResourceLocation( "block.note." + arguments[0] ) ) )
        {
            throw new LuaException("Invalid instrument, \"" + arguments[0] + "\"!");
        }

        if ( arguments.length > 1 )
        {
            if ( arguments[1] != null && !(arguments[1] instanceof Double) )   // Arg wrong type
            {
                throw new LuaException( "Expected string, number (optional), number (optional)" );
            }
            volume = arguments[1] != null ? ((Double) arguments[1]).floatValue() : 1f;
        }

        if( arguments.length > 2 )
        {
            if( arguments[2] != null && !(arguments[2] instanceof Double) )  // Arg wrong type
            {
                throw new LuaException("Expected string, number (optional), number (optional)");
            }
            pitch = arguments[2] != null ? ((Double) arguments[2]).floatValue() : 1f;
        }

        // If the resource location for note block notes changes, this method call will need to be updated
        Object[] returnValue = playSound(
            new Object[] {
                "block.note." + arguments[0],
                (double)Math.min( volume, 3f ),
                Math.pow( 2.0f, ( pitch - 12.0f ) / 12.0f)
            }, context, true
        );

        if( returnValue[0] instanceof Boolean && (Boolean) returnValue[0] )
        {
            m_notesThisTick++;
        }

        return returnValue;
    }

    @Nonnull
    private synchronized Object[] playSound( Object[] arguments, ILuaContext context, boolean isNote ) throws LuaException
    {

        float volume = 1.0f;
        float pitch = 1.0f;

        // Check if arguments are correct
        if( arguments.length == 0 ) // Too few args
        {
            throw new LuaException( "Expected string, number (optional), number (optional)" );
        }

        if( !(arguments[0] instanceof String) ) // Arg wrong type
        {
            throw new LuaException( "Expected string, number (optional), number (optional)" );
        }

        if( arguments.length > 1 )
        {
            if( arguments[1] != null && !(arguments[1] instanceof Double) )  // Arg wrong type
            {
                throw new LuaException( "Expected string, number (optional), number (optional)" );
            }

            volume = arguments[1] != null ? ((Double) arguments[1]).floatValue() : 1f;

        }

        if( arguments.length > 2 )
        {
            if( arguments[2] != null && !(arguments[2] instanceof Double) )  // Arg wrong type
            {
                throw new LuaException( "Expected string, number (optional), number (optional)" );
            }
            pitch = arguments[2] != null ? ((Double) arguments[2]).floatValue() : 1f;
        }

        ResourceLocation resourceName = new ResourceLocation( (String) arguments[0] );

        if( m_clock - m_lastPlayTime >= TileSpeaker.MIN_TICKS_BETWEEN_SOUNDS || ( ( m_clock - m_lastPlayTime == 0 ) && ( m_notesThisTick < ComputerCraft.maxNotesPerTick ) && isNote ) )
        {
            if( SoundEvent.REGISTRY.containsKey(resourceName) )
            {
                final World world = getWorld();
                final BlockPos pos = getPos();
                final ResourceLocation resource = resourceName;
                final float vol = volume;
                final float soundPitch = pitch;

                context.issueMainThreadTask(new ILuaTask()
                {
                    @Nullable
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        world.playSound( null, pos, SoundEvent.REGISTRY.getObject( resource ), SoundCategory.RECORDS, Math.min( vol, 3f ), soundPitch );
                        return null;
                    }
                });

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

