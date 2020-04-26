package encryptedpush.com.encryptedpush.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import encryptedpush.com.encryptedpush.utils.GeneralUtils;
import encryptedpush.com.encryptedpush.data.MessageData;
import encryptedpush.com.encryptedpush.data.NotificationData;
import encryptedpush.com.encryptedpush.fcm.MyFirebaseMessagingService;

public class FcmRequests
{
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    // Key like this should never be saved hardcoded on code,
    // but since I don't have server for this assignment it is stored like this
    final private String SERVER_KEY = "key=" + "AAAAecbOrYo:APA91bGgynv5IPywA9MAN2wgFTAViLtM7LCroDfnWU69bzGxHxHDapJ7E2zzZuqDS9VMPIy7bEntj1wmPGdrzWgG0Y4Dgz7fz6VF0sW6lbNz_gesD6Y4glvX3wVJuFJU9CRhKfZ1TZEs";
    final private String CONTENT_TYPE_VALUE = "application/json";
    final private String CONTENT_TYPE = "Content-Type";
    final private String TO_TOKEN = "to";
    final private String FCM_DATA = "data";



    final private String TAG = "FcmRequests";
    final private String AUTHORIZATION = "Authorization";

    public JsonObjectRequest sendNotificationRequest(Context context, MessageData messageData) {

        String fcmToken = GeneralUtils.getFCMToken(context);
        JSONObject jsonObject = createJsonForSending(fcmToken,messageData);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse: "+error);
                    }
                }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(AUTHORIZATION, SERVER_KEY);
                params.put(CONTENT_TYPE, CONTENT_TYPE_VALUE);
                return params;
            }
        };
        return jsonObjectRequest;
    }

    private JSONObject createJsonForSending(String token,MessageData messageData)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            NotificationData notificationData = new NotificationData(messageData,MyFirebaseMessagingService.TYPE_ENCRYPTED);
            jsonObject.put(TO_TOKEN,token);
            String json = new Gson().toJson(notificationData);
            // Apparently this request must get JSONObject so I must create new JSONObject for sending...
            jsonObject.put(FCM_DATA, new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }



}
