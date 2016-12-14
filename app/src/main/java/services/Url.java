package services;

import java.util.Map;

import entry.TranslatedData;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Url {
    @FormUrlEncoded
    @POST("api/v1.5/tr.json/translate")
    Call<TranslatedData> update(@FieldMap Map<String, String> map);
}
