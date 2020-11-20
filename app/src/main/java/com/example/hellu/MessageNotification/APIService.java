package com.example.hellu.MessageNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {"Content-Type:application/json",
                    "Authorization:key=AAAAw4ycOIA:APA91bEA642Qfvqgfm-SmP7iBagewr_kXfY3L8pP1H3tec0yZtrYn94_EtPkrc-" +
                            "YoxDH3IpZHMfgPxYXecH6wEHbleLggQlo36HQg4mamvfIY4e5Ft9XpmTnM_EqSvAZKv8cJIlUAexZ"
                    //server key trong firebase cloud messaging
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
