package com.silentquot.Fragments;

import com.silentquot.Notifications.MyResponse;
import com.silentquot.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA8v47u3Y:APA91bFGcGpG4r3B0-3yWl79B2OFq_5F5vx-6woqDLFpaXLXA8QH3g6lzLnK04P9LuvMvjvAUbpIIzGPPcL9PLtEPxY9sZ-yIi8SObrdAp5bsaqqtIWYof54SrOHJkinHPR6F8KmqZw8"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
