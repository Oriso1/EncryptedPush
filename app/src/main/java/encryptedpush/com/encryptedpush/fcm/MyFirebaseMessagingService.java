package encryptedpush.com.encryptedpush.fcm;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import encryptedpush.com.encryptedpush.R;
import encryptedpush.com.encryptedpush.data.NotificationData;
import encryptedpush.com.encryptedpush.ui.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService  {

    private static final String TAG = "MyFirebaseMessagingService";

    public static final String FCM_TOKEN = "fcm_token";
    public static final String TYPE_ENCRYPTED = "type_encrypted";

    private final String ADMIN_CHANNEL_ID ="admin_channel";


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i(TAG, "onTokenRefresh completed with token: " + token);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FCM_TOKEN,token);
        editor.apply();


    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG,"Got new push message");
        Gson gson = new GsonBuilder().serializeNulls().create();
        NotificationData notificationData = gson.fromJson(gson.toJson(remoteMessage.getData()), NotificationData.class);

        // in case there will be more types of push messages
        if(notificationData.getType() != null && notificationData.getType().equals(TYPE_ENCRYPTED))
        {
            final Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.FROM_NOTIFICATION,true);
            intent.putExtra(MainActivity.NOTIFICATION_DATA,notificationData);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationID = new Random().nextInt(3000);

            setupChannels(notificationManager);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this , notificationID, intent, PendingIntent.FLAG_ONE_SHOT);

            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationData.getTitle())
                    .setContentText(notificationData.getMessage())
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            notificationManager.notify(notificationID, notificationBuilder.build());
        }


    }

    private void setupChannels(NotificationManager notificationManager){
        CharSequence adminChannelName = "Notification";
        String adminChannelDescription = "Device to device notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        adminChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }


}