package co.giftree.maskeyes.api;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AddressService {
    @GET("/v2/local/search/address.json")
    Call<JsonElement> searchAddress(@Query("query") String query);
}
