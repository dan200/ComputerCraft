package dan200.computercraft.core.apis;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by timia2109 (Tim Ittermann) on 04.05.2017.
 *
 * @author timia2109 (Tim Ittermann)
 */
public class TimeUtilsAPI implements ILuaAPI {

    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd";

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
                "timeutils"
        };
    }

    @Override
    public void startup() {
    }

    @Override
    public void advance(double _dt) {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public String[] getMethodNames() {
        return new String[]{
                "format",
                "parse"
        };
    }

    @Override
    public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {
        Calendar cal = Calendar.getInstance();

        if (args.length > 0 && args[0] != null && args[0] instanceof Double) {
            long argTime = ((Double) args[0]).longValue() * 1000;
            cal.setTimeInMillis(argTime);
        }

        switch (method) {
            case 0:
                String format = DEFAULT_TIME_FORMAT;
                if (args.length == 1 && args[0] instanceof String) {
                    format = (String) args[0];
                } else if (args.length == 2 && args[1] instanceof String) {
                    format = (String) args[1];
                }
                return new Object[]{formatDate(format, cal.getTime())};
            case 1:
                //parse(String pattern, String input)
                if (args.length < 2 || args[0] == null || args[1] == null || !(args[0] instanceof String) || !(args[1] instanceof String)) {
                    throw new LuaException("String expected!");
                }
                String pattern = (String) args[0],
                        input = (String) args[1];
                return new Object[]{parseDate(pattern, input) / 1000};
        }
        return null;
    }
}
