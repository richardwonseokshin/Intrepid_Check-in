package com.intrepid.wonseokshin.intrepidcheckin;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

public interface SlackWebhookService {
    @POST("/services/{webhook_url}")
    void sendCheckinMessage(@Path(value = "webhook_url", encode = false) String urlKey, @Body SlackMessage message, Callback<Object> callback);
}
