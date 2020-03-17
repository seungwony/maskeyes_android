package co.giftree.maskeyes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Date;
import java.util.Locale;

import co.giftree.maskeyes.util.DateUtil;

public class NoticeActivity extends AppCompatActivity {

    private final static String TAG = "NoticeActivity";
    private final String BASIC_URL = "http://giftree.co/maskeyes/maskeyes_notice.html";
    private final String ENGLISH_URL = "http://giftree.co/maskeyes/maskeyes_notice_eng.html";
    private ProgressBar progress;

    private Button confirm_btn;
    private String url;
    public static void open(Context context) {
        Intent intent = new Intent(context, NoticeActivity.class);

//        intent.putExtra("url", url);

        Log.d(TAG, "NoticeActivity" );
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

       confirm_btn  = findViewById(R.id.confirm_btn);

        progress = (ProgressBar)findViewById(R.id.progress);

//        url = getIntent().getStringExtra("url");


        url = Locale.getDefault().getLanguage().equals("ko") ? BASIC_URL : ENGLISH_URL;
        String subject = getIntent().getStringExtra("subject");

        final WebView webView = (WebView)findViewById(R.id.webView);

        webView.setBackgroundColor(Color.TRANSPARENT);

        webView.getSettings().setDefaultTextEncodingName("utf-8");

        webView.setWebViewClient(new MyWebViewClient());

        webView.getSettings().setJavaScriptEnabled(true);



        final boolean old_user = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("old_user", false);


//        Log.d(TAG, "old_user : " + old_user);




        webView.loadUrl(url);

        TextView title = (TextView)findViewById(R.id.actionbar_title);

        if(subject != null){
            title.setText(subject);
        }


        progress.setVisibility(View.VISIBLE);
        confirm_btn.setText(getString(R.string.loading));


        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(old_user){
                    closeActivity();


                }else {
                    showMarkerHelper();
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("old_user", true).apply();
                }


            }
        });
    }

    private void closeActivity(){
        Log.d(TAG, "closeActivity");
        Date date = new Date();

        String savedDate = DateUtil.convertedSimpleOnlyMonthFormat(date);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("savedDate", savedDate).apply();

        finish();
    }
    private class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progress.setVisibility(View.GONE);
            confirm_btn.setText(getString(R.string.confirm));
        }
    }

    private void showMarkerHelper(){

//        Log.d(TAG, "showMarkerHelper");

        boolean wrapInScrollView = false;
        final MaterialDialog alertDialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.marker_helper))
                .customView(R.layout.dialog_help, wrapInScrollView)
                .positiveText("OK")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        closeActivity();
                    }
                })
                .canceledOnTouchOutside(false)
                .show();

        ImageView img = alertDialog.getCustomView().findViewById(R.id.marker_guide_img);

        int res = Locale.getDefault().getLanguage().equals("ko") ? R.drawable.marker_guide : R.drawable.marker_guide_eng;
                img.setImageResource(res);

    }

}
