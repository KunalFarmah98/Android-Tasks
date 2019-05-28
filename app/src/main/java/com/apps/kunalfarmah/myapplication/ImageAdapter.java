package com.apps.kunalfarmah.myapplication;

import android.content.Context;
import android.graphics.Movie;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends ArrayAdapter<file_struct> {

    private Context mContext;
    private List<file_struct> files_list = new ArrayList<>();

    public ImageAdapter( Context context,  ArrayList<file_struct> list) {
        super(context, 0 , list);
        mContext = context;
        files_list = list;
    }


    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {

        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        file_struct currentfile = files_list.get(position);



        TextView name = (TextView) listItem.findViewById(R.id.title);
        name.setText(currentfile.getName());

        TextView size = (TextView) listItem.findViewById(R.id.size);
        size.setText(currentfile.getSize()+" KB");

        return listItem;

    }
}
