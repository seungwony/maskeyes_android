package co.giftree.maskeyes;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

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
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.MarkerIcons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import co.giftree.maskeyes.NaverMap.Util.ErrorCode;
import co.giftree.maskeyes.adapter.NamesAdapter;
import co.giftree.maskeyes.api.ApiManager;
import co.giftree.maskeyes.model.MaskInfo;
import co.giftree.maskeyes.model.Names;
import co.giftree.maskeyes.service.GpsTracker;
import co.giftree.maskeyes.util.DataUtil;
import co.giftree.maskeyes.util.JsonUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener  {
    private final static String TAG = "MainActivity";

//    EditText etLat; //위도 에딧
//    EditText etLng; //경도 에딧

    NaverMap mNaverMap; //네이버 맵
    Marker mOneMarker; //스마트폰 자체의 위치, 검색된 위치 등을 표시할 때 사용
    LinkedList<Marker> mDrawMarkerList; //여러개의 위치를 동시에 표시할 때 사용
    LinkedList<Marker> mClearMarkerList; //여러개의 위치를 동시에 지워야 할 때 사용


    ArrayList<MaskInfo> maskInfos;

    private LocationManager locationManager;
    private Location onlyOneLocation;
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    private ArrayAdapter<String> adapter;

    List<Names> namesList;
    NamesAdapter namesAdapter;
    private AutoCompleteTextView search_address_edittxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ApiManager.getInstance().setRestService();

        maskInfos = new ArrayList<>();

        onErrorResult();
        setFragment();



        openNoitce();


        mDrawMarkerList = new LinkedList<>();
        mClearMarkerList = new LinkedList<>();
//        findViewById(R.id.btn_Search).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(etLat.getText().toString().length()!=0 && etLng.getText().toString().length()!=0){
//                    try{
//                        Double lat = Double.parseDouble(etLat.getText().toString());
//                        Double lng = Double.parseDouble(etLng.getText().toString());
//                        drawMarker(lat,lng);
//                    }catch (Exception e){
//                        Toast.makeText(MainActivity.this, "위도,경도가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });





        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }




    }


    private void openNoitce(){
        NoticeActivity.open(this);
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
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
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
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

        mNaverMap.setMinZoom(14);
        mNaverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int i, boolean b) {

            }
        });

        mNaverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {

            }


        });

        Log.d(TAG, "zoom min : "+ mNaverMap.getMinZoom() + " max : "+ mNaverMap.getMaxZoom());


        gpsTracker = new GpsTracker(MainActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);
//        textview_address.setText(address);

        LatLng latlng = new LatLng(latitude, longitude);
        CameraPosition position = new CameraPosition(latlng, 15);
       // mNaverMap.setCameraPosition(position);

        //Toast.makeText(MainActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude + " 주소 : " + address, Toast.LENGTH_LONG).show();

        getTest();
    }

    /** 맵을 호출하려는데 발생하는 에러에 대한 처리 내용을 이곳에 작성 */
    private void onErrorResult(){
        NaverMapSdk.getInstance(this).setOnAuthFailedListener(new NaverMapSdk.OnAuthFailedListener(){
            @Override
            public void onAuthFailed(@NonNull NaverMapSdk.AuthFailedException e) {
                /* 처리 내용은 이곳에 작성 */
                Context mContext = MainActivity.this;
                Toast.makeText(mContext,
                        ""+ ErrorCode.getErrorType(mContext, e.getErrorCode()),
                        Toast.LENGTH_LONG).show();
            }
        } );
    }


    private void getTest(){
        getMaskInfo(String.valueOf(37.566676), String.valueOf(126.978414), String.valueOf(10000));
    }


    private void getMaskInfo(String lat, String lng, String meter ){

        final Call<JsonElement> call = ApiManager.getInstance().getApiCommonService().storesByGeo(lat, lng, meter);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.isSuccessful()) {
                    // tasks available
                    JsonElement element = response.body();

                    Log.d(TAG, "storesByGeo : " + element.toString());

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

                    maskInfos.clear();


                    maskInfos.addAll(temps);
//                        messagesAdapter.addToEnd(addedMessage, true);



                    notifyDataSet();

                } else {
                    // error response, no access to resource?
//                        Log.e(TAG, String.valueOf(response.body().toString()));
                    Log.e(TAG, String.valueOf(response.raw().code()));

                    convertStringToJson(readTextFile(getApplicationContext(), R.raw.dummy));

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

          //  Log.d(TAG,"element count : " + element.getAsJsonObject().get("stores").getAsJsonArray().size());

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

            marker.setCaptionColor(color);

            String des = DataUtil.convertReadableRemainStat(this, info.getRemain_stat());


            OverlayImage overlayImage = DataUtil.findOverlayImg(info.getType(), info.getRemain_stat());

            marker.setSubCaptionText(des);
            marker.setSubCaptionColor(Color.BLUE);
            marker.setIcon(overlayImage);



            marker.setOnClickListener(new Overlay.OnClickListener() {
                @Override
                public boolean onClick(@NonNull Overlay overlay) {
//                    Toast.makeText(getApplicationContext(), info.getName(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra("maskInfo", info);
                    startActivity(intent);
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

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }
}
