package com.cheng.lkc12306.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by cheng on 2016/8/18.
 */
public class MyFragmentAdapter extends FragmentPagerAdapter {
    List<Fragment> fragments;
    public MyFragmentAdapter(FragmentManager fm,List<Fragment> fragments){
        super(fm);
        this.fragments=fragments;

    }
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
