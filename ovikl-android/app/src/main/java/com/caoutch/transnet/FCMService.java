package com.caoutch.transnet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class FCMService extends FirebaseMessagingService {

    String TAG = "FCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());



        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
        User user = User.getInstance();


        if(user.email!=null&&token!=null&&(user.fcmToken==null||!token.contentEquals(user.fcmToken))){
            MyVolley myVolley = MyVolley.getInstance(this);
            RequestQueue queue = myVolley.getRequestQueue();
            Log.d("FCMService", token);
            user.fcmToken=token;
            //user.saveUser();
            Map<String, String> params = new HashMap<>();
            params.put("_id", user._id);
            params.put("hashedKey", user.hashedKey);
            params.put("fcmToken", user.fcmToken);
            params.put("iosToken", "");
            GsonRequest<GsonResponse> myReq = new GsonRequest<GsonResponse>(Request.Method.POST,
                    Constants.nodejs_index_url + "/register2/",
                    GsonResponse.class,
                    params,
                    createGetSuccessListener(),
                    createGetErrorListener());
            queue.add(myReq);
        }
    }

    private Response.Listener<GsonResponse> createGetSuccessListener() {
        Log.i(TAG, "createGetUserSuccessListener2");
        return new Response.Listener<GsonResponse>() {
            @Override
            public void onResponse(GsonResponse response) {

            }
        };
    }

    private Response.ErrorListener createGetErrorListener() {
        Log.i(TAG,"createGetUserErrorListener2");
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "json error: "+ error.getMessage());
                User user = User.getInstance();
                user.fcmToken=null;
                User.setInstance(user);

            }
        };
    }


}
