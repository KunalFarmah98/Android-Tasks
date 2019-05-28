package com.apps.kunalfarmah.myapplication;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    TextView uploading;
    ImageView img;
    EditText name;
    ProgressBar pb;
    Button upl,vimg;

   static File[] files;

    String path;

    ArrayList<File> list;

    Uri imageUri;
    File imageFile,file;

    final int PHOTO_PIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = new ArrayList<>();

        path = getApplicationContext().getFilesDir().toString();


        uploading = findViewById(R.id.uploading);
        upl = findViewById(R.id.upload);
        img = findViewById(R.id.img);
        vimg = findViewById(R.id.vie_img);
        name =findViewById(R.id.name);
        pb = findViewById(R.id.progress);

        pb.setVisibility(GONE);
        uploading.setVisibility(GONE);
        name.setVisibility(GONE);

        files = new File[]{};
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        //making a new directory named Images in the app folder
        file = wrapper.getDir("Images",MODE_PRIVATE);


        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pic = new Intent(Intent.ACTION_PICK);
                pic.setType("image/*");
                startActivityForResult(pic,PHOTO_PIC);
                name.setVisibility(View.VISIBLE);
            }
        });


        upl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (imageUri == null) {
                    Toast.makeText(getApplicationContext(), "Please Select a file to upload", Toast.LENGTH_SHORT).show();
                    return;
                }

         // compressing and saving image
                Bitmap bm = compressAndSave(imageFile);
                img.setImageBitmap(bm);
                name.setText(name.getText().toString()+".png");

                Toast.makeText(getApplicationContext(),"Successfuly Uploaded "+name.getText().toString() +".png "
                , Toast.LENGTH_SHORT).show();

                //using compressor library if required
//                imageFile.setReadOnly();
//                try {
//                    File compressedImageFile = new Compressor(getApplicationContext()).compressToFile(imageFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }


            }
        });

        vimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                int idx = path.indexOf("/files");
//                path = path.substring(0,idx);
//                path = path+"/app_images";

                path= "/data/data/com.apps.kunalfarmah.myapplication/app_Images";

//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    Uri uri = Uri.parse("content://com.apps.kunalfarmah.myapplication/" + path );
//                    intent.setDataAndType(uri,"resource/folder");
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    startActivity(intent);

                startActivity(new Intent(getApplicationContext(),Images.class));

                Log.d("Files", "Path: " + path);
                File directory = new File(path);
                files = directory.listFiles();
                Log.d("Files", "Size: "+ files.length);
                for (int i = 0; i < files.length; i++)
                {
                    Log.d("Files", "FileName:" + files[i].getName());
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PHOTO_PIC:
                if (resultCode == RESULT_OK) {

                        imageUri = data.getData();
                        img.setImageURI(imageUri);
                        imageFile = new File(getPathFromUri(imageUri));

                }
                break;
        }
    }


    private Bitmap compressAndSave(File f) {
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

        // max 1 MB image allowed
        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Log.d(TAG, "Width :" + b.getWidth() + " Height :" + b.getHeight());

        imageFile = new File(file, name.getText().toString()+".png");
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            list.add(imageFile);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }



    public String getPathFromUri(Uri uriPhoto) {
        if (uriPhoto == null)
            return null;

        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uriPhoto, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);

            String tempFilename = getTempFilename(this);
            output = new FileOutputStream(tempFilename);

            int read;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return tempFilename;
        } catch (IOException ignored) {
            // Nothing we can do
        } finally {

                closeSilently(input);
                closeSilently(output);
        }
        return null;
    }

    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            // Do nothing
        }
    }

    private static String getTempFilename(Context context) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("image", "tmp", outputDir);
        return outputFile.getAbsolutePath();
    }


    private HashMap<String, byte[]> encryptBytes(byte[] plainTextBytes, String passwordString)
    {
        HashMap<String, byte[]> map = new HashMap<String, byte[]>();

        try
        {
            //Random salt for next step
            SecureRandom random = new SecureRandom();
            byte salt[] = new byte[256];
            random.nextBytes(salt);

            //PBKDF2 - derive the key from the password, don't use passwords directly
            char[] passwordChar = passwordString.toCharArray(); //Turn password into char[] array
            PBEKeySpec pbKeySpec = new PBEKeySpec(passwordChar, salt, 1324, 256); //1324 iterations
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = secretKeyFactory.generateSecret(pbKeySpec).getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            //Create initialization vector for AES
            SecureRandom ivRandom = new SecureRandom(); //not caching previous seeded instance of SecureRandom
            byte[] iv = new byte[16];
            ivRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            //Encrypt
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainTextBytes);

            map.put("salt", salt);
            map.put("iv", iv);
            map.put("encrypted", encrypted);
        }
        catch(Exception e)
        {
            Log.e("MYAPP", "encryption exception", e);
        }

        return map;
    }


    private byte[] decryptData(HashMap<String, byte[]> map, String passwordString)
    {
        byte[] decrypted = null;
        try
        {
            byte salt[] = map.get("salt");
            byte iv[] = map.get("iv");
            byte encrypted[] = map.get("encrypted");

            //regenerate key from password
            char[] passwordChar = passwordString.toCharArray();
            PBEKeySpec pbKeySpec = new PBEKeySpec(passwordChar, salt, 1324, 256);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = secretKeyFactory.generateSecret(pbKeySpec).getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            //Decrypt
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            decrypted = cipher.doFinal(encrypted);
        }
        catch(Exception e)
        {
            Log.e("MYAPP", "decryption exception", e);
        }

        return decrypted;
    }



}

