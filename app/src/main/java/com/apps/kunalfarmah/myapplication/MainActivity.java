package com.apps.kunalfarmah.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import id.zelory.compressor.Compressor;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    TextView uploading;
    ImageView img;
    ProgressBar pb;
    Button upl;

    File file;

    ArrayList<Bitmap> list;

    static Uri imageUri;

    final int PHOTO_PIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = new ArrayList<>();

        uploading = findViewById(R.id.uploading);
        upl = findViewById(R.id.upload);
        img = findViewById(R.id.img);
        pb = findViewById(R.id.progress);

        pb.setVisibility(GONE);
        uploading.setVisibility(GONE);


        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pic = new Intent(Intent.ACTION_PICK);
                pic.setType("image/*");
                startActivityForResult(pic,PHOTO_PIC);
            }
        });


        upl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (imageUri == null) {
                    Toast.makeText(getApplicationContext(), "Please Select a file to upload", Toast.LENGTH_SHORT).show();
                    return;
                }

                File imageFile = new File(imageUri.getPath());

                compressFile(imageFile);
//                imageFile.setReadOnly();
//                try {
//                    File compressedImageFile = new Compressor(getApplicationContext()).compressToFile(imageFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PHOTO_PIC:
                if (resultCode == RESULT_OK) {
                    try {
                        imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        img.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }


    private Bitmap compressFile(File f) {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.d("Compressed", "Width :" + b.getWidth() + " Height :" + b.getHeight());

        File destFile = new File(file, "img.png");
        try {
            FileOutputStream out = new FileOutputStream(destFile);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            // adding file bitmap to uploaded files list
            list.add(b);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

}

