package co.giftree.maskeyes.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.naver.maps.map.overlay.OverlayImage;

import co.giftree.maskeyes.R;

public class DataUtil {

    public static String convertReadableRemainStat(Context context, final String stat){
        if (stat.equals("plenty")){
            return "100+";
        }else if (stat.equals("some")){
            return "30+";
        }else if (stat.equals("few")){
            return "1~30";
        }else if (stat.equals("empty")){
            return context.getString(R.string.empty);
        }

        return context.getString(R.string.unknown);

    }

    public static int convertColorRemainStat(Context context, final String stat) {
        if (stat.equals("plenty")){
            return ContextCompat.getColor(context, R.color.stat_plenty);
        }else if (stat.equals("some")){
            return ContextCompat.getColor(context, R.color.stat_some);
        }else if (stat.equals("few")){
            return ContextCompat.getColor(context, R.color.stat_few);
        }else if (stat.equals("empty")){
            return ContextCompat.getColor(context, R.color.stat_empty);
        }
        return ContextCompat.getColor(context, R.color.stat_unknown);
    }

    public static boolean isSoldOut(final String stat){
        return stat.equals("empty");
    }


    public static int findTypeRes(final String type){
        int t = Integer.valueOf(type);

        if (t == 1) {
            return R.drawable.ic_type01;
        }else if (t == 2) {
             return R.drawable.ic_type02;
        }else if (t == 3) {
            return R.drawable.ic_type03;
        }

        return R.drawable.ic_type01;
    }
    public static Drawable findTypeDrawable(Context context, final String type){
        int t = Integer.valueOf(type);

        if (t == 1) {
            return ContextCompat.getDrawable(context, R.drawable.ic_type01);
        }else if (t == 2) {
            return ContextCompat.getDrawable(context, R.drawable.ic_type02);
        }else if (t == 3) {
            return ContextCompat.getDrawable(context, R.drawable.ic_type03);
        }

        return ContextCompat.getDrawable(context, R.drawable.ic_type01);
    }

    public static OverlayImage findOverlayImg(final String type, final String stat){
        int t = Integer.valueOf(type);

        if (t == 1) {

//            if(DataUtil.isSoldOut(stat)){
//                return OverlayImage.fromResource(R.drawable.ic_mask_sold_out_01);
//            }else{
//                return OverlayImage.fromResource(R.drawable.ic_not_mask_sold_out_01);
//            }

            if (stat.equals("plenty")){
                return OverlayImage.fromResource(R.drawable.ic_mask_type01_plenty);
            }else if (stat.equals("some")){
                return OverlayImage.fromResource(R.drawable.ic_marker_type01_some);
            }else if (stat.equals("few")){
                return OverlayImage.fromResource(R.drawable.ic_marker_type01_few);
            }else if (stat.equals("empty")){
                return OverlayImage.fromResource(R.drawable.ic_marker_type01_empty);
            }else{
                return OverlayImage.fromResource(R.drawable.ic_marker_type01_unknown);
            }

        }else if (t == 2) {

//            if(DataUtil.isSoldOut(stat)){
//                return OverlayImage.fromResource(R.drawable.ic_mask_sold_out_02);
//            }else{
//                return OverlayImage.fromResource(R.drawable.ic_not_mask_sold_out_02);
//            }

            if (stat.equals("plenty")){
                return OverlayImage.fromResource(R.drawable.ic_mask_type02_plenty);
            }else if (stat.equals("some")){
                return OverlayImage.fromResource(R.drawable.ic_marker_type02_some);
            }else if (stat.equals("few")){
                return OverlayImage.fromResource(R.drawable.ic_marker_type02_few);
            }else if (stat.equals("empty")){
                return OverlayImage.fromResource(R.drawable.ic_marker_type02_empty);
            }else{
                return OverlayImage.fromResource(R.drawable.ic_marker_type02_unknown);
            }


        }else if (t == 3){

//            if(DataUtil.isSoldOut(stat)){
//                return OverlayImage.fromResource(R.drawable.ic_mask_sold_out_03);
//            }else{
//                return OverlayImage.fromResource(R.drawable.ic_not_mask_sold_out_03);
//            }

            if (stat.equals("plenty")){
                return OverlayImage.fromResource(R.drawable.ic_mask_type03_plenty);
            }else if (stat.equals("some")){
                return OverlayImage.fromResource(R.drawable.ic_marker_type03_some);
            }else if (stat.equals("few")){
                return OverlayImage.fromResource(R.drawable.ic_marker_type03_few);
            }else if (stat.equals("empty")){
                return OverlayImage.fromResource(R.drawable.ic_marker_type03_empty);
            }else{
                return OverlayImage.fromResource(R.drawable.ic_marker_type03_unknown);
            }
        }

        return OverlayImage.fromResource(R.drawable.ic_mask_sold_out_v2);
    }





}
