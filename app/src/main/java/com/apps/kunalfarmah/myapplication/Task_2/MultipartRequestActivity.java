package com.apps.kunalfarmah.myapplication.Task_2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apps.kunalfarmah.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MultipartRequestActivity extends AppCompatActivity {

    // auth token for volley multipart requests
    private static SharedPreferences mtoken;
    private SharedPreferences.Editor mTokenEditor;

    ImageView img;
    EditText name;
    Button upl, vimg, t2;
    String token = "";

    Uri imageUri;

    final int PHOTO_PIC = 1;

    final String AUTH_URL = "https://sample-auth.herokuapp.com/requestToken";
    // url for uploading an image

    final String URL_IMAGE = "";
    private int REQUEST_SUCCESS = 1;

    HashMap<String, String> header;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        upl = findViewById(R.id.upload);
        img = findViewById(R.id.img);
        vimg = findViewById(R.id.vie_img);
        name = findViewById(R.id.name);
        t2 = findViewById(R.id.task2);
        t2.setVisibility(View.GONE);
        vimg.setVisibility(View.GONE);

        mtoken = getSharedPreferences("auth-token", MODE_PRIVATE);
        mTokenEditor = mtoken.edit();
//        mTokenEditor.putString("token","SportsAppTask2");

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pic = new Intent(Intent.ACTION_PICK);
                pic.setType("image/*");
                startActivityForResult(pic, PHOTO_PIC);
                img.setBackground(getDrawable(R.drawable.black));
                name.setVisibility(View.VISIBLE);
                name.setText("");
                name.setEnabled(true);
            }
        });


        upl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (imageUri == null) {
                    Toast.makeText(getApplicationContext(), "Please Select a File", Toast.LENGTH_SHORT).show();
                }

                name.setText(name.getText().toString() + ".png");
                name.setEnabled(false);
                uploadData();
                // if actual data would be recieved
                //loadImage();
            }
        });

        // creating a timer to make sure token is fetched after 1 hour of the previous token
        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                        AUTH_URL, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("auth", response.toString());
                                // setting token for headers
                                token = response.toString();
                                mTokenEditor.putString("token", token).apply();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("VolleyError", "Error: " + error.getMessage());
                    }
                });

                // setting header once every 1 hour

                header = new HashMap<String, String>();
                String token_ = getSharedPreferences("auth-token", MODE_PRIVATE).getString("token", null);
                try {
                    // extracting the correct token
                    int i = token_.indexOf(':');
                    token_ = token_.substring(i + 2, token_.length() - 2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // putting the token as header
                header.put("auth-token", token_);


                //Adding request to request queue
                VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(jsonObjReq);

            }

        };

// schedule the task to run starting now and then every hour...
        timer.schedule(hourlyTask, 0l, 1000 * 60 * 60 * 60);   // every hour after the first call
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PHOTO_PIC:
                if (resultCode == RESULT_OK) {

                    imageUri = data.getData();
                    img.setImageURI(imageUri);
                }
                break;
        }
    }


    private void uploadData() {

        String url = "https://sample-auth.herokuapp.com";
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, url, header, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    String data = "";
                    boolean valid = result.getBoolean("success");
                    Log.d("success", String.valueOf(result.getBoolean("success")));
                    if (valid) {
                        data = result.getString("data");
                    }
                    String message = result.getString("message");

                    if (valid) {
                        Log.d("Accepted", data);
                        Toast.makeText(getApplicationContext(), "Sucessfully Sent File " + ((name.getText().toString() != null) ? name.getText().toString() : "")
                                , Toast.LENGTH_SHORT).show();
                    } else if (!valid) {
                        Log.d("TokenError", message);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        throw new VolleyError();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (VolleyError e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    } else if (error.getClass().equals(VolleyError.class)) {
                        errorMessage = "Authentication Failed";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message + " Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message + " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message + " Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }); //{
         /*   @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("api_token", getApplicationContext().getSharedPreferences("auth_token", 0).getString("token", null));
                params.put("name", "Kunal  Farmah");
                params.put("location", "New Delhi");
                params.put("about", "Test");
                params.put("contact", "SportsApp");
                return params;
            }*/

// not setting header every call
      /*      @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // setting header for the subsequent requests

                header = new HashMap<String, String>();
                String token_ = getSharedPreferences("auth-token", MODE_PRIVATE).getString("token", null);

                // extracting the correct token
                int i = token_.indexOf(':');
                token_ = token_.substring(i + 2, token_.length() - 2);

                // putting the token as header
                header.put("auth-token", token_);

                return header;
            }*/
      //  };
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
    }


/*
     if an image was to be retrieved
*/

    public void loadImage() {

        ImageLoader imageLoader = VolleySingleton.getInstance(getBaseContext()).getImageLoader();

        imageLoader.get(URL_IMAGE, new ImageLoader.ImageListener() {

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                // setImageView
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ImageError", "Image Load Error: " + error.getMessage());
            }

        });
    }


    /**
     * Turn drawable resource into byte array.
     *
     * @param context parent context
     * @param id      drawable resource id
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Context context, int id) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Turn drawable into byte array.
     *
     * @param drawable data
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Context context, Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


}

