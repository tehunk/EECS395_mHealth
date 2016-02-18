package edu.northwestern.mhealth395.neckmonitor;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William on 2/4/2016
 */
public class CustomAdapter extends BaseAdapter {
    private Activity mContext;
    private List<BluetoothDevice> mList;
    public List<Integer> streaming = new ArrayList<>();
    private LayoutInflater mLayoutInflater = null;

    public CustomAdapter(Activity context, List<BluetoothDevice> list) {
        mContext = context;
        mList = list;
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutInflater = context.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int pos) {
        return mList.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        mList.clear();
        streaming.clear();
    }

    public void add(BluetoothDevice o) {
        mList.add(o);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        convertView = mLayoutInflater.inflate(R.layout.item, null);

        TextView txt=(TextView)convertView.findViewById(R.id.text);

        txt.setText(mList.get(position).getName());

        if (streaming.contains(position)) {
            convertView.setBackgroundColor(Color.GREEN);
        } else {
            convertView.setBackgroundColor(Color.RED);
        }

        return convertView;
    }
}
