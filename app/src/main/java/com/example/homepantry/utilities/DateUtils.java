package com.example.homepantry.utilities;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    public static long daysTillExpiration(Date date){

        //Midnight current date
        Calendar today = new GregorianCalendar();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        //Midnight expiry date
        Calendar calendarDate = new GregorianCalendar();
        calendarDate.setTime(date);
        calendarDate.set(Calendar.HOUR_OF_DAY, 0);
        calendarDate.set(Calendar.MINUTE, 0);
        calendarDate.set(Calendar.SECOND, 0);
        calendarDate.set(Calendar.MILLISECOND, 0);

        long diff = calendarDate.getTime().getTime() - today.getTime().getTime();
        TimeUnit time = TimeUnit.DAYS;

        return time.convert(diff, TimeUnit.MILLISECONDS);
    }
}
