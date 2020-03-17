package co.giftree.maskeyes.util;

import android.content.Context;

import androidx.preference.PreferenceManager;

import co.giftree.maskeyes.R;

public class PreferenceUtil {

    public static boolean enable_smart_widget(Context context){
        return  PreferenceManager.getDefaultSharedPreferences(context).getBoolean("smart_widget_enable", true);

    }


    public static int range_meter(Context context){
        return  PreferenceManager.getDefaultSharedPreferences(context).getInt("range_meter", 3000);
    }

    public static String rangeMeterStr(Context context){
        int meter = PreferenceManager.getDefaultSharedPreferences(context).getInt("range_meter", 3000);

        switch (meter){
            case 500:
                return "500m";
            case 1000:
                return "1km";
            case 2000:
                return "2km";
            case 3000:
                return "3km";
            case 5000:
                return "5km";

        }

        return "3km";
    }


    public static int smart_widget_option(Context context){
        return  PreferenceManager.getDefaultSharedPreferences(context).getInt("smart_widget_option", 0);
    }

    public static String smartWidgetStr(Context context){
        int option = PreferenceManager.getDefaultSharedPreferences(context).getInt("smart_widget_option", 0);

        switch (option){
            case 0:
                return context.getString(R.string.priority_case1);
            case 1:
                return context.getString(R.string.priority_case2);
            case 2:
                return context.getString(R.string.priority_case3);


        }

        return context.getString(R.string.priority_case1);
    }

    public static void saveRangeMeter(Context context, int range_meter){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("range_meter", range_meter).apply();
    }

    public static void saveSmartWidgetOption(Context context, int option){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("smart_widget_option", option).apply();
    }

}
