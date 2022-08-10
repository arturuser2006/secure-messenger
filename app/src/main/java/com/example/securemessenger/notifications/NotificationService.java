package com.example.securemessenger.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.Navigation;

import com.example.securemessenger.MainActivity;
import com.example.securemessenger.R;
import com.example.securemessenger.models.Message;
import com.example.securemessenger.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class NotificationService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(getApplicationContext(),"This is a Service running in Background", Toast.LENGTH_SHORT).show();
        sendNotification();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);

        super.onTaskRemoved(rootIntent);
    }

    private void sendNotification() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/user-messages/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    DatabaseReference childReference = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                            .getReference("/user-messages/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + snapshot.getKey());

                    childReference.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            Message message = snapshot.getValue(Message.class);

                            if (message.isReadReceiver() == false && message.getReceiverUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                                Log.d("MyLog", message.getId());

                                DatabaseReference senderReference = FirebaseDatabase.getInstance("https://messenger-project-1ab76-default-rtdb.europe-west1.firebasedatabase.app")
                                        .getReference("/users/" + message.getSenderUid());

                                senderReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        @NonNull
                                        User sender = snapshot.getValue(User.class);

                                        /*
                                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.add(Calendar.SECOND,  1);
                                        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_MUTABLE);
                                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                                         */

                                        Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
                                        intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        int messageId = (int) Math.random() * 100000;

                                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {

                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                                            stackBuilder.addNextIntentWithParentStack(intentMain);

                                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intentMain, PendingIntent.FLAG_IMMUTABLE);

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                                                    .setSmallIcon(R.drawable.logo)
                                                    .setContentTitle(sender.getName())
                                                    .setContentText(message.getText())
                                                    .setContentIntent(pendingIntent)
                                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                    .setAutoCancel(true);

                                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                            notificationManager.notify(messageId, builder.build());
                                        } else {
                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                                            stackBuilder.addNextIntentWithParentStack(intentMain);

                                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intentMain, PendingIntent.FLAG_IMMUTABLE);

                                            NotificationChannel channel = new NotificationChannel("ID", "name", NotificationManager.IMPORTANCE_DEFAULT);

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ID")
                                                    .setSmallIcon(R.drawable.logo)
                                                    .setContentTitle(sender.getName())
                                                    .setContentText(message.getText())
                                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                    .setContentIntent(pendingIntent)
                                                    .setAutoCancel(true);

                                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                            notificationManager.createNotificationChannel(channel);
                                            notificationManager.notify(messageId, builder.build());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
