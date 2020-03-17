package co.giftree.maskeyes.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class ApiManager {

    //13.125.214.78
    public static final String BASIC_URL = "https://8oi9s0nnth.apigw.ntruss.com";

    private ApiCommonService apiCommonService;


//    private APIService tempService;

    private Retrofit retrofit;
    protected Gson gson;
    protected static OkHttpClient okClient;
    private volatile static ApiManager instance = null;

    public static ApiManager getInstance(){
        if(instance==null){
            synchronized (ApiManager.class){
                if(instance==null){
                    instance = new ApiManager();
                }
            }
        }
        return instance;
    }


    public ApiManager(){
//        httpClient = new OkHttpClient.Builder();
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        setRestService();
    }


    public void setRestService() {
        try{


            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.readTimeout(10, TimeUnit.SECONDS);
            builder.connectTimeout(5, TimeUnit.SECONDS);

//            if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            builder.addInterceptor(interceptor);
//            }

            builder.addInterceptor(new Interceptor() {
                @Override public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder()
                            .addHeader("User-Agent", "maskeyes")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")

                            .build();
                    return chain.proceed(request);
                }
            });

//            builder.addInterceptor(new UnauthorisedInterceptor(context));
            okClient = builder.build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASIC_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okClient)
                    .build();


            apiCommonService = retrofit.create(ApiCommonService.class);


        }catch (Exception ex){

            Log.e("init api", ex.toString());

        }
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }



    public ApiCommonService getApiCommonService(){ return apiCommonService; }


}
