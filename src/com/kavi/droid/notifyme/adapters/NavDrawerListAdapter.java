package com.kavi.droid.notifyme.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kavi.droid.notifyme.views.NavDrawerItem;

import java.util.ArrayList;

/**
 * Created by Kavi on 11/5/14.
 * @author Kavimal Wijewardana <kavi707@gmail.com>
 */
public class NavDrawerListAdapter extends BaseAdapter {
	
	private Context context;
	private ArrayList<NavDrawerItem> navDrawerItems;
	
	public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
		this.context = context;
		this.navDrawerItems = navDrawerItems;
	}

	@Override
	public int getCount() {
		return navDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(com.kavi.droid.notifyme.R.layout.drawer_list_item, null);
        }
         
        ImageView imgIcon = (ImageView) convertView.findViewById(com.kavi.droid.notifyme.R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(com.kavi.droid.notifyme.R.id.title);
        TextView txtCount = (TextView) convertView.findViewById(com.kavi.droid.notifyme.R.id.counter);
         
        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
        txtTitle.setText(navDrawerItems.get(position).getTitle());
        
        // displaying count
        // check whether it set visible or not
        if(navDrawerItems.get(position).getCounterVisibility()){
        	txtCount.setText(navDrawerItems.get(position).getCount());
        }else{
        	// hide the counter view
        	txtCount.setVisibility(View.GONE);
        }
        
        return convertView;
	}

}
