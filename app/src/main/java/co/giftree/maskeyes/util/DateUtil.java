package co.giftree.maskeyes.util;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import co.giftree.maskeyes.R;

public class DateUtil {
    public static String convertedSimpleFormat(Date date){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return df.format(date);
    }

    public static String convertedWidgetFormat(Date date){
        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss");

        return df.format(date);
    }

    public static String convertedSimpleOnlyMonthFormat(Date date){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");

        return df.format(date);
    }


    public static String todayWeekDay(Context context){
        String[] weekDay = { context.getString(R.string.sun), context.getString(R.string.mon), context.getString(R.string.tue), context.getString(R.string.wed), context.getString(R.string.thur), context.getString(R.string.fri), context.getString(R.string.sat) };

        Calendar cal = Calendar.getInstance();

        int num = cal.get(Calendar.DAY_OF_WEEK)-1;

        String today = weekDay[num];



        return today;
    }

    public static String todayTargetForMain(Context context){


        String[] target = { context.getString(R.string.non_purchaser), String.format(context.getString(R.string.regulation_people_born_format), "1", "6"), String.format(context.getString(R.string.regulation_people_born_format), "2", "7"), String.format(context.getString(R.string.regulation_people_born_format), "3", "8"), String.format(context.getString(R.string.regulation_people_born_format), "4", "9"), String.format(context.getString(R.string.regulation_people_born_format), "5", "0"), context.getString(R.string.non_purchaser) };



        Calendar cal = Calendar.getInstance();

        int num = cal.get(Calendar.DAY_OF_WEEK)-1;

        String today = target[num];



        return today;
    }

    public static String todayTarget(Context context){
        String[] target = { context.getString(R.string.non_purchaser), "1,6", "2,7", "3,8", "4,9", "5,0", context.getString(R.string.non_purchaser) };



        Calendar cal = Calendar.getInstance();

        int num = cal.get(Calendar.DAY_OF_WEEK)-1;

        String today = target[num];



        return today;
    }



}
