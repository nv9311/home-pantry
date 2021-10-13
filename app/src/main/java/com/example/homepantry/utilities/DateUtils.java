package com.example.homepantry.utilities;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.homepantry.MainActivity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long daysTillExpiration(Context context, Date date){
        Instant instant = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
        long diff = date.getTime() - Date.from(instant).getTime();
        TimeUnit time = TimeUnit.DAYS;
        long difference = time.convert(diff, TimeUnit.MILLISECONDS);
        return difference;
    }
}
