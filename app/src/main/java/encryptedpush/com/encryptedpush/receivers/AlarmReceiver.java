package encryptedpush.com.encryptedpush.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import encryptedpush.com.encryptedpush.data.MessageData;
import encryptedpush.com.encryptedpush.services.SendPushJobIntentService;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";


    public static final String MESSAGE_TO_SHOW = "message_to_show";
    public static final String TITLE_TO_SHOW = "title_to_show";
    public static final String ENCRYPTED_MESSAGE = "encrypted_message";
    public static final String PAYLOAD = "payload";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Even though this is really a small thing to do (making one network request),
        // broadcast receiver should not do background work so it will be sent to jobIntentService
        String textMessageToShow="";
        String encryptedMessage="";
        String payload = "";
        String title="";
        MessageData messageData;
        Log.d(TAG,"onReceive");
        if(intent!=null && intent.getExtras()!=null)
        {
            //Apparently Alarm manager receivers have problem to get custom parcelable objects so I'm getting it as many strings
            // https://stackoverflow.com/a/39478479

            textMessageToShow =intent.getExtras().getString(MESSAGE_TO_SHOW,"");
            encryptedMessage =intent.getExtras().getString(ENCRYPTED_MESSAGE,"");
            title =intent.getExtras().getString(TITLE_TO_SHOW,"");
            payload =intent.getExtras().getString(PAYLOAD,"");
        }
        if(!encryptedMessage.isEmpty() && !textMessageToShow.isEmpty())
        {
            Intent jobIntent = new Intent(SendPushJobIntentService.INTENT_ACTION_SEND_PUSH);
            messageData = new MessageData(title,textMessageToShow,payload,encryptedMessage);
            jobIntent.putExtra(SendPushJobIntentService.INTENT_EXTRA_MESSAGE_DATA,messageData);

            Log.d(TAG,"Sending a job");
            SendPushJobIntentService.enqueueWork(context,SendPushJobIntentService.class,SendPushJobIntentService.JOB_ID,jobIntent);

        }
    }
}