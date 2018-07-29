package ict.ac.humanmotion.uapplication

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface UploadServer {

    @FormUrlEncoded
    @POST("save/{label}")
    fun postSave(@Path("label") label: String, @Field("result") str: String): Call<String>

    @FormUrlEncoded
    @POST("predict")
    fun postPredict(@Field("content") str: String): Call<String>
}