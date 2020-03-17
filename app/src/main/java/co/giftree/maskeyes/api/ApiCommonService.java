package co.giftree.maskeyes.api;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiCommonService {
    @GET("/corona19-masks/v1/storesByGeo/json")
    Call<JsonElement> storesByGeo(@Query("lat") String lat, @Query("lng") String lng, @Query("m") String m);
}
