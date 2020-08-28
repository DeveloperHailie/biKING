package com.sungshin.biking.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sungshin.biking.R;

import java.util.ArrayList;



public class rankingListAdaptor extends BaseAdapter {
    public ArrayList<rankingListItem> listViewItemList = new ArrayList<rankingListItem>();

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
            convertView = inflater.inflate(R.layout.ranking_list_item, parent, false);
        }

        TextView ranking = (TextView) convertView.findViewById(R.id.ranking_ranking);
        ImageView image = (ImageView) convertView.findViewById(R.id.ranking_image);
        TextView name = (TextView) convertView.findViewById(R.id.ranking_username);
        TextView exp = (TextView) convertView.findViewById(R.id.ranking_exp);

        rankingListItem item = listViewItemList.get(position);

        name.setText(item.getName()+"님");
        ranking.setText(item.getRanking()+"위");
        exp.setText("누적 경험치 "+item.getExp());
        image.setImageResource(item.getImage());


        return convertView;
    }

    public void addItem(int ranking, String name, String exp, int image ){
        rankingListItem item = new rankingListItem();
        item.setRanking(ranking);
        item.setName(name);
        item.setExp(exp);
        item.setImage(image);

        listViewItemList.add(item);

    }

    public void clearItem(){
        listViewItemList.clear();

    }

}
