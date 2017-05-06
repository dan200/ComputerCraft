/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.shared.util.StringUtil;

import java.util.*;

public class OSAPI implements ILuaAPI
{
    private IAPIEnvironment m_apiEnvironment;

    private final Map<Integer, Timer> m_timers;
    private final Map<Integer, Alarm> m_alarms;
    private int m_clock;
    private double m_time;
    private int m_day;

    private int m_nextTimerToken;
    private int m_nextAlarmToken;
    
    private static class Timer
    {
        public int m_ticksLeft;

        public Timer( int ticksLeft )
        {
            m_ticksLeft = ticksLeft;
        }
    }
    
    private class Alarm implements Comparable<Alarm>
    {
        public final double m_time;
        public final int m_day;

        public Alarm( double time, int day )
        {
            m_time = time;
            m_day = day;
        }

        @Override
        public int compareTo( Alarm o )
        {
            double t = (double)m_day * 24.0 + m_time;
            double ot = (double)m_day * 24.0 + m_time;
            if( t < ot ) {
                return -1;
            } else if( t > ot ) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    public OSAPI( IAPIEnvironment environment )
    {
        m_apiEnvironment = environment;
        m_nextTimerToken = 0;
        m_nextAlarmToken = 0;
        m_timers = new HashMap<Integer, Timer>();
        m_alarms = new HashMap<Integer, Alarm>();
    }
    
    // ILuaAPI implementation
    
    @Override
    public String[] getNames()
    {
        return new String[] {
            "os"
        };
    }
    
    @Override
    public void startup()
    {
        m_time = m_apiEnvironment.getComputerEnvironment().getTimeOfDay();
        m_day = m_apiEnvironment.getComputerEnvironment().getDay();
        m_clock = 0;

        synchronized( m_timers )
        {
            m_timers.clear();
        }

        synchronized( m_alarms )
        {
            m_alarms.clear();
        }
    }
    
    @Override
    public void advance( double dt )
    {
        synchronized( m_timers )
        {
            // Update the clock
            m_clock++;
            
            // Countdown all of our active timers
            Iterator<Map.Entry<Integer, Timer>> it = m_timers.entrySet().iterator();
            while( it.hasNext() )
            {
                Map.Entry<Integer, Timer> entry = it.next();
                Timer timer = entry.getValue();
                timer.m_ticksLeft = timer.m_ticksLeft - 1;
                if( timer.m_ticksLeft <= 0 )
                {
                    // Queue the "timer" event
                    queueLuaEvent( "timer", new Object[] { entry.getKey() } );
                    it.remove();
                }
            }
        }
        
        // Wait for all of our alarms
        synchronized( m_alarms )
        {
            double previousTime = m_time;
            int previousDay = m_day;
            double time = m_apiEnvironment.getComputerEnvironment().getTimeOfDay();
            int day =  m_apiEnvironment.getComputerEnvironment().getDay();
            
            if( time > previousTime || day > previousDay )
            {
                double now = (double)m_day * 24.0 + m_time;
                Iterator<Map.Entry<Integer, Alarm>> it = m_alarms.entrySet().iterator();
                while( it.hasNext() )
                {
                    Map.Entry<Integer, Alarm> entry = it.next();
                    Alarm alarm = entry.getValue();
                    double t = (double)alarm.m_day * 24.0 + alarm.m_time;
                    if( now >= t )
                    {
                        queueLuaEvent( "alarm", new Object[]{ entry.getKey() } );
                        it.remove();
                    }
                }
            }

            m_time = time;
            m_day = day;
        }
    }
    
    @Override
    public void shutdown( )
    {
        synchronized( m_timers )
        {
            m_timers.clear();
        }
        
        synchronized( m_alarms )
        {
            m_alarms.clear();
        }
    }

    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "queueEvent",
            "startTimer",
            "setAlarm",
            "shutdown",
            "reboot",
            "computerID",
            "getComputerID",
            "setComputerLabel",
            "computerLabel",
            "getComputerLabel",
            "clock",
            "time",
            "day",
            "cancelTimer",
            "cancelAlarm",
        };
    }

    @Override
    public Object[] callMethod( ILuaContext context, int method, Object[] args ) throws LuaException
    {
        switch( method )
        {
            case 0:
            {
                // queueEvent
                if( args.length == 0 || args[0] == null || !(args[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                queueLuaEvent( (String)args[0], trimArray( args, 1 ) );
                return null;
            }
            case 1:
            {
                // startTimer
                if( args.length < 1 || args[0] == null || !(args[0] instanceof Number) )
                {
                    throw new LuaException( "Expected number" );
                }
                double timer = ((Number)args[0]).doubleValue();
                synchronized( m_timers )
                {
                    m_timers.put( m_nextTimerToken, new Timer( (int)Math.round( timer / 0.05 ) ) );
                    return new Object[] { m_nextTimerToken++ };
                }
            }
            case 2:
            {
                // setAlarm
                if( args.length < 1 || args[0] == null || !(args[0] instanceof Number) )
                {
                    throw new LuaException( "Expected number" );
                }
                double time = ((Number)args[0]).doubleValue();
                if( time < 0.0 || time >= 24.0 )
                {
                    throw new LuaException( "Number out of range" );
                }                
                synchronized( m_alarms )
                {
                    int day = (time > m_time) ? m_day : (m_day + 1);
                    m_alarms.put( m_nextAlarmToken, new Alarm( time, day ) );
                    return new Object[] { m_nextAlarmToken++ };
                }
            }
            case 3:
            {
                // shutdown
                m_apiEnvironment.shutdown();
                return null;
            }
            case 4:
            {
                // reboot
                m_apiEnvironment.reboot();
                return null;
            }
            case 5:
            case 6:
            {
                // computerID/getComputerID
                return new Object[] { getComputerID() };
            }
            case 7:
            {
                // setComputerLabel
                String label = null;
                if( args.length > 0 && args[0] != null )
                {
                    if(!(args[0] instanceof String))
                    {
                        throw new LuaException( "Expected string or nil" );
                    }
                    label = StringUtil.normaliseLabel( (String) args[0] );
                }
                m_apiEnvironment.setLabel( label );
                return null;
            }
            case 8:
            case 9:
            {
                // computerLabel/getComputerLabel
                String label = m_apiEnvironment.getLabel();
                if( label != null )
                {
                    return new Object[] { label };
                }
                return null;
            }
            case 10:
            {
                // clock
                synchronized( m_timers )
                {
                    return new Object[] { (double)m_clock * 0.05 };
                }
            }
            case 11:
            {
                // m_time
                if (args.length == 0) {
                    synchronized (m_alarms) {
                        return new Object[]{m_time};
                    }
                }
                else if (args.length > 0 && args[0] != null && args[0] instanceof String) {
                    String param = (String) args[0];
                    //Get Hour of day (UTC)
                    if (param.equals("utc")) {
                        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        float hourOfDay = c.get(Calendar.HOUR_OF_DAY);
                        hourOfDay += ((float)c.get(Calendar.MINUTE)/60)+(float)(c.get(Calendar.SECOND)/60*60);
                        return new Object[] {hourOfDay};
                    }
                    //Get Hour of day (local time)
                    else if (param.equals("local")) {
                        Calendar c = Calendar.getInstance();
                        float hourOfDay = c.get(Calendar.HOUR_OF_DAY);
                        hourOfDay += ((float)c.get(Calendar.MINUTE)/60)+(float)(c.get(Calendar.SECOND)/60*60);
                        return new Object[] {hourOfDay};
                    }
                    //Get ingame hour
                    else if (param.equals("ingame")) {
                        return callMethod(context, method, new Object[0]);
                    }
                    //Get timestamp (without mills)
                    else if (param.equals("timestamp")) {
                        long timestamp = Calendar.getInstance().getTimeInMillis()/1000;
                        return new Object[]{timestamp};
                    }
                    else {
                        throw new LuaException("Unsupported operation");
                    }
                }
                else {
                    throw new LuaException("Expected string");
                }
            }
            case 12:
            {
                // day
                if (args.length == 0 ) {
                    synchronized (m_alarms) {
                        return new Object[]{m_day};
                    }
                }
                else if (args.length > 0 && args[0] != null && args[0] instanceof String) {
                    String param = (String) args[0];
                    //Get numbers of days since 1970-01-01 (utc)
                    if (param.equals("utc")) {
                        long timestamp = Calendar.getInstance().getTimeInMillis();
                        timestamp /= 86400000; //Secounds of a day
                        return new Object[] {timestamp};
                    }
                    //Get numbers of days since 1970-01-01 (local time)
                    else if (param.equals("local")) {
                        long timestamp = Calendar.getInstance().getTimeInMillis();
                        int offset = TimeZone.getDefault().getRawOffset();
                        timestamp += offset; //Add TZOffset to mills
                        timestamp /= 86400000; //Secounds of a day
                        return new Object[] {timestamp};
                    }
                    //Get game day
                    else if (param.equals("ingame")){
                        return callMethod(context, method, new Object[0]); //Normal os.day()
                    }
                    else {
                        throw new LuaException("Unsupported operation");
                    }
                }
                else {
                    throw new LuaException("Expected string");
                }
            }
            case 13:
            {
                // cancelTimer
                if( args.length < 1 || args[0] == null || !(args[0] instanceof Number) )
                {
                    throw new LuaException( "Expected number" );
                }
                int token = ((Number)args[0]).intValue();
                synchronized( m_timers )
                {
                    if( m_timers.containsKey( token ) )
                    {
                        m_timers.remove( token );
                    }
                }
                return null;
            }
            case 14:
            {
                // cancelAlarm
                if( args.length < 1 || args[0] == null || !(args[0] instanceof Number) )
                {
                    throw new LuaException( "Expected number" );
                }
                int token = ((Number)args[0]).intValue();
                synchronized( m_alarms )
                {
                    if( m_alarms.containsKey( token ) )
                    {
                        m_alarms.remove( token );
                    }
                }
                return null;
            }
            default:
            {
                return null;
            }
        }
    }

    // Private methods

    private void queueLuaEvent( String event, Object[] args )
    {
        m_apiEnvironment.queueEvent( event, args );
    }
    
    private Object[] trimArray( Object[] array, int skip )
    {
        return Arrays.copyOfRange( array, skip, array.length );
    }
    
    private int getComputerID()
    {
        return m_apiEnvironment.getComputerID();
    }
}
