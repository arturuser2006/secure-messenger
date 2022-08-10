package com.example.securemessenger.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.securemessenger.MainActivity;
import com.example.securemessenger.R;
import com.example.securemessenger.models.Message;
import com.example.securemessenger.models.User;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("MyLog", "OnReceive");

        Intent intentMain = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int messageId = (int) Math.random() * 100000;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(intentMain);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentMain, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(((User)intent.getExtras().getParcelable("USER")).getName())
                    .setContentText(((Message)intent.getExtras().getParcelable("MESSAGE")).getText())
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(messageId, builder.build());
        } else {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(intentMain);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentMain, PendingIntent.FLAG_IMMUTABLE);

            NotificationChannel channel = new NotificationChannel("ID", "name", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ID")
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(MainActivity.sender.getName())
                    .setContentText(MainActivity.message.getText())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(messageId, builder.build());
        }
    }
}