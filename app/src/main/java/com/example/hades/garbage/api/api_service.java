package com.example.hades.garbage.api;
import android.renderscript.Sampler;

import com.example.hades.garbage.model.*;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
/**
 * Created by Hades on 4/29/2018.
 */

public interface api_service {

    @FormUrlEncoded
    @POST("insert.php")
    Call<ResponseModel> daftar(@Field("email") String email,
                       @Field("hp") String hp,
                       @Field("pass") String pass);


    @FormUrlEncoded
    @POST("cek_login.php")
    Call<ResponseModel> cek(@Field("user") String ktp,
                            @Field("pass") String nama);

}
