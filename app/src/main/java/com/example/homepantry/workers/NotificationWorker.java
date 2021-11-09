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

import java.util.ArrayList;
import java.util.List;

public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int addedToNotification = 0;
        int notificationSize = 6;
        int numOfSoonToBeExpired;
        int addedToNotificationSoonExpired = 0;

        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(context);
        List<Item> itemListExpired = db.itemDao().getExpiredItems();
        List<Item> soonToBeExpiredItems = db.itemDao().getSoonToBeExpiredItems();

        ArrayList<String> dataArray = new ArrayList<>();
        if(itemListExpired.size() > 0){
            for (int i = 0; i < itemListExpired.size(); i++) {
                Item item = itemListExpired.get(i);
                dataArray.add(expiredNotificationString(context, item));
                addedToNotification++;
            }

        }
        numOfSoonToBeExpired = Math.min(notificationSize - addedToNotification, soonToBeExpiredItems.size());

        for (int i = 0; i < numOfSoonToBeExpired; i++) {
            Item item = soonToBeExpiredItems.get(i);
            long difference = abs(DateUtils.daysTillExpiration(item.expirationDate));
            if (difference <= 1){
                dataArray.add(0 ,soonToBeExpiredString(context, item));
                addedToNotificationSoonExpired++;
            }
        }

        String contentText = contentTextString(context, addedToNotification, addedToNotificationSoonExpired);

        if(!dataArray.isEmpty()) {
            NotificationUtils.createNotification(context, contentText, dataArray);
        }
        return Result.success();
    }
    private String expiredNotificationString(Context context, Item item){
        String hasExpired = context.getString(R.string.notification_has_expired);
        String daysAgo = context.getString(R.string.notification_days_ago);
        String notification = item.itemName + " " + hasExpired + " ";
        long difference = abs(DateUtils.daysTillExpiration(item.expirationDate));
        notification += Long.toString(difference);

        notification = notification + " " + daysAgo;
        return notification;
    }
    private String soonToBeExpiredString(Context context, Item item){
        String willExpire = context.getString(R.string.notification_will_expire);
        return item.itemName + " " + willExpire + "!";
    }
    private String contentTextString(Context context, int numOfExpired, int numOfSoonExpired){
        String willExpireInOneDay = context.getString(R.string.notification_will_expire_in_one_day);
        String returnString = "";
        if(numOfExpired > 0){
            returnString = returnString + numOfExpired + " "
                    + context.getString(R.string.notification_products_have_expired) + " ";
        }
        if(numOfSoonExpired > 0){
            returnString = returnString
                    + numOfSoonExpired + " "
                    + willExpireInOneDay;
        }
        return returnString;
    }
}
