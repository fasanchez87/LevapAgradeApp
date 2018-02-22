package com.ingeniapps.agradeser.fcm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ingeniapps.agradeser.activity.Principal;
import com.ingeniapps.agradeser.app.Config;
import com.ingeniapps.agradeser.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.agradeser.util.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private gestionSharedPreferences sharedPreferences;
    private JSONObject evento;

    private NotificationUtils notificationUtils;
    private int counterNoticias;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        sharedPreferences = new gestionSharedPreferences(getApplicationContext());

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null)
        {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0)
        {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData());

            try
            {
                Map<String, String> params = remoteMessage.getData();
                JSONObject object = new JSONObject(params);
                Log.e(TAG, "X - "+object.toString());
                // JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(object);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message)
    {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext()))
        {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();

            Log.e(TAG, "From: " + "");

        }
        else
        {
            // If the app is in background, firebase itself handles the notification
            Log.e(TAG, "From: " + "");
        }
    }

    private void handleDataMessage(JSONObject json)
    {
        Log.e(TAG, "push json: " + json.toString());
        try
        {
            //JSONObject data = json.getJSONObject("data");
            String title = json.getString("title");
            String message = json.getString("message");
            String imageUrl = json.getString("image");
            String timestamp = json.getString("timestamp");
            String keyMessage = json.getString("keyMessage");
            boolean isBackground = json.getBoolean("is_background");

            String payload = json.getString("data");

            if(!TextUtils.isEmpty(payload))
            {
                evento=new JSONObject(payload);
            }

            else
            {
                evento=null;
            }

            Log.e("ESC", "title: " + title);
            Log.e("ESC", "message: " + message);
            Log.e("ESC", "isBackground: " + isBackground);
            //Log.e("ESC", "payload: " + evento.toString());
            Log.e("ESC", "imageUrl: " + imageUrl);
            Log.e("ESC", "timestamp: " + timestamp);
            Log.e("ESC", "keyMessage: " + keyMessage);

            if (!isBackground)
            {
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext()))//IS FRONT APP
                {
                    if (keyMessage.equals("newVoto"))//PUSH NUEVA NOTICIA
                    {
                        Intent intent = new Intent(Config.PUSH_NOMINACION);
                        intent.putExtra("message", message);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(3000);
                        // play notification sound
                        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                        notificationUtils.playNotificationSound();
                    }
                }

                else

                if (NotificationUtils.isAppIsInBackground(getApplicationContext()))//IS BACKGROUND APP
                {
                    if (keyMessage.equals("newVoto"))
                    {
                        //CONTADOR DE NOTICIAS
                        //counterNoticias=0;
                       // sharedPreferences.putInt("counterNoticias", counterNoticias = sharedPreferences.getInt("counterNoticias")+1);
                        //Log.i("pushDespachado",""+sharedPreferences.getInt("counterNoticias"));
                        //BADGER CONTADOR ICONO
                        //ShortcutBadger.applyCount(this, sharedPreferences.getInt("counterNoticias"));


                        Log.i("jovo",""+message);


                        Intent resultIntent = new Intent(MyFirebaseMessagingService.this, Principal.class);

                        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
                        resultIntent.putExtra("indicaPush", "pushNominacion");
                        resultIntent.putExtra("message", message);
                        /*resultIntent.putExtra("notificarUsoTicket", notificarUsoTicket);
                        resultIntent.putExtra("ticket", ticket.toString());*/
                        PendingIntent pendingNot = PendingIntent.getActivity(getApplicationContext(),0,
                                resultIntent,0);

                        // check for image attachment
                        if (TextUtils.isEmpty(imageUrl))
                        {
                            showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
                        }

                        else
                        {
                            // image is present, show notification with image
                            showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                        }

                    }

                }
            }

        }

        catch (JSONException e)
        {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        }

        catch (Exception e)
        {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent)
    {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl)
    {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
