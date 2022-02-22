package com.example.how_you_doin_.notifications

import com.example.how_you_doin_.notifications.Constants.Companion.CONTENT_TYPE
import com.example.how_you_doin_.notifications.Constants.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification):
            Response<ResponseBody>
}