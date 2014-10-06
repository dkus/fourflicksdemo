package com.github.dkus.fourflicks.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.dkus.fourflicks.R;


public class MainFragment extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = MapFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        view.findViewById(R.id.locations).setOnClickListener(this);
        view.findViewById(R.id.takePicture).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.locations) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container,
                    new MapFragment(),
                    MapFragment.FRAGMENT_TAG).addToBackStack(MapFragment.FRAGMENT_TAG).commit();
        } else if (v.getId() == R.id.takePicture) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container,
                    new MapFragment(),
                    MapFragment.FRAGMENT_TAG).addToBackStack(MapFragment.FRAGMENT_TAG).commit();
        }
    }
}
