package co.giftree.maskeyes;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import co.giftree.maskeyes.NaverMap.Util.ErrorCode;
import co.giftree.maskeyes.api.ApiManager;
import co.giftree.maskeyes.model.MaskInfo;
import co.giftree.maskeyes.service.GpsTracker;
import co.giftree.maskeyes.util.DataUtil;
import co.giftree.maskeyes.util.DateUtil;
import co.giftree.maskeyes.util.DistanceCalculator;
import co.giftree.maskeyes.util.JsonUtil;
import co.giftree.maskeyes.util.PreferenceUtil;
import co.giftree.maskeyes.widget.WidgetProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainV2Activity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, LocationListener {
    private final static String TAG = "MainV2Activity";


    NaverMap mNaverMap; //네이버 맵
    Marker mOneMarker; //스마트폰 자체의 위치, 검색된 위치 등을 표시할 때 사용
    LinkedList<Marker> mDrawMarkerList; //여러개의 위치를 동시에 표시할 때 사용
    LinkedList<Marker> mClearMarkerList; //여러개의 위치를 동시에 지워야 할 때 사용


    ArrayList<MaskInfo> maskInfos;
    private GpsTracker gpsTracker;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final  int MAP_REQUEST = 1001;
    private static final  int ADDRESS_REQUEST = 1002;

    private FusedLocationSource locationSource;
    private AdView adView;
    private static boolean _doubleBackToExitPressedOnce = false;

    TextView range_text, smart_widget_priority_txt;

    private CheckBox hide_soldout_cb;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);




// Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        adView = findViewById(R.id.ad_view);
        Date date = new Date();
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();


        hide_soldout_cb = findViewById(R.id.hide_soldout_cb);

        WidgetProvider.updateAppWidget(getApplicationContext());

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
        String currentDate = "Updated : " + DateUtil.convertedSimpleFormat(date);

        String targetStr = DateUtil.todayWeekDay(this) + " ("+ DateUtil.todayTargetForMain(this) +getString(R.string.last_format_target)+")";

        TextView day_info = findViewById(R.id.day_info);
        TextView update_date_txt = findViewById(R.id.update_date_txt);
        smart_widget_priority_txt= findViewById(R.id.smart_widget_priority_txt);
        range_text = findViewById(R.id.range_text);
        TextView version_txt = findViewById(R.id.version_txt);
        Button mask_policy_btn = findViewById(R.id.mask_policy_btn);
        Button list_btn = findViewById(R.id.list_btn);
        Button fav_btn = findViewById(R.id.fav_btn);
        Button contact_btn = findViewById(R.id.contact_btn);
        Button help_btn = findViewById(R.id.help_btn);

        View range_btn= findViewById(R.id.range_btn);
        View smart_widget_priority_btn= findViewById(R.id.smart_widget_priority_btn);

        Button find_btn = findViewById(R.id.find_btn);
        Button search_address_btn = findViewById(R.id.search_address_btn);


        smart_widget_priority_txt.setText(PreferenceUtil.smartWidgetStr(getApplication()));
        range_text.setText(PreferenceUtil.rangeMeterStr(getApplication()));

        day_info.setText(targetStr);

        int font_size = Locale.getDefault().getLanguage().equals("ko") ? 16 : 12;
        day_info.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);

        update_date_txt.setText(currentDate);


        search_address_btn.setOnClickListener(this);

        mask_policy_btn.setOnClickListener(this);
        list_btn.setOnClickListener(this);
        fav_btn.setOnClickListener(this);
        contact_btn.setOnClickListener(this);
        find_btn.setOnClickListener(this);
        help_btn.setOnClickListener(this);
        range_btn.setOnClickListener(this);
        smart_widget_priority_btn.setOnClickListener(this);

        maskInfos = new ArrayList<>();

        onErrorResult();
        setFragment();






        mDrawMarkerList = new LinkedList<>();
        mClearMarkerList = new LinkedList<>();


        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            version_txt.setText("Ver : "+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }


        locationSource =
                new FusedLocationSource(this, PERMISSIONS_REQUEST_CODE);


        hide_soldout_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(mNaverMap!=null){
                    CameraPosition position = mNaverMap.getCameraPosition();


                    double latitude = position.target.latitude;
                    double longitude = position.target.longitude;

                    getMaskInfo(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(PreferenceUtil.range_meter(getApplicationContext())));
                }


            }
        });

        SwitchCompat smart_widget_enable_switch = findViewById(R.id.smart_widget_enable_switch);

        boolean enable_smart_widget =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("smart_widget_enable", true);
        smart_widget_enable_switch.setChecked(enable_smart_widget);
        smart_widget_enable_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("smart_widget_enable", isChecked).apply();
                Log.d(TAG, "enable smart widget : "  + isChecked);
                WidgetProvider.updateAppWidget(getApplicationContext());
            }
        });
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){

            case R.id.range_btn:
                alert_range();
                break;
            case R.id.smart_widget_priority_btn:
                alert_widget_priority();
                break;
            case R.id.search_address_btn:
                intent = new Intent(getApplicationContext(), SearchAddressActivity.class);


                startActivityForResult(intent, ADDRESS_REQUEST);

                break;
            case R.id.find_btn:



                if(mNaverMap!=null){
                    CameraPosition position = mNaverMap.getCameraPosition();
                    double latitude = position.target.latitude;
                    double longitude = position.target.longitude;

                    getMaskInfo(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(PreferenceUtil.range_meter(getApplication())));
                }


                break;

            case R.id.mask_policy_btn:
                NoticeActivity.open(this);
                break;

            case R.id.list_btn:




                intent = new Intent(getApplicationContext(), ListActivity.class);
                intent.putExtra("list", maskInfos);

                if(gpsTracker!=null){
                    intent.putExtra("lat", gpsTracker.getLatitude());
                    intent.putExtra("lng", gpsTracker.getLongitude());
                }


                startActivityForResult(intent, MAP_REQUEST);

                break;

            case R.id.fav_btn:
                break;
            case R.id.help_btn:
                showMarkerHelper();
                break;

            case R.id.contact_btn:
                send_email();
                break;
        }
    }


    private void openNoitce(){
        Date date = new Date();

        String today = DateUtil.convertedSimpleOnlyMonthFormat(date);

        String savedDate = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("savedDate", "");

//        Log.d(TAG, "today : " + today + " saveDate : " + savedDate );

        if(!savedDate.equals(today)){
            NoticeActivity.open(this);
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(gpsTracker!=null){
            if(gpsTracker.getLatitude()==0 && gpsTracker.getLongitude()==0 ){
                gpsTracker = new GpsTracker(MainV2Activity.this);

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();


                double dummyLat = 37.5660096;
                double dummyLng = 126.983635;



                if(latitude!=0 && longitude!=0){
                    LatLng latlng = new LatLng(latitude, longitude);
                    CameraPosition position = new CameraPosition(latlng, 16);
                    mNaverMap.setCameraPosition(position);

                    getMaskInfo(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(PreferenceUtil.range_meter(getApplication())));
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;


            case MAP_REQUEST:
                if(resultCode == RESULT_OK){
                    MaskInfo maskInfo = data.getParcelableExtra("info");

                    if (maskInfo.getLat()!=0.0 && maskInfo.getLng()!=0.0) {
                        LatLng latlng = new LatLng(maskInfo.getLat(), maskInfo.getLng());
                        CameraPosition position = new CameraPosition(latlng, 15);
                        mNaverMap.setCameraPosition(position);

                        getMaskInfo(String.valueOf(maskInfo.getLat()), String.valueOf(maskInfo.getLng()), String.valueOf(PreferenceUtil.range_meter(getApplication())));
                    }

                }
                break;

            case ADDRESS_REQUEST:
                if(resultCode == RESULT_OK){
                    double lat = data.getDoubleExtra("lat", 0.0);
                    double lng = data.getDoubleExtra("lng", 0.0);

                    if (lat!=0.0 && lng!=0.0){
                        LatLng latlng = new LatLng(lat, lng);
                        CameraPosition position = new CameraPosition(latlng, 15);
                        mNaverMap.setCameraPosition(position);
                        getMaskInfo(String.valueOf(lat), String.valueOf(lng), String.valueOf(PreferenceUtil.range_meter(getApplication())));
                    }

                }
                break;
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainV2Activity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainV2Activity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainV2Activity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음




        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainV2Activity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainV2Activity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainV2Activity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainV2Activity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
//            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
//            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
//            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    /** 1개의 좌표로 이동하여 마커 찍는 메소드 */
    private void drawMarker(double lat, double lng){
        LatLng latLng = new LatLng(lat,lng);
        mOneMarker.setMap(null);
        mOneMarker.setPosition(latLng);
        mOneMarker.setCaptionText("Title");
        mOneMarker.setCaptionColor(Color.RED);
        mOneMarker.setSubCaptionText("sub Title");
        mOneMarker.setSubCaptionColor(Color.BLUE);
        mOneMarker.setIcon(MarkerIcons.BLACK);
        mOneMarker.setIconTintColor(Color.GREEN);
        mOneMarker.setMap(mNaverMap);



        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(latLng,18).animate(CameraAnimation.Fly);
        mNaverMap.moveCamera(cameraUpdate);
    }
    /** 여러개 찍어야할 때 리스트에 담는 메소드 */
    private void addMarker(Marker marker){
        mDrawMarkerList.add(marker);
    }
    /** 여러개의 좌표로 마커를 찍는 메소드 */
    private void drawMarkerList(){
        clearMarkerList();
        for(Marker m : mDrawMarkerList){
            m.setMap(mNaverMap);
            mClearMarkerList.add(m);
        }
        mDrawMarkerList.clear();
    }
    /** 이전 마커들을 지우는 메소드 */
    private void clearMarkerList(){
        for(Marker m : mClearMarkerList){
            m.setMap(null);
        }
        mClearMarkerList.clear();
    }

    /** 네이버 맵 프레그먼트를 이곳에 작성 */
    private void setFragment(){
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    /** 맵을 성공적으로 처음 호출했을 때 설정할 내용을 이곳에 작성 */
    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.mNaverMap = naverMap;
//        mOneMarker = new Marker();
//        mOneMarker.setPosition(new LatLng(37.566676,126.978414));
//        mOneMarker.setCaptionText("Title");
//        mOneMarker.setCaptionColor(Color.RED);
//        mOneMarker.setSubCaptionText("sub Title");
//        mOneMarker.setSubCaptionColor(Color.BLUE);
//        mOneMarker.setIcon(MarkerIcons.BLACK);
//        mOneMarker.setIconTintColor(Color.GREEN);
//        mOneMarker.setMap(mNaverMap);
        naverMap.setLocationSource(locationSource);


        mNaverMap.setMinZoom(12);
//        mNaverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
//            @Override
//            public void onCameraChange(int i, boolean b) {
//
//               // Log.d(TAG, "onCameraChange i : " + i + " b : " + b);
//            }
//        });

//        mNaverMap.removeOnLocationChangeListener(onLocationChangeListener);
//        mNaverMap.addOnLocationChangeListener(onLocationChangeListener);
//        mNaverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
//            @Override
//            public void onLocationChange(@NonNull Location location) {
//
//                Log.d(TAG, "onLocationChange " + location.toString());
//                Toast.makeText(getApplicationContext(),
//                        location.getLatitude() + ", " + location.getLongitude(),
//                        Toast.LENGTH_SHORT).show();
//            }
//
//
//        });



//        naverMap.setLocationTrackingMode(LocationTrackingMode.None);





        UiSettings uiSettings = mNaverMap.getUiSettings();


        uiSettings.setLocationButtonEnabled(true);
        Log.d(TAG, "zoom min : "+ mNaverMap.getMinZoom() + " max : "+ mNaverMap.getMaxZoom());


        gpsTracker = new GpsTracker(MainV2Activity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();


        double dummyLat = 37.5660096;
        double dummyLng = 126.983635;

        String address = getCurrentAddress(latitude, longitude);
//        textview_address.setText(address);


//        getTest();

        if(latitude!=0 && longitude!=0){
            LatLng latlng = new LatLng(latitude, longitude);
            CameraPosition position = new CameraPosition(latlng, 16);
            mNaverMap.setCameraPosition(position);

            getMaskInfo(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(PreferenceUtil.range_meter(getApplication())));
        }

    }

    /** 맵을 호출하려는데 발생하는 에러에 대한 처리 내용을 이곳에 작성 */
    private void onErrorResult(){
        NaverMapSdk.getInstance(this).setOnAuthFailedListener(new NaverMapSdk.OnAuthFailedListener(){
            @Override
            public void onAuthFailed(@NonNull NaverMapSdk.AuthFailedException e) {
                /* 처리 내용은 이곳에 작성 */
                Context mContext = MainV2Activity.this;
                Toast.makeText(mContext,
                        ""+ ErrorCode.getErrorType(mContext, e.getErrorCode()),
                        Toast.LENGTH_LONG).show();
            }
        } );
    }


    NaverMap.OnLocationChangeListener onLocationChangeListener = new NaverMap.OnLocationChangeListener() {
        @Override
        public void onLocationChange(@NonNull Location location) {
            if(gpsTracker!=null){
                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                getMaskInfo(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(PreferenceUtil.range_meter(getApplicationContext())));
            }


        }
    };

    private void getTest(){
        getMaskInfo(String.valueOf(37.566676), String.valueOf(126.978414), String.valueOf(Constants.METER));
    }


    private void getMaskInfo(String lat, String lng, String meter ){


        double d_lat = Double.valueOf(lat);
        double d_lng = Double.valueOf(lng);

        if(d_lat==0 && d_lng ==0){
            return;
        }

        final Call<JsonElement> call = ApiManager.getInstance().getApiCommonService().storesByGeo(lat, lng, meter);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.isSuccessful()) {
                    // tasks available
                    JsonElement element = response.body();



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

                                    if(hide_soldout_cb.isChecked() && remain_stat.equals("empty")){

                                    }else{
                                        temps.add(maskInfo);
                                    }


                                }

                            }
                        }
                    }

                    maskInfos.clear();


                    maskInfos.addAll(temps);
//                        messagesAdapter.addToEnd(addedMessage, true);



                    notifyDataSet();

                } else {
                    // error response, no access to resource?
//                        Log.e(TAG, String.valueOf(response.body().toString()));
                    Log.e(TAG, String.valueOf(response.raw().code()));

//                    convertStringToJson(readTextFile(getApplicationContext(), R.raw.dummy));
                    if (response.raw().code() == 500){
                        Toast.makeText(getApplicationContext(), "알 수 없는 서버 오류입니다.", Toast.LENGTH_SHORT).show();
                    }else if (response.raw().code() == 429){
                        Toast.makeText(getApplicationContext(), "서버 과부하로 접속하지 못하고 있습니다.", Toast.LENGTH_SHORT).show();
                    }else if (response.raw().code() == 400){
                        Toast.makeText(getApplicationContext(), "알 수 없는 서버 오류입니다.", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }
    public static String readTextFile(Context context,@RawRes int id){
        InputStream inputStream = context.getResources().openRawResource(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buffer[] = new byte[1024];
        int size;
        try {
            while ((size = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }


    private void convertStringToJson(String dataStr){
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(dataStr);


        ArrayList<MaskInfo> temps = new ArrayList<>();
        if(element.getAsJsonObject().has("stores")) {

//              Log.d(TAG,"element count : " + element.getAsJsonObject().get("stores").getAsJsonArray().size());

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

                        temps.add(maskInfo);
                    }

                }
            }
        }

        maskInfos.clear();
        maskInfos.addAll(temps);

        notifyDataSet();
    }

    private boolean existMaskInfo(MaskInfo info){
        for (MaskInfo _info : maskInfos) {

            if(info.getCode().equals(_info.getCode())){
                return true;
            }
        }
        return false;
    }


    private void notifyDataSet(){
        for (final MaskInfo info : maskInfos) {

            Marker marker = new Marker();

            LatLng latLng = new LatLng(info.getLat(),info.getLng());

            marker.setPosition(latLng);
            marker.setCaptionText(info.getName());

            int color = DataUtil.isSoldOut(info.getRemain_stat()) ? Color.RED : Color.GREEN;

            marker.setCaptionColor(Color.BLACK);

            String des = DataUtil.convertReadableRemainStat(this, info.getRemain_stat());


//            if(info.getName().equals("지성한약국")){
//                Log.d(TAG, "지성한약국 type : " + info.getType() + " remain : "+info.getRemain_stat());
//            }

            OverlayImage overlayImage = DataUtil.findOverlayImg(info.getType(), info.getRemain_stat());

            marker.setSubCaptionText(des);
            marker.setSubCaptionColor(DataUtil.convertColorRemainStat(getApplicationContext(), info.getRemain_stat()));
            marker.setIcon(overlayImage);



            marker.setOnClickListener(new Overlay.OnClickListener() {
                @Override
                public boolean onClick(@NonNull Overlay overlay) {
//                    Toast.makeText(getApplicationContext(), info.getName(), Toast.LENGTH_SHORT).show();

//                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
//                    intent.putExtra("maskInfo", info);

//                    startActivity(intent);

                    showMarkerDialog(info);
                    return true;
                }
            });

            addMarker(marker);


        }
        drawMarkerList();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {
                locationSource.onRequestPermissionsResult(
                        permsRequestCode, permissions, grandResults);

                //위치 값을 가져올 수 있음
                openNoitce();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainV2Activity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(MainV2Activity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    private void send_email(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"master@giftree.co"});
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_title));
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, getString(R.string.contact_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "Not installed any email app.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (_doubleBackToExitPressedOnce) {
            super.onBackPressed();

            return;
        }

        this._doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.more_press_back), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                _doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private void alert_range(){

        final int CASE_500m = 0;
        final int CASE_1km  = 1;
        final int CASE_2km  = 2;
        final int CASE_3km  = 3;
        final int CASE_5km  = 4;
//        final int CASE_3km  = 5;



        new MaterialDialog.Builder(this)
                .title(R.string.range_of_radius)
                .titleColor(ContextCompat.getColor(getApplication(), R.color.colorAccent))
                .items(R.array.range_arrays)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        range_text.setText(text);
                        switch (which){

                            case CASE_500m:
                                PreferenceUtil.saveRangeMeter(getApplicationContext(), 500);
                                break;

                            case CASE_1km:
                                PreferenceUtil.saveRangeMeter(getApplicationContext(), 1000);
                                break;

                            case CASE_2km:
                                PreferenceUtil.saveRangeMeter(getApplicationContext(), 2000);
                                break;
                            case CASE_3km:
                                PreferenceUtil.saveRangeMeter(getApplicationContext(), 3000);
                                break;
                            case CASE_5km:
                                PreferenceUtil.saveRangeMeter(getApplicationContext(), 5000);
                                break;
                            default:
                                dialog.dismiss();
                                break;
                        }

                        if(mNaverMap!=null){
                            CameraPosition position = mNaverMap.getCameraPosition();
                            double latitude = position.target.latitude;
                            double longitude = position.target.longitude;

                            getMaskInfo(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(PreferenceUtil.range_meter(getApplication())));
                        }

                        WidgetProvider.updateAppWidget(getApplicationContext());
                    }
                })
                .show();
    }
    private void alert_widget_priority(){

        final int CASE1 = 0;
        final int CASE2  = 1;
        final int CASE3  = 2;



        new MaterialDialog.Builder(this)
                .title(R.string.Priority)
                .titleColor(ContextCompat.getColor(getApplication(), R.color.colorAccent))
                .items(R.array.smart_widget_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                        smart_widget_priority_txt.setText(text);

                        switch (which){

                            case CASE1:



                                PreferenceUtil.saveSmartWidgetOption(getApplicationContext(), CASE1);

                                break;

                            case CASE2:
                                PreferenceUtil.saveSmartWidgetOption(getApplicationContext(), CASE2);
                                break;

                            case CASE3:
                                PreferenceUtil.saveSmartWidgetOption(getApplicationContext(), CASE3);
                                break;

                            default:
                                dialog.dismiss();
                                break;
                        }

                        WidgetProvider.updateAppWidget(getApplicationContext());
                    }
                })
                .show();
    }


    private void showMarkerHelper(){

        boolean wrapInScrollView = false;
        final MaterialDialog alertDialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.marker_helper))
                .customView(R.layout.dialog_help, wrapInScrollView)
                .positiveText("OK")
                .canceledOnTouchOutside(false)
                .show();

        ImageView img = alertDialog.getCustomView().findViewById(R.id.marker_guide_img);

        int res = Locale.getDefault().getLanguage().equals("ko") ? R.drawable.marker_guide : R.drawable.marker_guide_eng;
        img.setImageResource(res);

    }

    private void showMarkerDialog(final MaskInfo maskInfo){




        boolean wrapInScrollView = false;
        final MaterialDialog alertDialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_marker, wrapInScrollView)
                .show();


        View v = alertDialog.getCustomView();


        ImageView typeImg = v.findViewById(R.id.type_icon_img);
        TextView name_txt= v.findViewById(R.id.name_txt);

        TextView distance_txt = v.findViewById(R.id.distance_txt);

        TextView remain_stat_txt = v.findViewById(R.id.remain_stat_txt);
        TextView stock_at_txt = v.findViewById(R.id.stock_at_txt);

        TextView remain_stat_title_txt = v.findViewById(R.id.remain_stat_title_txt);

        Button navermap_btn = v.findViewById(R.id.navermap_btn);
        Button kakaomap_btn = v.findViewById(R.id.kakaomap_btn);
        Button googlemap_btn = v.findViewById(R.id.googlemap_btn);


        ImageButton close_btn = v.findViewById(R.id.close_btn);
        View address_view = v.findViewById(R.id.address_view);

        TextView address_txt = v.findViewById(R.id.address_txt);

        typeImg.setImageDrawable(DataUtil.findTypeDrawable(getApplicationContext(), maskInfo.getType()));

        name_txt.setText(maskInfo.getName());
        remain_stat_txt.setText(DataUtil.convertReadableRemainStat(this, maskInfo.getRemain_stat()));

        remain_stat_txt.setTextColor(DataUtil.convertColorRemainStat(getApplicationContext(), maskInfo.getRemain_stat()));
        remain_stat_title_txt.setTextColor(DataUtil.convertColorRemainStat(getApplicationContext(), maskInfo.getRemain_stat()));

        String stock_at = !maskInfo.getStock_at().equals("") ? maskInfo.getStock_at() : getString(R.string.unknown);
        stock_at_txt.setText(stock_at);

        address_txt.setText(maskInfo.getAddr());


        try{

            if(gpsTracker!=null && gpsTracker.getLatitude() != 0  && gpsTracker.getLongitude() != 0 ){
                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                double cal_dis = DistanceCalculator.distance(latitude, longitude, maskInfo.getLat(), maskInfo.getLng(), "K");
//                        double result = Math.round(Math.abs(cal_dis)*100d);

                DecimalFormat format = new DecimalFormat("0.#");
                String dis_str = format.format(cal_dis) + " km";

                if(cal_dis>10000){
                    distance_txt.setVisibility(View.GONE);
                }else{
                    distance_txt.setVisibility(View.VISIBLE);

                    distance_txt.setText(String.format(getString(R.string.distance_format), dis_str));
                }
            }


        }catch (Exception ex){
            distance_txt.setVisibility(View.GONE);
        }

        address_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sdk_Version = android.os.Build.VERSION.SDK_INT;
                if(sdk_Version < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(maskInfo.getAddr());   // Assuming that you are copying the text from a TextView
                    Toast.makeText(getApplicationContext(), getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
                }
                else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText(getString(R.string.address), maskInfo.getAddr());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
                }
            }
        });

        navermap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        navermap_btn.setVisibility(View.GONE);
        kakaomap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = "https://map.kakao.com/link/map/"+maskInfo.getName()+","+maskInfo.getLat()+","+maskInfo.getLng();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        googlemap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //https://www.google.com/maps/@35.9716496,128.9242832,15z

                String url = "https://www.google.com/maps/place/"+maskInfo.getLat()+"+"+maskInfo.getLng()+"/@"+maskInfo.getLat()+","+maskInfo.getLng()+",z20";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }
}
