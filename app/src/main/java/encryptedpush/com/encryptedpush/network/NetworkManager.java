package encryptedpush.com.encryptedpush.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import encryptedpush.com.encryptedpush.data.MessageData;

public class NetworkManager {

    private static NetworkManager instance;
    private RequestQueue requestQueue;
    private static final Object syncObject = new Object();

    private FcmRequests fcmRequests;

    private NetworkManager(Context context) {
        requestQueue = getRequestQueue(context);
        fcmRequests = new FcmRequests();
    }

    public static NetworkManager getInstance(Context context) {
        if (instance == null) {
            synchronized (syncObject)
            {
                if(instance == null)
                    instance = new NetworkManager(context);
            }
        }
        return instance;
    }

    private RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {
            // getApplicationContext() keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    private  <T> void addToRequestQueue(Context context,Request<T> req) {
        getRequestQueue(context).add(req);
    }

    public void sendPushNotificationMessage(Context context,MessageData messageData)
    {
        JsonObjectRequest jsonObject = fcmRequests.sendNotificationRequest(context, messageData);
        addToRequestQueue(context,jsonObject);

    }
}