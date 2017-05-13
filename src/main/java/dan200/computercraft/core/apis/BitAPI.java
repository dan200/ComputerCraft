/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;

// Contributed by Nia
// Based on LuaBit (http://luaforge.net/projects/bit)
 
public class BitAPI implements ILuaAPI
{
    private static final int BNOT                    = 0;
    private static final int BAND                    = 1;
    private static final int BOR                     = 2;
    private static final int BXOR                    = 3;
    private static final int BRSHIFT                  = 4;
    private static final int BLSHIFT                  = 5;
    private static final int BLOGIC_RSHIFT              = 6;
 
    private static int checkInt( Object o, int count ) throws LuaException
    {
        if( o instanceof Number )
        {
            return (int)(((Number)o).longValue());
        }
        else
        {
            if( count == 2 )
            {
                throw new LuaException( "Expected number, number" );
            }
            else
            {
                throw new LuaException( "Expected number" );
            }
        }
    }
    
    public BitAPI( IAPIEnvironment _environment )
    {
    }
    
    @Override
    public String[] getNames()
    {
        return new String[] {
            "bit"
        };
    }

    @Override
    public void startup( )
    {
    }

    @Override
    public void advance( double _dt )
    {
    }
    
    @Override
    public void shutdown( )
    {
    }
    
    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[] {
            "bnot", "band", "bor", "bxor",
            "brshift", "blshift", "blogic_rshift"
        };
    }
    
    @Override
    public Object[] callMethod( @Nonnull ILuaContext context, int method, @Nonnull Object[] args ) throws LuaException
    {
        Object a = args.length>0?args[0]:null;
        Object b = args.length>1?args[1]:null;
 
        int ret = 0;
        switch(method) {
            case BNOT:
                ret = ~checkInt(a, 1);
                break;
            case BAND:
                ret = checkInt(a, 2) & checkInt(b, 2);
                break;
            case BOR:
                ret = checkInt(a, 2) | checkInt(b, 2);
                break;
            case BXOR:
                ret = checkInt(a, 2) ^ checkInt(b, 2);
                break;
            case BRSHIFT:
                ret = checkInt(a, 2) >> checkInt(b, 2);
                break;
            case BLSHIFT:
                ret = checkInt(a, 2) << checkInt(b, 2);
                break;
            case BLOGIC_RSHIFT:
                ret = checkInt(a, 2) >>> checkInt(b, 2);
                break;
        }
        
        return new Object[]{ ret&0xFFFFFFFFL };
    }
}
