package co.giftree.maskeyes.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import co.giftree.maskeyes.Constants;
import co.giftree.maskeyes.MainActivity;
import co.giftree.maskeyes.R;
import co.giftree.maskeyes.api.ApiManager;
import co.giftree.maskeyes.model.MaskInfo;
import co.giftree.maskeyes.service.GpsTracker;
import co.giftree.maskeyes.util.DataUtil;
import co.giftree.maskeyes.util.DateUtil;
import co.giftree.maskeyes.util.FileUtil;
import co.giftree.maskeyes.util.GPSUtil;
import co.giftree.maskeyes.util.JsonUtil;
import co.giftree.maskeyes.util.PreferenceUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = WidgetProvider.class.getSimpleName();
    private static final String ACTION_APP_LAUNCH = "ACTION_APP_LAUNCH";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        if(ACTION_APP_LAUNCH.equals(intent.getAction())){

//            Log.d(TAG, "ACTION_APP_LAUNCH");
            PackageManager manager = context.getPackageManager();

            Intent i = manager.getLaunchIntentForPackage(context.getPackageName());
//                    i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);

        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        updateAppWidget(context);

    }
    public static void updateAppWidget(Context context) {


        GpsTracker gpsTracker = new GpsTracker(context);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String address = GPSUtil.getCurrentAddress(context, latitude, longitude);

        Date date = new Date();

        String currentDate = DateUtil.convertedSimpleFormat(date);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_summary);

        views.setTextViewText(R.id.update_addr_txt, address);
        views.setTextViewText(R.id.update_date_txt, currentDate);


        String targetStr = DateUtil.todayWeekDay(context) + " ("+ DateUtil.todayTarget(context) + context.getString(R.string.last_format_target)+") ";

        views.setTextViewText(R.id.day_info, targetStr);


//        ComponentName appWidget = new ComponentName(context, WidgetProvider.class);
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//        appWidgetManager.updateAppWidget(appWidget, views);




        getMaskInfo(context, views, String.valueOf(latitude), String.valueOf(longitude), String.valueOf(PreferenceUtil.range_meter(context)));

//        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    private static void intentAction(Context context, RemoteViews views, int res, String action){
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(action);
        // And this time we are sending a broadcast with getBroadcast
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(res, pendingIntent);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }


    private static  void getMaskInfo(final Context context, final RemoteViews views, final String currlat, final String currlng, String meter ){

        final Call<JsonElement> call = ApiManager.getInstance().getApiCommonService().storesByGeo(currlat, currlng, meter);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.isSuccessful()) {
                    // tasks available
                    JsonElement element = response.body();

//                    Log.d(TAG, "storesByGeo : " + element.toString());

                    if (element.getAsJsonObject().has("message")) {


                    }


                    ArrayList<MaskInfo> temps = new ArrayList<>();
                    if(element.getAsJsonObject().has("stores")) {


                        if (element.getAsJsonObject().get("stores") instanceof JsonArray) {
                            if (element.getAsJsonObject().get("stores").getAsJsonArray().size() > 0) {
                                for (JsonElement item : element.getAsJsonObject().get("stores").getAsJsonArray()) {
                                    String addr = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "addr");
                                    String code = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "code");
                                    String created_at = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "created_at");
                                    String lat = JsonUtil.hasJsonAndGetNumberString(item.getAsJsonObject(), "lat");
                                    String lng = JsonUtil.hasJsonAndGetNumberString(item.getAsJsonObject(), "lng");
                                    String name = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "name");

                                    String type = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "type");
                                    String remain_stat = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "remain_stat");
                                    String stock_at = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "stock_at");



                                    MaskInfo maskInfo = new MaskInfo();
                                    maskInfo.setName(name);
                                    maskInfo.setAddr(addr);
                                    maskInfo.setCode(code);
                                    maskInfo.setCreated_at(created_at);

                                    maskInfo.setType(type);
                                    maskInfo.setRemain_stat(remain_stat);
                                    maskInfo.setStock_at(stock_at);

                                    maskInfo.setLat(Float.parseFloat(lat));
                                    maskInfo.setLng(Float.parseFloat(lng));


//                                    if (!existMaskInfo(maskInfo)) {
//
//                                    }
                                    temps.add(maskInfo);
                                }

                            }
                        }
                    }


                    double currentLat = Double.valueOf(currlat);
                    double currentLng = Double.valueOf(currlng);


                    for( MaskInfo maskInfo : temps){
                        double comparedLat =  maskInfo.getLat();
                        double comparedLng =  maskInfo.getLng();

                        double distance = GPSUtil.distance(currentLat, currentLng, comparedLat, comparedLng);

                        maskInfo.setDistance(distance);
                    }



                    Collections.sort(temps, new Comparator<MaskInfo>() {
                        @Override
                        public int compare(MaskInfo o1, MaskInfo o2) {


                            return Double.compare(o1.getDistance(), o2.getDistance());
                        }


                    });


//                    boolean enable_smart_widget =  PreferenceManager.getDefaultSharedPreferences(context).getBoolean("smart_widget_enable", true);

                    int smart_widget_option = PreferenceUtil.smart_widget_option(context);
//                    showList(temps);

                    if(smart_widget_option == 0){
                        Collections.sort(temps, new Comparator<MaskInfo>() {
                            @Override
                            public int compare(MaskInfo o1, MaskInfo o2) {


                                int com1 = o1.getRemain_stat().equals("empty") || o1.getRemain_stat().equals("break") || o1.getRemain_stat().equals("") ? 1 : -1;
                                int com2 = o2.getRemain_stat().equals("empty") || o2.getRemain_stat().equals("break") || o2.getRemain_stat().equals("") ? 1 : -1;


                                return Integer.compare(com1, com2);
                            }


                        });
                    }else if(smart_widget_option == 1){
                        Collections.sort(temps, new Comparator<MaskInfo>() {
                            @Override
                            public int compare(MaskInfo o1, MaskInfo o2) {


                                int com1 = o1.getRemain_stat().equals("empty") ? 1 : -1;
                                int com2 = o2.getRemain_stat().equals("empty") ? 1 : -1;


                                return Integer.compare(com1, com2);
                            }


                        });
                    }


                    if( temps.size() > 2 ) {

                        MaskInfo first = temps.get(0);
                        MaskInfo second = temps.get(1);
                        MaskInfo third = temps.get(2);

                        views.setViewVisibility(R.id.addr_view, View.GONE);



                        views.setImageViewResource(R.id.type_icon, DataUtil.findTypeRes(first.getType()));
                        views.setTextViewText(R.id.name_txt, first.getName());
                        views.setTextViewText(R.id.name_remain_cnt, DataUtil.convertReadableRemainStat(context, first.getRemain_stat()));
                        views.setTextColor(R.id.name_remain_cnt, DataUtil.convertColorRemainStat(context, first.getRemain_stat()));

                        views.setImageViewResource(R.id.type_icon2, DataUtil.findTypeRes(second.getType()));
                        views.setTextViewText(R.id.name2_txt, second.getName());
                        views.setTextViewText(R.id.name2_remain_cnt, DataUtil.convertReadableRemainStat(context, second.getRemain_stat()));
                        views.setTextColor(R.id.name2_remain_cnt, DataUtil.convertColorRemainStat(context, second.getRemain_stat()));


                        views.setImageViewResource(R.id.type_icon3, DataUtil.findTypeRes(third.getType()));
                        views.setTextViewText(R.id.name3_txt, third.getName());
                        views.setTextViewText(R.id.name3_remain_cnt, DataUtil.convertReadableRemainStat(context, third.getRemain_stat()));
                        views.setTextColor(R.id.name3_remain_cnt, DataUtil.convertColorRemainStat(context, third.getRemain_stat()));

                    }else if( temps.size() == 2 ) {

                        MaskInfo first = temps.get(0);
                        MaskInfo second = temps.get(1);

                        views.setViewVisibility(R.id.addr_view, View.VISIBLE);

                        views.setImageViewResource(R.id.type_icon, DataUtil.findTypeRes(first.getType()));
                        views.setTextViewText(R.id.name_txt, first.getName());
                        views.setTextViewText(R.id.name_remain_cnt, DataUtil.convertReadableRemainStat(context, first.getRemain_stat()));
                        views.setTextColor(R.id.name_remain_cnt, DataUtil.convertColorRemainStat(context, first.getRemain_stat()));

                        views.setImageViewResource(R.id.type_icon2, DataUtil.findTypeRes(second.getType()));
                        views.setTextViewText(R.id.name2_txt, second.getName());
                        views.setTextViewText(R.id.name2_remain_cnt, DataUtil.convertReadableRemainStat(context, second.getRemain_stat()));
                        views.setTextColor(R.id.name2_remain_cnt, DataUtil.convertColorRemainStat(context, second.getRemain_stat()));

                        views.setImageViewResource(R.id.type_icon3, 0);
                        views.setTextViewText(R.id.name3_txt, "");
                        views.setTextViewText(R.id.name3_remain_cnt, "");
                    }else if ( temps.size() == 1 ) {
                        MaskInfo first = temps.get(0);
                        views.setViewVisibility(R.id.addr_view, View.VISIBLE);

                        views.setImageViewResource(R.id.type_icon, DataUtil.findTypeRes(first.getType()));
                        views.setTextViewText(R.id.name_txt, first.getName());
                        views.setTextViewText(R.id.name_remain_cnt, DataUtil.convertReadableRemainStat(context, first.getRemain_stat()));

                        views.setTextColor(R.id.name_remain_cnt, DataUtil.convertColorRemainStat(context, first.getRemain_stat()));

                        views.setImageViewResource(R.id.type_icon2, 0);
                        views.setTextViewText(R.id.name2_txt, "");
                        views.setTextViewText(R.id.name2_remain_cnt, "");

                        views.setImageViewResource(R.id.type_icon3, 0);
                        views.setTextViewText(R.id.name3_txt, "");
                        views.setTextViewText(R.id.name3_remain_cnt, "");
                    }else {
                        views.setViewVisibility(R.id.addr_view, View.VISIBLE);
                        views.setImageViewResource(R.id.type_icon, 0);
                        views.setTextViewText(R.id.name_txt, "");
                        views.setTextViewText(R.id.name_remain_cnt, "");

                        views.setImageViewResource(R.id.type_icon2, 0);
                        views.setTextViewText(R.id.name2_txt, "");
                        views.setTextViewText(R.id.name2_remain_cnt, "");

                        views.setImageViewResource(R.id.type_icon3, 0);
                        views.setTextViewText(R.id.name3_txt, "");
                        views.setTextViewText(R.id.name3_remain_cnt, "");

                    }

                    intentAction(context, views, R.id.root_view, ACTION_APP_LAUNCH);
//                    maskInfos.clear();


//                    maskInfos.addAll(temps);
//                        messagesAdapter.addToEnd(addedMessage, true);



//                    notifyDataSet();

                            ComponentName appWidget = new ComponentName(context, WidgetProvider.class);
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                            appWidgetManager.updateAppWidget(appWidget, views);

                } else {
                    // error response, no access to resource?
//                        Log.e(TAG, String.valueOf(response.body().toString()));
                    Log.e(TAG, String.valueOf(response.raw().code()));

//                    convertStringToJson(context, views, FileUtil.readTextFile(context, R.raw.dummy), currlat, currlng);

                }

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }

//    private static  void convertStringToJson(final Context context, final RemoteViews views, String dataStr, final String currlat, final String currlng){
//        JsonParser parser = new JsonParser();
//        JsonElement element = parser.parse(dataStr);
//
//
//        ArrayList<MaskInfo> temps = new ArrayList<>();
//        if(element.getAsJsonObject().has("stores")) {
//
//            //  Log.d(TAG,"element count : " + element.getAsJsonObject().get("stores").getAsJsonArray().size());
//
//            if (element.getAsJsonObject().get("stores") instanceof JsonArray) {
//                if (element.getAsJsonObject().get("stores").getAsJsonArray().size() > 0) {
//
//                    for (JsonElement item : element.getAsJsonObject().get("stores").getAsJsonArray()) {
//                        String addr = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "addr");
//                        String code = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "code");
//                        String created_at = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "created_at");
//                        String lat = JsonUtil.hasJsonAndGetNumberString(item.getAsJsonObject(), "lat");
//                        String lng = JsonUtil.hasJsonAndGetNumberString(item.getAsJsonObject(), "lng");
//                        String name = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "name");
//                        String type = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "type");
//                        String remain_stat = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "remain_stat");
//                        String stock_at = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "stock_at");
//
//
//
//
//
//
//                        MaskInfo maskInfo = new MaskInfo();
//                        maskInfo.setName(name);
//                        maskInfo.setAddr(addr);
//                        maskInfo.setCode(code);
//                        maskInfo.setCreated_at(created_at);
//
//                        maskInfo.setType(type);
//                        maskInfo.setRemain_stat(remain_stat);
//                        maskInfo.setStock_at(stock_at);
//
//
//                        maskInfo.setLat(Float.parseFloat(lat));
//                        maskInfo.setLng(Float.parseFloat(lng));
//
////                                    if (!existMaskInfo(maskInfo)) {
////
////                                    }
//                        temps.add(maskInfo);
//                    }
//
//                }
//            }
//        }
//        double currentLat = Double.valueOf(currlat);
//        double currentLng = Double.valueOf(currlng);
//        for( MaskInfo maskInfo : temps){
//            double comparedLat =  maskInfo.getLat();
//            double comparedLng =  maskInfo.getLng();
//
//            double distance = GPSUtil.distance(currentLat, currentLng, comparedLat, comparedLng);
//
//            maskInfo.setDistance(distance);
//        }
//
//
//
//        Collections.sort(temps, new Comparator<MaskInfo>() {
//            @Override
//            public int compare(MaskInfo o1, MaskInfo o2) {
//
//
//                return Double.compare(o1.getDistance(), o2.getDistance());
//            }
//
//
//        });
//
//
////        showList(temps);
//
//
//
//        if( temps.size() > 2 ) {
//
//            MaskInfo first = temps.get(0);
//            MaskInfo second = temps.get(1);
//            MaskInfo third = temps.get(2);
//
//            views.setViewVisibility(R.id.addr_view, View.GONE);
//
//
//
//            views.setImageViewResource(R.id.type_icon, DataUtil.findTypeRes(first.getType()));
//            views.setTextViewText(R.id.name_txt, first.getName());
//            views.setTextViewText(R.id.name_remain_cnt, DataUtil.convertReadableRemainStat(context, first.getRemain_stat()));
//            views.setTextColor(R.id.name_remain_cnt, DataUtil.convertColorRemainStat(context, first.getRemain_stat()));
//
//            views.setImageViewResource(R.id.type_icon2, DataUtil.findTypeRes(second.getType()));
//            views.setTextViewText(R.id.name2_txt, second.getName());
//            views.setTextViewText(R.id.name2_remain_cnt, DataUtil.convertReadableRemainStat(context, second.getRemain_stat()));
//            views.setTextColor(R.id.name2_remain_cnt, DataUtil.convertColorRemainStat(context, second.getRemain_stat()));
//
//
//            views.setImageViewResource(R.id.type_icon3, DataUtil.findTypeRes(third.getType()));
//            views.setTextViewText(R.id.name3_txt, third.getName());
//            views.setTextViewText(R.id.name3_remain_cnt, DataUtil.convertReadableRemainStat(context, third.getRemain_stat()));
//            views.setTextColor(R.id.name3_remain_cnt, DataUtil.convertColorRemainStat(context, third.getRemain_stat()));
//
//        }else if( temps.size() == 2 ) {
//
//            MaskInfo first = temps.get(0);
//            MaskInfo second = temps.get(1);
//
//            views.setViewVisibility(R.id.addr_view, View.VISIBLE);
//
//            views.setImageViewResource(R.id.type_icon, DataUtil.findTypeRes(first.getType()));
//            views.setTextViewText(R.id.name_txt, first.getName());
//            views.setTextViewText(R.id.name_remain_cnt, DataUtil.convertReadableRemainStat(context, first.getRemain_stat()));
//            views.setTextColor(R.id.name_remain_cnt, DataUtil.convertColorRemainStat(context, first.getRemain_stat()));
//
//            views.setImageViewResource(R.id.type_icon2, DataUtil.findTypeRes(second.getType()));
//            views.setTextViewText(R.id.name2_txt, second.getName());
//            views.setTextViewText(R.id.name2_remain_cnt, DataUtil.convertReadableRemainStat(context, second.getRemain_stat()));
//            views.setTextColor(R.id.name2_remain_cnt, DataUtil.convertColorRemainStat(context, second.getRemain_stat()));
//
//            views.setImageViewResource(R.id.type_icon3, 0);
//            views.setTextViewText(R.id.name3_txt, "");
//            views.setTextViewText(R.id.name3_remain_cnt, "");
//        }else if ( temps.size() == 1 ) {
//            MaskInfo first = temps.get(0);
//            views.setViewVisibility(R.id.addr_view, View.VISIBLE);
//
//            views.setImageViewResource(R.id.type_icon, DataUtil.findTypeRes(first.getType()));
//            views.setTextViewText(R.id.name_txt, first.getName());
//            views.setTextViewText(R.id.name_remain_cnt, DataUtil.convertReadableRemainStat(context, first.getRemain_stat()));
//
//            views.setTextColor(R.id.name_remain_cnt, DataUtil.convertColorRemainStat(context, first.getRemain_stat()));
//
//            views.setImageViewResource(R.id.type_icon2, 0);
//            views.setTextViewText(R.id.name2_txt, "");
//            views.setTextViewText(R.id.name2_remain_cnt, "");
//
//            views.setImageViewResource(R.id.type_icon3, 0);
//            views.setTextViewText(R.id.name3_txt, "");
//            views.setTextViewText(R.id.name3_remain_cnt, "");
//        }else {
//            views.setViewVisibility(R.id.addr_view, View.VISIBLE);
//            views.setImageViewResource(R.id.type_icon, 0);
//            views.setTextViewText(R.id.name_txt, "");
//            views.setTextViewText(R.id.name_remain_cnt, "");
//
//            views.setImageViewResource(R.id.type_icon2, 0);
//            views.setTextViewText(R.id.name2_txt, "");
//            views.setTextViewText(R.id.name2_remain_cnt, "");
//
//            views.setImageViewResource(R.id.type_icon3, 0);
//            views.setTextViewText(R.id.name3_txt, "");
//            views.setTextViewText(R.id.name3_remain_cnt, "");
//
//        }
//
//
//
//        intentAction(context, views, R.id.root_view, ACTION_APP_LAUNCH);
//
//        ComponentName appWidget = new ComponentName(context, WidgetProvider.class);
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//        appWidgetManager.updateAppWidget(appWidget, views);
//
//
////        maskInfos.clear();
////        maskInfos.addAll(temps);
////
////        notifyDataSet();
//    }


    private static  void showList(ArrayList<MaskInfo> temps){
        for(MaskInfo maskInfo: temps){
            Log.d(TAG,maskInfo.getName() + "  d : " + maskInfo.getDistance());
        }
    }

}
