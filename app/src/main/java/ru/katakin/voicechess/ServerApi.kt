package ru.katakin.voicechess

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface ServerApi {

    @Headers("Content-Type: application/json")
    @POST("move")
    fun move(
        @Header("user") user: String,
        @Body fields: Map<String, String>
    ): Single<ResponseBody>
}