package com.sungshin.biking.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.sungshin.biking.R;

import java.util.ArrayList;

public class mapPathListAdaptor  extends BaseAdapter {
    public ArrayList<mapPathListItem> listViewItemList = new ArrayList<mapPathListItem>();

    @Override
    public int getCount(){
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if( convertView == null ){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.map_path_list_item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.path_name);
        TextView location = (TextView) convertView.findViewById(R.id.path_location);
        Button condition = (Button) convertView.findViewById(R.id.path_confirm);

        mapPathListItem item = listViewItemList.get(position);

        name.setText(item.getName());
        location.setText(item.getLocation());
        condition.setText(item.getKm()+"km");


        return convertView;
    }

    public void addItem(String name, String location, String km ){
        mapPathListItem item = new mapPathListItem();

        item.setName(name);
        item.setLocation(location);
        item.setKm(km);

        listViewItemList.add(item);

    }

    public void clearItem(){
        listViewItemList.clear();

    }

}
