package com.plymouth.assessment.cw2.showcase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewProjectActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://web.socem.plymouth.ac.uk/COMP2000/api/";
    private ProjectApi projectApi;

    TextInputEditText eTitle, eYear, eDescription, studentId, first_name, second_name;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);

        eTitle = findViewById(R.id.title);
        eYear = findViewById(R.id.year);
        eDescription = findViewById(R.id.description);
        studentId = findViewById(R.id.studentId);
        first_name = findViewById(R.id.first_name);
        second_name = findViewById(R.id.second_name);
        submitButton = findViewById(R.id.submit_button);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
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

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (formValidation()) {
                    int studentID = Integer.parseInt(studentId.getText().toString());
                    String title = eTitle.getText().toString();
                    String description = eDescription.getText().toString();
                    int year = Integer.parseInt(eYear.getText().toString());
                    String firstName = first_name.getText().toString();
                    String secondName = second_name.getText().toString();

                    Project project = new Project(studentID, title, description,
                            year, firstName, secondName);

                    createProject(project);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void Back(View view) {
        onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean formValidation() {

        boolean isValid = true;
        EditText[] formElements = {eTitle, eYear, eDescription, studentId, first_name, second_name};

        for (EditText i : formElements) {
            if (i.getText().toString().trim().length() == 0) {
                i.setError("This field cannot be null");
                isValid = false;
            }
        }
        return isValid;
    }

    private void showSuccessMessage() {
        Toast.makeText(this, R.string.success, Toast.LENGTH_LONG).show();
    }

    private void createProject(Project project) {
        Call<Void> call = projectApi.createProject(project);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() != 201) {
                    Log.d("API Status Code", String.valueOf(response.code()));
                    Log.d("API Request", response.message());
                    return;
                }
                Log.d("API Request", response.message());

                showSuccessMessage();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}