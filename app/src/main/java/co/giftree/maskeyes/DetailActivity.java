package co.giftree.maskeyes;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import co.giftree.maskeyes.model.MaskInfo;

public class DetailActivity extends AppCompatActivity {

    private MaskInfo maskInfo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                onBackPressed();
            }
        });


        maskInfo = getIntent().getParcelableExtra("maskInfo");

        if(maskInfo==null){
            finish();
        }

        initUI();
    }


    private void initUI(){
        TextView name_txt = findViewById(R.id.name);
        TextView addr_txt = findViewById(R.id.addr);
        TextView stock_t_txt = findViewById(R.id.stock_t);
        TextView stock_cnt_txt = findViewById(R.id.stock_cnt);
        TextView sold_cnt_txt = findViewById(R.id.sold_cnt);
        TextView remain_cnt_txt = findViewById(R.id.remain_cnt);
        TextView sold_out_txt = findViewById(R.id.sold_out);
        TextView created_at_txt = findViewById(R.id.created_at);
        TextView actionbar_title = findViewById(R.id.actionbar_title);

        name_txt.setText(maskInfo.getName());
        actionbar_title.setText(maskInfo.getName());
        addr_txt.setText(maskInfo.getAddr());


        created_at_txt.setText(maskInfo.getCreated_at());

    }


}
