package co.giftree.maskeyes.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class GeoPointer extends AsyncTask<String, Void, GeoPointer.Point[]> {

    private OnGeoPointListener onGeoPointListener;

    private Context context;

    public GeoPointer(Context context, OnGeoPointListener listener) {
        this.context = context;
        onGeoPointListener = listener;
    }

    public interface OnGeoPointListener {
        void onPoint(Point[] p);

        void onProgress(int progress, int max);
    }

    public class Point {
        // 위도
        public double x;
        // 경도
        public double y;
        public String addr;
        // 포인트를 받았는지 여부
        public boolean havePoint;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("x : ");
            builder.append(x);
            builder.append(" y : ");
            builder.append(y);
            builder.append(" addr : ");
            builder.append(addr);

            return builder.toString();
        }
    }

    @Override
    protected Point[] doInBackground(String... params) {
        // 리턴할 포인터 객체를 파람의 수만큼 배열로 만든다.
        Point[] points = new Point[params.length];
        for (int i = 0; i < params.length; i++) {
            // 프로그래스를 돌린다.
            onGeoPointListener.onProgress(i + 1, params.length);

            final String addr = params[i];
            // 구글의 GeoCode로 부터 주소를 기준으로 데이터를 가져온다.
            Point point = getPointFromGeoCoder(addr);

            // 구글의 지오코더로부터 주소를 갖고오지 못했으면 네이버 api를 이용해서 가져온다.
//            if (!point.havePoint) point = getPointFromNaver(addr);

            points[i] = point;
        }

        return points;
    }


    @Override
    protected void onPostExecute(Point[] point) {
        onGeoPointListener.onPoint(point);
    }

    /**
     * 지오코더(구글꺼)에서 좌표를 가져온다.
     */
    private Point getPointFromGeoCoder(String addr) {
        Point point = new Point();
        point.addr = addr;

        Geocoder geocoder = new Geocoder(context);
        List<Address> listAddress;
        try {
            listAddress = geocoder.getFromLocationName(addr, 1);
        } catch (IOException e) {
            e.printStackTrace();
            point.havePoint = false;
            return point;
        }

        if (listAddress.isEmpty()) {
            point.havePoint = false;
            return point;
        }

        point.havePoint = true;
        point.x = listAddress.get(0).getLongitude();
        point.y = listAddress.get(0).getLatitude();
        return point;
    }
}
