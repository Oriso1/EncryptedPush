package encryptedpush.com.encryptedpush.services;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import encryptedpush.com.encryptedpush.data.MessageData;
import encryptedpush.com.encryptedpush.network.NetworkManager;

public class SendPushJobIntentService extends JobIntentService {

    private static final String TAG = "SendPushJobIntentService";

    public static final int JOB_ID = 1234;
    public static final String INTENT_ACTION_SEND_PUSH = "send_push";
    public static final String INTENT_EXTRA_MESSAGE_DATA= "message_data";


    @Override
    protected void onHandleWork(@NonNull Intent intent)
    {
        Log.d(TAG,"onHandleWork");
        if(intent.getAction()!=null)
        {
            switch (intent.getAction())
            {
                case INTENT_ACTION_SEND_PUSH:
                    if(intent.getExtras()!=null)
                    {
                        MessageData messageData;
                        messageData = intent.getExtras().getParcelable(INTENT_EXTRA_MESSAGE_DATA);

                        if(messageData!=null)
                        {
                            Log.d(TAG,"sending notification");
                            NetworkManager.getInstance(getApplicationContext()).sendPushNotificationMessage(getApplicationContext(),messageData);
                        }
                    }
                    break;
            }
        }

    }
}
