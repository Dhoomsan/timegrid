package com.example.evo09.timetablemanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.relex.circleindicator.CircleIndicator;


public class Instruction extends Fragment implements ViewPager.OnPageChangeListener{
    private ViewPager viewPager;
    CircleIndicator indicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.app_name);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview= inflater.inflate(R.layout.fragment_instruction, container, false);

        viewPager = (ViewPager)rootview. findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewInstAdapter(getChildFragmentManager()));
        viewPager.getAdapter().notifyDataSetChanged();
        viewPager.addOnPageChangeListener(this);
        indicator = (CircleIndicator)rootview. findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);

        setHasOptionsMenu(true);
        return rootview;
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
       // addBottomDots(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
