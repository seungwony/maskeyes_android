package co.giftree.maskeyes.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import co.giftree.maskeyes.MainV2Activity;
import co.giftree.maskeyes.R;

public class MyFirebaseMsgService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";





    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "From: " + remoteMessage.getFrom());




        Log.d(TAG, "Notification Message Data: " + remoteMessage.getData().toString());

        String title = remoteMessage.getData().get("title");

//        String nickname = remoteMessage.getData().get("nickname");
        String msg = remoteMessage.getData().get("message");

        String customContent = remoteMessage.getData().get("customContent");



        sendNotification(title , msg);

//        onArrival(customContent);

    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String msgTitle, String messageBody) {
        Intent intent = new Intent(this, MainV2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        long[] pattern = { 1000 };

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(msgTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
//                .setSound(sound)
                .setVibrate(pattern)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }

    private void mainActivity(Context context){
        Intent intent = new Intent();

        intent.setClass(context.getApplicationContext(), MainV2Activity.class);
        context.getApplicationContext().startActivity(intent);
    }



//    private void onArrival(String customContentString){
//
//
//        Log.d(TAG, "arrival : " + customContentString);
//        if (!TextUtils.isEmpty(customContentString)) {
//            JSONObject customJson = null;
//            try {
//                customJson = new JSONObject(customContentString);
//                String myvalue = null;
//                String id = null;
//                String cars_id = null;
//                String key_secret = null;
//                String start_date = null;
//                String end_date = null;
//                boolean is_Unlimited = false;
//                String type = null;
//
//                if(customJson.has("type")){
//                    type = customJson.getString("type");
//                }
//
//                if(customJson.has("id")){
//                    id = customJson.getString("id");
//                }
//
//                if(customJson.has("cars_id")){
//                    cars_id = customJson.getString("cars_id");
//                }
//
//                if(customJson.has("key_secret")){
//                    key_secret = customJson.getString("key_secret");
//                }
//
//                if(customJson.has("start_time")){
//                    start_date = customJson.getString("start_time");
//                }
//
//                if(customJson.has("end_time")){
//                    end_date = customJson.getString("end_time");
//                }
//
//                if(customJson.has("is_unlimited")){
//                    is_Unlimited = customJson.getString("is_unlimited").equals("1") ;
//                }
//
//
//                if(start_date!=null && end_date != null){
//
//                    RentPeriod rent = new RentPeriod();
//                    rent.setId(cars_id);
//
//                    if(Integer.valueOf(type) == Alarm.DONE_REQ_SECRET_KEY){
//                        rent.setStartDate(StringUtil.prettyDate(start_date));
//                        rent.setEndDate(StringUtil.prettyDate(end_date));
//                    }else{
//                        rent.setStartDate(start_date);
//                        rent.setEndDate(end_date);
//                    }
//
//                    rent.setUnlimited(is_Unlimited);
//
//                    RentManager.save(getApplicationContext(), cars_id, rent);
//
//                    broadcastUpdate(Alarm.RENT_INFO_HAS_BEEN_CHANGED);
//                }else{
//                    Log.e(TAG, "start_date and end_date are empty.");
//                }
//
//
//                if(type!=null){
//                    if(Integer.valueOf(type) == Alarm.DONE_REQ_SECRET_KEY){
//
//                        savePin(cars_id, key_secret);
//                    }else if(Integer.valueOf(type) == Alarm.REQ_SECRET_KEY){
//
//                    }else if(Integer.valueOf(type) == Alarm.LEND_CAR){
//                        savePin(cars_id, key_secret);
//                    }else if(Integer.valueOf(type) == Alarm.DONE_EXTEND_DATE_LEND){
////                        savePin(cars_id, key_secret);
//                    }else if(Integer.valueOf(type) == Alarm.LOG_OUT){
//
//                        UserManagement.requestLogout(new LogoutResponseCallback() {
//                            @Override
//                            public void onCompleteLogout() {
////                redirectLoginActivity();
//                                Log.d(TAG, "onCompleteLogout");
//                            }
//                        });
//
//                        broadcastUpdate(BleControlMsg.REQ_DISCONNECT_DEVICE);
//
//
//                        if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
//                                .remove("uid")
//                                .remove("access_token")
//                                .remove("social_id")
//                                .remove("user_type")
//                                .remove("main_key")
//                                .commit()) {
//
//                            ApiManager.getInstance().setCredentials(ApiManager.default_credentials);
//                            broadcastUpdate(Alarm.LOGOUT);
//                        }
//                    }
//
//                }
//
//            } catch (JSONException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
