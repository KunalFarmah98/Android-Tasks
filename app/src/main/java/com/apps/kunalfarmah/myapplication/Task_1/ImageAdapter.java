package com.apps.kunalfarmah.myapplication.Task_1;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.kunalfarmah.myapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static android.content.Context.MODE_PRIVATE;


public class ImageAdapter extends ArrayAdapter<file_struct> {

    private Context mContext;
    private List<file_struct> files_list = new ArrayList<>();

     SharedPreferences settings;
     String stringArray ="";
     File file;

    public ImageAdapter( Context context,  ArrayList<file_struct> list) {
        super(context, 0 , list);
        mContext = context;
        files_list = list;
    }


    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {

        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        final file_struct currentfile = files_list.get(position);

        TextView name = listItem.findViewById(R.id.title);
        name.setText(currentfile.getName());

        TextView size = listItem.findViewById(R.id.size);
        size.setText(currentfile.getSize()+" KB");

        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = currentfile.getName();
               // String path = currentfile.getPath();

                byte[] decryptedbytes = decryptData("yyhs@98KF",name);
                if(decryptedbytes==null){
                    Toast.makeText(mContext,"Oops! Can't Decrypt the File :(",Toast.LENGTH_SHORT).show();
                    return;
                }

                ContextWrapper wrapper = new ContextWrapper(mContext);

                //making a new directory named Images in the app folder
              file = wrapper.getDir("Temp",MODE_PRIVATE);


                 File temp = new File(file, "temp.png");

                try {
                    FileOutputStream fos=new FileOutputStream(temp.getPath());
                    fos.write(decryptedbytes);
                    fos.close();
                }
                catch (java.io.IOException e) {
                    Toast.makeText(mContext,"Oops! Can't Decrypt the File :(",Toast.LENGTH_SHORT).show();
                    Log.e("PictureDemo", "Exception in photoCallback", e);
                }

                try {
                    Intent view_image = new Intent(mContext, ViewImageActivity.class);
                    view_image.putExtra("bytes", decryptedbytes);
                    mContext.startActivity(view_image);
                }
                catch (Exception e){
                    e.printStackTrace();
                }


/* trying to open using file manager*/

//                Intent open = new Intent(Intent.ACTION_VIEW);
//                Uri uri = Uri.parse("content://com.apps.kunalfarmah.myapplication/" + temp.getPath() );
//                open.setDataAndType(uri,"image/*");
//                mContext.startActivity(open);

            }
        });


        return listItem;

    }

    private byte[] decryptData(String passwordString, String filename)
    {
        byte[] decrypted = null;

        byte[] salt={},iv={},encrypted={};
        try
        {

//            settings = mContext.getSharedPreferences("salt", 0);
//            stringArray = settings.getString(filename, null);

            stringArray = mContext.getSharedPreferences("salt",0).getString(filename,null);

            if (stringArray != null) {
                String[] split = stringArray.substring(1, stringArray.length()-1).split(", ");
                salt = new byte[split.length];
                for (int i = 0; i < split.length; i++) {
                    salt[i] = Byte.parseByte(split[i]);
                }
            }

//            settings = mContext.getSharedPreferences("iv", 0);
//            stringArray = settings.getString(filename, null);
            stringArray = mContext.getSharedPreferences("iv",0).getString(filename,null);

            if (stringArray != null) {
                String[] split = stringArray.substring(1, stringArray.length()-1).split(", ");
                iv = new byte[split.length];
                for (int i = 0; i < split.length; i++) {
                    iv[i] = Byte.parseByte(split[i]);
                }
            }

//            settings = mContext.getSharedPreferences("encrypted", 0);
//            stringArray = settings.getString(filename, null);
            stringArray = mContext.getSharedPreferences("encrypted",0).getString(filename,null);
            if (stringArray != null) {
                String[] split = stringArray.substring(1, stringArray.length()-1).split(", ");
                encrypted = new byte[split.length];
                for (int i = 0; i < split.length; i++) {
                    encrypted[i] = Byte.parseByte(split[i]);
                }
            }

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
            Toast.makeText(mContext,"Oops! Can't Decrypt the File :(",Toast.LENGTH_SHORT).show();
            Log.e("MYAPP", "decryption exception", e);
        }

        return decrypted;
    }

}
