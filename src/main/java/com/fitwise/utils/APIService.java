package com.fitwise.utils;

import com.fitwise.exercise.model.VimeoModel;
import com.fitwise.model.instructor.VimeoVersioningModel;
import com.fitwise.response.kloudless.MeetingWindow;
import com.fitwise.response.kloudless.VerifyTokenResponse;
import com.fitwise.view.dynamiclink.DynamicLinkRequest;
import retrofit2.Call;
import retrofit2.http.*;

public interface APIService {

    /**
     * API to retrieve a employee details based on the intent ID.
     *
     * @param request The intent request details of the employee.
     *
     * @return The response.
     *
     */
    @POST("me/videos")
    public Call<VimeoModel> createVideoPlaceholder(@Body VimeoModel request, @Header("Authorization") String token,@Header("Content-Type") String contentType, @Header("Accept") String accept);

    @PUT("me/projects/{projectId}/videos/{videoId}")
    public Call<Object> moveVideo(@Path("projectId") String projectId, @Path("videoId") String videoId, @Header("Authorization") String token);

    /**
     * Process the API for the vimeo video versioning
     * @param request
     * @param videoId
     * @param token
     * @param contentType
     * @param accept
     * @return
     */
    @POST("videos/{videoId}/versions")
    public Call<Object> createVideoVersion(@Body VimeoVersioningModel request, @Path("videoId") String videoId, @Header("Authorization") String token, @Header("Content-Type") String contentType, @Header("Accept") String accept);

    /**
     * API for Firebase Dynamic link creation
     * @param requestBody
     * @param contentType
     * @param webApiKey
     * @return
     */
    @POST("v1/shortLinks")
    public Call<Object> createShortLink(@Body DynamicLinkRequest requestBody, @Header("Content-Type") String contentType, @Query("key") String webApiKey);

    /**
     * Verify the kloudless account's access token granted to the application.
     * @param token access token
     * @return account and application details
     */
    @GET("v1/oauth/token")
    public Call<VerifyTokenResponse> verifyOauthToken(@Header("Authorization") String token);

    /**
     * Returns the kloudless meeting window.
     * @param token
     * @param meetingId
     * @return
     */
    @GET("/v1/meetings/windows/{id}/")
    public Call<MeetingWindow> getMeetingWindow(@Header("Authorization") String token, @Path("id") String meetingId);

}