package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TimeAPI implements ILuaAPI{

    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private String formatDate(String pattern, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    private long parseDate(String pattern, String value) throws LuaException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        long timestamp;
        try {
            timestamp = sdf.parse(value).getTime();
        } catch (ParseException parseException) {
            throw new LuaException("Date can't parsed");
        }
        return timestamp;
    }

    @Override
    public String[] getNames() {
        return new String[]{
                "time",
        };
    }

    @Override
    public void startup() {}

    @Override
    public void advance(double _dt) {}

    @Override
    public void shutdown() {}

    @Override
    public String[] getMethodNames() {
        return new String[] {
                "format",
                "getDay",
                "getMonth",
                "getYear",
                "getHour",
                "getMinute",
                "getSecound",
                "getTime",
                "parse"
        };
    }

    @Override
    public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {

        //timestamp like PHP without mills!

        Calendar cal = Calendar.getInstance();

        //If timestamp is given
        if (args.length > 0 && args[0] != null && args[0] instanceof Double) {
            long argTime = ((Double)args[0]).longValue()*1000;
            cal.setTimeInMillis(argTime);
        }

        Object[] returnArray = new Object[1];
        int calendarValue = -1;
        int addValue = 0;

        switch (method) {
            case 0:
                //format()
                String format = DEFAULT_TIME_FORMAT;
                if (args.length == 1 && args[0] instanceof String) {
                    format = (String) args[0];
                }
                else if (args.length == 2 && args[1] instanceof String) {
                    format = (String) args[1];
                }
                returnArray[0] = formatDate(format, cal.getTime());
                break;
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
                returnArray[0] = cal.getTimeInMillis() / 1000;
                break;
            case 8:
                //parse(String pattern, String input)
                if (args.length < 2 || args[0] == null || args[1] == null || !(args[0] instanceof String) || !(args[1] instanceof String)) {
                    throw new LuaException("String expected!");
                }
                String  pattern = (String) args[0],
                        input   = (String) args[1];
                returnArray[0] = parseDate(pattern, input) / 1000;
        }

        if (calendarValue != -1 && returnArray[0] == null) {
            returnArray[0] = cal.get(calendarValue)+addValue;
        }
        return returnArray;
    }
}
