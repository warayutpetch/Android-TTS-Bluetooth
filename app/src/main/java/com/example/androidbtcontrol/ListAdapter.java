package com.example.androidbtcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by patipan on 9/21/2016 AD.
 */

public class ListAdapter extends BaseAdapter {
    Context context;
    //Dog dogs = new Dog();
    Dog dogs;

    ListAdapter(Context context, Dog dogs) {
        this.context = context;
        this.dogs = dogs;
    }

    @Override
    public int getCount() {
        if (dogs == null) return 0;
        if (dogs.getDogs() == null) return 0;

        return dogs.getDogs().size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = mInflater.inflate(R.layout.list_item, parent, false);
//        if(convertView != null){
//            view = convertView;
//        }else {
//            view = mInflater.inflate(R.layout.list_item, parent, false);
//        }

        TextView textView = (TextView) view.findViewById(R.id.title);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView textDesc = (TextView) view.findViewById(R.id.desc);


        if (dogs != null && dogs.getDogs() != null) {
            textView.setText(dogs.getDogs().get(position).getBreed());
            imageView.setImageResource(dogs.getDogs().get(position).getResId());
            textDesc.setText(dogs.getDogs().get(position).getDescription());
        }


//        if (convertView != null)
//            view = convertView;
        return view;


    }
}
