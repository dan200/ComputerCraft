package dan200.computercraft.core.apis;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.Calendar;
import java.util.TimeZone;


public class TimeAPI extends LuaTable {

    private TimeZone timeZone;
    private Calendar calendarInstance;

    public TimeAPI() {
        timeZone = TimeZone.getDefault();
        calendarInstance = Calendar.getInstance( timeZone );
    }

    public void setTimeZone(String timeZoneId) {
        timeZone = TimeZone.getTimeZone(timeZoneId);
        calendarInstance = Calendar.getInstance( timeZone );
    }

    public LuaValue getResult(int method) {

        //timestamp like PHP without mills!

        Object returnValue = null;
        int calendarValue = -1;
        int addValue = 0;

        switch (method) {
            case 1:
                //getDay()
                calendarValue = Calendar.DAY_OF_MONTH;
                break;
            case 2:
                //getMonth()
                calendarValue = Calendar.MONTH;
                addValue = 1;
                break;
            case 3:
                //getYear()
                calendarValue = Calendar.YEAR;
                break;
            case 4:
                //getHour()
                calendarValue = Calendar.HOUR_OF_DAY;
                break;
            case 5:
                //getMinute()
                calendarValue = Calendar.MINUTE;
                break;
            case 6:
                //getSecound()
                calendarValue = Calendar.SECOND;
                break;
            case 7:
                //getTime()
                return wrap( Math.floor( calendarInstance.getTimeInMillis() / 1000 ) );
        }

        if (calendarValue != -1 && returnValue == null) {
            return wrap( calendarInstance.get(calendarValue)+addValue );
        }
        else
            return LuaValue.NIL;
    }

    @Override
    public LuaValue get(LuaValue luaValue) {

        String searched = luaValue.toString();

        if (searched.equals("day"))
            return getResult(1);
        else if (searched.equals("month"))
            return getResult(2);
        else if (searched.equals("year"))
            return getResult(3);
        else if (searched.equals("hour"))
            return getResult(4);
        else if (searched.equals("minute"))
            return getResult(5);
        else if (searched.equals("second"))
            return getResult(6);
        else if (searched.equals("timezone"))
            return wrap(timeZone.getID());
        else
            return super.get(luaValue);
    }

    @Override
    public void set(LuaValue key, LuaValue value) {
        if (key.toString().equals("timezone")) {
            setTimeZone( value.toString() );
        }
        else {
            super.set(key, value);
        }
    }

    private LuaValue wrap(double number) {
        return LuaValue.valueOf(number);
    }

    private LuaValue wrap(String value) {
        return LuaValue.valueOf(value);
    }
}
