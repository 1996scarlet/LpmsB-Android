package ict.ac.humanmotion.uapplication

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UploadServer {

    @FormUrlEncoded
    @POST("save")
    fun postSave(@Field("result") str: String): Call<String>
}