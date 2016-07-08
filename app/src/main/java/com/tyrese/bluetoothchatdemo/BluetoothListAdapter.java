package com.tyrese.bluetoothchatdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyrese on 2016/7/8.
 */
public class BluetoothListAdapter extends BaseAdapter {

    private List<DeviceItem> mData = new ArrayList<>();
    private Context mCtx;
    private LayoutInflater mInflater;

    public BluetoothListAdapter(Context context) {
        mCtx = context;
        mInflater = LayoutInflater.from(mCtx);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_list, null);
            viewHolder.mTextName = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.mTextAddress = (TextView) convertView.findViewById(R.id.device_address);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        DeviceItem item = mData.get(position);
        viewHolder.mTextName.setText(item.getName());
        viewHolder.mTextAddress.setText(item.getAddress());
        return convertView;
    }

    public void addItems(List<DeviceItem> items) {
        mData.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(DeviceItem item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView mTextName;
        TextView mTextAddress;
    }
}
