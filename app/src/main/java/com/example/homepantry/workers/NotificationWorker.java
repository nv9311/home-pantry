package com.example.homepantry.workers;

import static java.lang.Math.abs;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.homepantry.R;
import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;
import com.example.homepantry.utilities.DateUtils;
import com.example.homepantry.utilities.NotificationUtils;

import java.util.List;

public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(context);
        List<Item> itemListExpired = db.itemDao().getExpiredItems();
        List<Item> soonTobeExpired = db.itemDao().getSoonToBeExpiredItems();

        String [] dataArray = new String[]{"", ""};
        if(itemListExpired.size() > 1){
            for (int i = 0; i < itemListExpired.size(); i++) {
                Item item = itemListExpired.get(i);
                dataArray[i] = expiredNotificationString(context, item);
            }

        }
        else if(itemListExpired.size() == 1){
            dataArray[0] = expiredNotificationString(context, itemListExpired.get(0));
            if(soonTobeExpired.size()> 0) {
                dataArray[1] = soonToBeExpiredString(context, soonTobeExpired.get(0));
            }
        }
        else{
            for (int i = 0; i < soonTobeExpired.size(); i++) {
                Item item = soonTobeExpired.get(i);
                dataArray[i] = soonToBeExpiredString(context, item);
            }
        }
        if(!dataArray[0].equals("")) {
            NotificationUtils.createNotification(context, dataArray[0], dataArray[1]);
        }
        return Result.success();
    }
    private String expiredNotificationString(Context context, Item item){
        String hasExpired = context.getString(R.string.notification_has_expired);
        String daysAgo = context.getString(R.string.notification_days_ago);
        String notification = item.itemName + " " + hasExpired + " ";
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            long difference = abs(DateUtils.daysTillExpiration(context, item.expirationDate));
            notification += Long.toString(difference);
        }
        notification = notification + " " + daysAgo;
        return notification;
    }
    private String soonToBeExpiredString(Context context, Item item){
        String willExpireIn = context.getString(R.string.notification_will_expire_in);
        String days = context.getString(R.string.notification_days);
        String notificationString = item.itemName + " " + willExpireIn + " ";
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            long difference = DateUtils.daysTillExpiration(context, item.expirationDate);
            notificationString += Long.toString(difference);
        }
        notificationString = notificationString + " " + days;
        return notificationString;
    }
}
