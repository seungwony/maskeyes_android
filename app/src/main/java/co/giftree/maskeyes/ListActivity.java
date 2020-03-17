package co.giftree.maskeyes;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import co.giftree.maskeyes.adapter.MaskInfoAdapter;
import co.giftree.maskeyes.model.MaskInfo;
import co.giftree.maskeyes.util.GPSUtil;

public class ListActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    RecyclerView mRecyclerView;
    MaskInfoAdapter mAdapter;
    private LinearLayoutManager layoutManager;

    private ArrayList<MaskInfo> items;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed();
            }
        });




        items = getIntent().getParcelableArrayListExtra("list");



        double currentLat = getIntent().getDoubleExtra("lat", 0.0);
        double currentLng = getIntent().getDoubleExtra("lng", 0.0);
        for( MaskInfo maskInfo : items){
            double comparedLat =  maskInfo.getLat();
            double comparedLng =  maskInfo.getLng();

            double distance = GPSUtil.distance(currentLat, currentLng, comparedLat, comparedLng);

            maskInfo.setDistance(distance);
        }


        if(currentLat!=0 && currentLng!=0){
            Collections.sort(items, new Comparator<MaskInfo>() {
                @Override
                public int compare(MaskInfo o1, MaskInfo o2) {


                    return Double.compare(o1.getDistance(), o2.getDistance());
                }


            });
        }



        layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //Your RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
      //  mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setLayoutManager(layoutManager);

        //Your RecyclerView.Adapter
        mAdapter = new MaskInfoAdapter(this, items);

        mAdapter.setCurrPos(currentLat, currentLng);
        mAdapter.setClickListener(new MaskInfoAdapter.MapClickListener() {
            @Override
            public void onMoveToPosition(MaskInfo maskInfo) {
                Intent intent = new Intent();
                intent.putExtra("info", maskInfo);
                setResult(RESULT_OK, intent);

                finish();
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


        hiddenEmptyView(items.size() > 0);





//        Log.d(TAG, "masks : "+ items.size());

    }

    private void hiddenEmptyView(boolean isHidden){
        View empty_view = findViewById(R.id.empty_view);
        ImageView warning_icon = findViewById(R.id.warning_icon);
        TextView warning_msg = findViewById(R.id.warning_msg);
        if(isHidden){
            empty_view.setVisibility(View.GONE);
        }else{

            empty_view.setVisibility(View.VISIBLE);


        }
    }
}
