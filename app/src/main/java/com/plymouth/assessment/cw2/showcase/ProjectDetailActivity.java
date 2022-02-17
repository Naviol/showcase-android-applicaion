package com.plymouth.assessment.cw2.showcase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProjectDetailActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://web.socem.plymouth.ac.uk/COMP2000/api/";
    public static String CHANNEL_ID = "Channel";
    private static final int PICK_IMAGE = 34;
    private ProjectApi projectApi;
    private String thumbnailURL;
    private String posterURL;
    private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManager;

    // Actionbar
    private Boolean isUploadNotify = true;

    private int studentId;
    private String title;
    private String description;
    private int year;
    private String first_name;
    private String second_name;

    TextView projectIdText, studentIdText, titleText, descText, first_name_text, second_name_text, yearText, poster, posterText;
    EditText studentIdUpdate, titleUpdate, descUpdate, first_name_update, second_name_update, yearUpdate;
    ImageView photoBitmap, thumbnail, posterImg;
    Bitmap decodedPhoto;
    Button deleteButton, updateButton, submitButton, cancelButton;
    ImageButton thumbnailUpdate, photoAddButton, thumbnailSubmit, posterUpdate, posterSubmit;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        createNotificationChannel();
        ActionBar actionBar = getSupportActionBar();

        Intent intent = getIntent();
        String mTitle = intent.getStringExtra("iTitle");

        // Initial Elements
        thumbnail = findViewById(R.id.thumbnail);
        poster = findViewById(R.id.poster);
        posterText = findViewById(R.id.posterText);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        projectIdText = findViewById(R.id.projectIdText);
        studentIdText = findViewById(R.id.studentIdText);
        yearText = findViewById(R.id.yearText);
        titleText = findViewById(R.id.titleText);
        descText = findViewById(R.id.descText);
        first_name_text = findViewById(R.id.first_name_text);
        second_name_text = findViewById(R.id.second_name_text);
        photoBitmap = findViewById(R.id.photoBitmap);
        photoAddButton = findViewById(R.id.photoAddButton);
        posterImg = findViewById(R.id.posterImg);

        // Edit Mode
        submitButton = findViewById(R.id.submitButton);
        cancelButton = findViewById(R.id.cancelButton);
        studentIdUpdate = findViewById(R.id.studentIdUpdate);
        yearUpdate = findViewById(R.id.yearUpdate);
        titleUpdate = findViewById(R.id.titleUpdate);
        descUpdate = findViewById(R.id.descUpdate);
        first_name_update = findViewById(R.id.first_name_update);
        second_name_update = findViewById(R.id.second_name_update);
        thumbnailUpdate = findViewById(R.id.thumbnailUpdate);
        thumbnailSubmit = findViewById(R.id.thumbnailSubmit);
        posterUpdate = findViewById(R.id.posterUpdate);
        posterSubmit = findViewById(R.id.posterSubmit);

        actionBar.setTitle(mTitle);

        int projectId = intent.getIntExtra("ProjectId",0);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,projectId, intent,
                PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(this,
                CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Showcase")
                .setContentText("Image uploaded! Click here to get back to project detail")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager = NotificationManagerCompat.from(this);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

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

        getProjectsById(projectId);
    }

    private void getProjectsById(int projectIdInt) {

        Call<Project> call = projectApi.getProjectsById(projectIdInt);

        call.enqueue(new Callback<Project>() {
            @Override
            public void onResponse(Call<Project> call, Response<Project> response) {
                if (response.code() != 200) {
                    Log.e("API Status Code", String.valueOf(response.code()));
                    Log.e("API Request", response.message());
                    return;
                }
                Log.d("API Request", response.message());

                Project project = response.body();

                projectIdText.setText(Integer.toString(project.getProjectID()));
                studentIdText.setText(Integer.toString(project.getStudentID()));
                yearText.setText(Integer.toString(project.getYear()));
                titleText.setText(project.getTitle());
                descText.setText(project.getDescription());
                first_name_text.setText(project.getFirst_Name());
                second_name_text.setText(project.getSecond_Name());

                // Preset EditText field content
                studentIdUpdate.setText(Integer.toString(project.getStudentID()));
                yearUpdate.setText(Integer.toString(project.getYear()));
                titleUpdate.setText(project.getTitle());
                descUpdate.setText(project.getDescription());
                first_name_update.setText(project.getFirst_Name());
                second_name_update.setText(project.getSecond_Name());

                thumbnailURL = project.getThumbnailURL();
                setThumbnail();

                posterURL = project.getPosterURL();

                if (posterURL == null || !URLUtil.isValidUrl(posterURL)) {
                    posterText.setText(R.string.na);
                } else {
                    posterText.setVisibility(View.INVISIBLE);
                    posterImg.setVisibility(View.VISIBLE);
                }
                setPoster();

                String photoBase64 = project.getPhoto();
                try {
                    if (photoBase64 != null) {
                        byte[] bytes = Base64.decode(photoBase64, Base64.DEFAULT);
                        decodedPhoto = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        photoBitmap.setImageBitmap(decodedPhoto);
                        photoAddButton.setVisibility(View.INVISIBLE);
                    } else {
                        photoBitmap.setVisibility(View.GONE);
                        photoAddButton.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    throw e;
                }
            }

            @Override
            public void onFailure(Call<Project> call, Throwable t) {

            }

        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteRecordAlertDialog();
            }
        });

        photoAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageDialog();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeEditMode();
            }
        });
    }

    private void setThumbnail() {
        if (thumbnailURL != null) {
            Glide.with(this)
                    .load(thumbnailURL)
                    .placeholder(R.drawable.null_image)
                    .error(R.drawable.null_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.d("Glide", "Thumbnail image load failed from " + thumbnailURL);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("Glide", "Loaded thumbnail from " + thumbnailURL);
                            return false;
                        }
                    })
                    .into(thumbnail);
        }
    }

    private void setPoster() {
        if (posterURL != null) {
            Glide.with(this)
                    .load(posterURL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.d("Glide", "Poster image load failed from " + posterURL);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("Glide", "Loaded poster from " + posterURL);
                            return false;
                        }
                    })
                    .into(posterImg);
        }
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
            case R.id.action_notification:
                if (isUploadNotify) {
                    Log.d("Actionbar", "Notification Off");
                    item.setIcon(R.drawable.ic_baseline_notifications_off_24);
                    isUploadNotify = false;
                } else {
                    Log.d("Actionbar", "Notification On");
                    item.setIcon(R.drawable.ic_baseline_notifications_active_24);
                    isUploadNotify = true;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeEditMode() {
        // Original Fields
        thumbnail.setVisibility(View.INVISIBLE);
        studentIdText.setVisibility(View.INVISIBLE);
        yearText.setVisibility(View.INVISIBLE);
        posterText.setVisibility(View.INVISIBLE);
        titleText.setVisibility(View.INVISIBLE);
        descText.setVisibility(View.INVISIBLE);
        first_name_text.setVisibility(View.INVISIBLE);
        second_name_text.setVisibility(View.INVISIBLE);
        updateButton.setVisibility(View.INVISIBLE);
        deleteButton.setVisibility(View.INVISIBLE);
        if (photoAddButton.getVisibility() == View.VISIBLE) {
            photoAddButton.setImageResource(R.drawable.ic_outline_image_not_supported_128);
            photoAddButton.setClickable(false);
        }

        // Edit Fields
        thumbnailUpdate.setVisibility(View.VISIBLE);
        studentIdUpdate.setVisibility(View.VISIBLE);
        yearUpdate.setVisibility(View.VISIBLE);
        posterUpdate.setVisibility(View.VISIBLE);
        titleUpdate.setVisibility(View.VISIBLE);
        descUpdate.setVisibility(View.VISIBLE);
        first_name_update.setVisibility(View.VISIBLE);
        second_name_update.setVisibility(View.VISIBLE);
        submitButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        thumbnailUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thumbnailDialog();
            }
        });

        posterUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posterDialog();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitEditMode();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                int projectId = intent.getIntExtra("ProjectId", 0);

                if (formValidation()) {
                    studentId = Integer.parseInt(studentIdUpdate.getText().toString());
                    title = titleUpdate.getText().toString();
                    description = descUpdate.getText().toString();
                    year = Integer.parseInt(yearUpdate.getText().toString());
                    first_name = first_name_update.getText().toString();
                    second_name = second_name_update.getText().toString();
                }

                Project project = new Project(studentId, title, description, year, first_name,
                        second_name, posterURL);
                updateProject(projectId, project);
            }
        });
    }

    private boolean formValidation() {
        if (studentIdUpdate.getText().toString().trim().length() == 0) {
            studentIdUpdate.setError("This field cannot be null");
            return false;
        } else if (yearUpdate.getText().toString().trim().length() == 0) {
            yearUpdate.setError("This field cannot be null");
            return false;
        } else if (titleUpdate.getText().toString().trim().length() == 0) {
            titleUpdate.setError("This field cannot be null");
            return false;
        } else if (descUpdate.getText().toString().trim().length() == 0) {
            descUpdate.setError("This field cannot be null");
            return false;
        } else if (first_name_update.getText().toString().trim().length() == 0) {
            first_name_update.setError("This field cannot be null");
            return false;
        } else if (second_name_update.getText().toString().trim().length() == 0) {
            second_name_update.setError("This field cannot be null");
            return false;
        }
        return true;
    }

    private void exitEditMode() {
        // Edit Fields
        thumbnailUpdate.setVisibility(View.INVISIBLE);
        posterUpdate.setVisibility(View.INVISIBLE);
        studentIdUpdate.setVisibility(View.INVISIBLE);
        yearUpdate.setVisibility(View.INVISIBLE);
        titleUpdate.setVisibility(View.INVISIBLE);
        descUpdate.setVisibility(View.INVISIBLE);
        first_name_update.setVisibility(View.INVISIBLE);
        second_name_update.setVisibility(View.INVISIBLE);
        submitButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);
        thumbnailSubmit.setImageURI(null);
        thumbnailSubmit.setVisibility(View.INVISIBLE);
        posterSubmit.setImageURI(null);
        posterSubmit.setVisibility(View.INVISIBLE);

        // Original Fields
        thumbnail.setVisibility(View.VISIBLE);
        studentIdText.setVisibility(View.VISIBLE);
        yearText.setVisibility(View.VISIBLE);
        if (posterImg.getVisibility() != View.VISIBLE) {
            posterText.setVisibility(View.VISIBLE);
        }
        titleText.setVisibility(View.VISIBLE);
        descText.setVisibility(View.VISIBLE);
        first_name_text.setVisibility(View.VISIBLE);
        second_name_text.setVisibility(View.VISIBLE);
        updateButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
        if (photoAddButton.getVisibility() == View.VISIBLE) {
            photoAddButton.setImageResource(R.drawable.ic_baseline_add_photo_alternate_128);
            photoAddButton.setClickable(true);
        }
    }

    private void thumbnailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.add_url));
        builder.setCancelable(false);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thumbnailURL = input.getText().toString();
                Intent intent = getIntent();
                int projectId = intent.getIntExtra("ProjectId", 0);

                Project project = new Project(thumbnailURL);

                updateProject(projectId, project);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void posterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.add_url));
        builder.setCancelable(false);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                posterURL = input.getText().toString();
                posterSubmit.setImageResource(R.drawable.ic_baseline_check_circle_green_24);
                posterUpdate.setVisibility(View.INVISIBLE);
                posterSubmit.setVisibility(View.VISIBLE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void deleteRecordAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.delete_confirm_message));
        builder.setCancelable(false);
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = getIntent();
                int projectId = intent.getIntExtra("ProjectId", 0);
                deleteProject(projectId);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showSuccessMessage() {
        Toast.makeText(this, R.string.success, Toast.LENGTH_LONG).show();
    }

    private void showUploading() {
        Toast.makeText(this, R.string.uploading, Toast.LENGTH_SHORT).show();
    }

    private void updateProject(int projectId, Project project) {
        Call<Project> call = projectApi.updateProject(projectId, project);
        call.enqueue(new Callback<Project>() {
            @Override
            public void onResponse(Call<Project> call, Response<Project> response) {
                try {
                    if (response.code() != 204) {
                        Log.e("API Status Code", String.valueOf(response.code()));
                        Log.e("API Request", response.message());
                        return;
                    }
                    Log.d("API Request", response.message());

                    Intent intent = getIntent();

                    showSuccessMessage();
                    finish();
                    startActivity(intent);

                } catch (Exception e) {
                    throw e;
                }
            }

            @Override
            public void onFailure(Call<Project> call, Throwable t) {

            }
        });

    }

    private void deleteProject(int projectId) {
        Call<Void> call = projectApi.deleteProject(projectId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() != 204) {
                    Log.e("API Status Code", String.valueOf(response.code()));
                    Log.e("API Request", response.message());
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

    private void selectImageDialog() {
        if (ActivityCompat.checkSelfPermission(ProjectDetailActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProjectDetailActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PICK_IMAGE);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    Intent intent = getIntent();
                    int projectIdInt = intent.getIntExtra("ProjectId", 0);

                    uploadPhoto(projectIdInt, selectedImageUri);

                    showUploading();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        }
    }

    private void uploadPhoto(int projectId, Uri imageUri) {
        File file = null;
        OutputStream outputStream = null;

        try {
            InputStream imageStream = this.getContentResolver().openInputStream(imageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            String path = this.getExternalCacheDir().toString();

            file = new File(path, "image.jpg");

            outputStream = new FileOutputStream(file);
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestBody requestFile = RequestBody.create(file, MultipartBody.FORM);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<Void> call = projectApi.uploadPhoto(projectId, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() != 201) {
                    Log.e("API Status Code", String.valueOf(response.code()));
                    Log.e("API Request", response.message());
                    return;
                }
                Log.d("API Request", response.message());

                if (isUploadNotify) {
                    notificationManager.notify(100, builder.build());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
