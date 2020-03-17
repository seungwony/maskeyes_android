package co.giftree.maskeyes;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.giftree.maskeyes.adapter.AddressAdapter;
import co.giftree.maskeyes.adapter.MaskInfoAdapter;
import co.giftree.maskeyes.api.ApiManager;
import co.giftree.maskeyes.api.KaKaoApiManager;
import co.giftree.maskeyes.model.AddressInfo;
import co.giftree.maskeyes.model.MaskInfo;
import co.giftree.maskeyes.util.GeoPointer;
import co.giftree.maskeyes.util.JsonUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchAddressActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();


    RecyclerView mRecyclerView;
    AddressAdapter mAdapter;
    private LinearLayoutManager layoutManager;

    private ArrayList<AddressInfo> items;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);


        items = new ArrayList<>();
        ImageButton back_btn = findViewById(R.id.back_btn);
        ImageView search_address_btn = findViewById(R.id.search_address_btn);
        final AutoCompleteTextView search_address_edittxt = findViewById(R.id.search_address_edittxt);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //Your RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        //  mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setLayoutManager(layoutManager);

        //Your RecyclerView.Adapter
        mAdapter = new AddressAdapter(this, items);
        mAdapter.setClickListener(new AddressAdapter.OnClickListener() {
            @Override
            public void onMoveToPosition(AddressInfo addressInfo) {
                Intent intent = new Intent();
                intent.putExtra("lat", addressInfo.getX());
                intent.putExtra("lng", addressInfo.getY());
                setResult(RESULT_OK, intent);

                finish();
            }
        });

        mRecyclerView.setAdapter(mAdapter);



        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        search_address_edittxt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if enter is pressed start calculating


                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){






                    int editTextLineCount = ((EditText)v).getLineCount();
                    if (editTextLineCount >= 1)
                        return true;
                }

                return false;
            }
        });


        search_address_edittxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(final Editable s) {

                searchAddress(s.toString());
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
////                        searchPointFromGeoCoder(s.toString());
//
//                    }
//                });

            }
        });
//        search_address_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Toast.makeText(getApplicationContext(), search_address_edittxt.getText().toString(), Toast.LENGTH_LONG).show();
//            }
//        });
//        testDummy();
    }
    private void getPoint(String... addr) {
        GeoPointer geoPointer = new GeoPointer(getApplicationContext(), listener);
        geoPointer.execute(addr);
    }


    private void searchAddress(final String address){

        final Call<JsonElement> call = KaKaoApiManager.getInstance().getAddressService().searchAddress(address);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.isSuccessful()) {
                    // tasks available
                    JsonElement element = response.body();

//                    Log.d(TAG, "searchAddress : " + element.toString());


                    ArrayList<AddressInfo> temps = new ArrayList<>();
                    if(element.getAsJsonObject().has("documents")) {


                        if (element.getAsJsonObject().get("documents") instanceof JsonArray) {
                            if (element.getAsJsonObject().get("documents").getAsJsonArray().size() > 0) {
                                for (JsonElement item : element.getAsJsonObject().get("documents").getAsJsonArray()) {
                                    String address_name = JsonUtil.hasJsonAndGetString(item.getAsJsonObject(), "address_name");
                                    String x = JsonUtil.hasJsonAndGetNumberString(item.getAsJsonObject(), "x");

                                    String y = JsonUtil.hasJsonAndGetNumberString(item.getAsJsonObject(), "y");

                                    AddressInfo addressInfo = new AddressInfo();
                                    addressInfo.setAddress(address_name);

                                    addressInfo.setX(Double.parseDouble(y));
                                    addressInfo.setY(Double.parseDouble(x));



//                                    if (!existMaskInfo(maskInfo)) {
//
//                                    }
                                    temps.add(addressInfo);
                                }

                            }
                        }
                    }

                    items.clear();


                    items.addAll(temps);
//                        messagesAdapter.addToEnd(addedMessage, true);



                    mAdapter.notifyDataSetChanged();

                } else {
                    // error response, no access to resource?
//                        Log.e(TAG, String.valueOf(response.body().toString()));
                    Log.e(TAG, String.valueOf(response.raw().code()));



                }

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }

    private GeoPointer.OnGeoPointListener listener = new GeoPointer.OnGeoPointListener() {
        @Override
        public void onPoint(GeoPointer.Point[] p) {
            int sCnt = 0, fCnt = 0;
            ArrayList<AddressInfo> temps = new ArrayList<>();
            for (GeoPointer.Point point : p) {
                if (point.havePoint){
                    //
                    AddressInfo info = new AddressInfo();
                    info.setAddress(point.addr);
                    info.setX(point.x);
                    info.setY(point.y);
                    temps.add(info);
                }

//                Log.d("TEST_CODE", point.toString());



            }
            items.clear();
            items.addAll(temps);
            mAdapter.notifyDataSetChanged();
//            Log.d("TEST_CODE", String.format("성공 : %s, 실패 : %s", sCnt, fCnt));
        }

        @Override
        public void onProgress(int progress, int max) {
//            Log.d("TEST_CODE", String.format("좌표를 얻어오는중 %s / %s", progress, max));
        }
    };

    private void testDummy(){
        AddressInfo info = new AddressInfo();
        info.setAddress("경북 영천시 완산동");
        info.setX(33);
        info.setY(123);

        AddressInfo info1 = new AddressInfo();
        info1.setAddress("경북 영천시 완산동 123");
        info1.setX(33);
        info1.setY(123);

        items.add(info);
        items.add(info1);

        mAdapter.notifyDataSetChanged();
    }
    private void searchPointFromGeoCoder(String addr) {

//        point.addr = addr;

        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> listAddress = null;
        try {
            listAddress = geocoder.getFromLocationName(addr, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(listAddress!=null){
            if (listAddress.isEmpty()) {
                return;
            }



            ArrayList<AddressInfo> temps = new ArrayList<>();
            for(Address address : listAddress){
                AddressInfo info = new AddressInfo();
                info.setAddress(address.getAddressLine(0));
                info.setX(address.getLatitude());
                info.setY(address.getLongitude());
                temps.add(info);
            }

            items.clear();
            items.addAll(temps);
            mAdapter.notifyDataSetChanged();
        }


//        point.havePoint = true;
//        point.x = listAddress.get(0).getLongitude();
//        point.y = listAddress.get(0).getLatitude();
    }
}
