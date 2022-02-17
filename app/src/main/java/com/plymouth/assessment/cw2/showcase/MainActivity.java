package com.plymouth.assessment.cw2.showcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://web.socem.plymouth.ac.uk/COMP2000/api/";

    private RecyclerView recyclerView;
    private ProjectApi projectApi;
    private FloatingActionButton floatingActionButton;
    private TextView loadingText;

    List<Project> projectList;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        loadingText = findViewById(R.id.loadingText);
        floatingActionButton = findViewById(R.id.floating_action_button);

        projectList = new ArrayList<>();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300,TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .callTimeout(300, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        projectApi = retrofit.create(ProjectApi.class);

        getProjects();

        // Support Swipe Refresh after loaded API
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(R.color.purple_500);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (recyclerView.getAdapter() != null) {
                    projectList.clear();
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
                showRefreshMessage();
                getProjects();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                startActivity(new Intent(getApplicationContext(), NewProjectActivity.class));
            }
        });
    }

    private void showRefreshMessage() {
        Toast.makeText(this,
                R.string.swipe_refresh_msg,
                Toast.LENGTH_LONG).show();
    }

    private void showRetryMessage() {
        Toast.makeText(this,
                R.string.retry_msg,
                Toast.LENGTH_LONG).show();
    }

    private void getProjects() {
        Call<List<Project>> call = projectApi.getProjects();

        loadingText.setText(R.string.loading_msg);
        call.enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                try {
                    if (!response.isSuccessful()) {
                        Log.d("API Status Code", String.valueOf(response.code()));
                        Log.d("API Request", response.message());
                        return;
                    }
                    Log.d("API Request", response.message());

                    List<Project> projects = response.body();

                    for (Project project: projects){
                        projectList.add(project);
                    }

                    retrieveDataInView(projectList);

                    loadingText.setText(null);
                } catch (Exception e) {
                    throw e;
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                showRetryMessage();
            }
        });
    }

    private void retrieveDataInView(List<Project> projectList) {
        Adapter adapter = new Adapter(this, projectList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}