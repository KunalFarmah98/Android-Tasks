package com.apps.kunalfarmah.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Images extends AppCompatActivity {

    ArrayList<file_struct> list;
    ListView lv;
    ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        list = new ArrayList<>();

        int length = MainActivity.files.length;

        for(int i=0; i<length; i++){
            list.add(new file_struct(MainActivity.files[i].getName(),String.valueOf(Math.round(Double.valueOf(MainActivity.files[i].length())*1.0/1000.0))
            , MainActivity.files[i].getPath()));
        }

        mAdapter = new ImageAdapter(this,list);

        lv = findViewById(R.id.list_images);
        lv.setAdapter(mAdapter);
    }



}
