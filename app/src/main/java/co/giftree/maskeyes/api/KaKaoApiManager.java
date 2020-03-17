package co.giftree.maskeyes.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import co.giftree.maskeyes.model.AddressInfo;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KaKaoApiManager {

    //13.125.214.78
    public static final String BASIC_URL = "https://dapi.kakao.com";

    private AddressService addressService;


//    private APIService tempService;

    private Retrofit retrofit;
    protected Gson gson;
    protected static OkHttpClient okClient;
    private volatile static KaKaoApiManager instance = null;

    public static KaKaoApiManager getInstance(){
        if(instance==null){
            synchronized (KaKaoApiManager.class){
                if(instance==null){
                    instance = new KaKaoApiManager();
                }
            }
        }
        return instance;
    }


    public KaKaoApiManager(){
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
                            .addHeader("Authorization", "KakaoAK 0fede97417a99808a41a466fc84a9bbe")
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


            addressService = retrofit.create(AddressService.class);


        }catch (Exception ex){

            Log.e("init api", ex.toString());

        }
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }



    public AddressService getAddressService(){ return addressService; }

}
