package com.android.minu.notifyme.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.minu.notifyme.R;

public class ContactsFragment extends Fragment {
	
	public ContactsFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
         
        return rootView;
    }
}
