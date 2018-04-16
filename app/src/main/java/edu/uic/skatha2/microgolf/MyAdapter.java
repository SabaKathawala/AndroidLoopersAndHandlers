package edu.uic.skatha2.microgolf;

import android.widget.ListAdapter;

/**
 * Created by sabask on 4/14/18.
 */

import android.widget.ArrayAdapter;
import android.content.Context;
import java.util.List;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

/**
 * Created by sabask on 2/27/18.
 */

class MyAdapter<T> extends ArrayAdapter<Hole> {

    private Context mContext;
    public Hole[] holesArray;

    public MyAdapter(Context context, Hole[] holes) {
        super(context, 0 , holes);
        mContext = context;
        this.holesArray = holes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = LayoutInflater.from(mContext).inflate(R.layout.holes, parent,false);

        TextView hole = (TextView) listItem.findViewById(R.id.hole);
        hole.setText("");
        hole.setBackgroundResource(holesArray[position].getColor());

        return listItem;
    }
}
