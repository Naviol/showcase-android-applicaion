package com.plymouth.assessment.cw2.showcase;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ProjectApi {

    @Headers("api_key: COMP2000HK-Assessment-CW2")
    @GET("students")
    Call<List<Project>> getProjects();

    @Headers("api_key: COMP2000HK-Assessment-CW2")
    @GET("students/{id}")
    Call<Project> getProjectsById(@Path("id") int projectId);

    @Headers("api_key: COMP2000HK-Assessment-CW2")
    @DELETE("students/{id}")
    Call<Void> deleteProject(@Path("id") int id);

    @Headers("api_key: COMP2000HK-Assessment-CW2")
    @POST("students")
    Call<Void> createProject(@Body Project project);

    @Headers("api_key: COMP2000HK-Assessment-CW2")
    @PUT("students/{id}")
    Call<Project> updateProject(@Path("id") int id, @Body Project project);

    @Headers("api_key: COMP2000HK-Assessment-CW2")
    @Multipart
    @POST("students/{id}/image")
    Call<Void> uploadPhoto(@Path("id") int id, @Part MultipartBody.Part part);
}
