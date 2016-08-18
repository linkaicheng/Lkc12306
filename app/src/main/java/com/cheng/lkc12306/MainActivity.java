package com.cheng.lkc12306;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cheng.lkc12306.adapter.MyFragmentAdapter;
import com.cheng.lkc12306.fragment.MyFragment;
import com.cheng.lkc12306.fragment.OrderFragment;
import com.cheng.lkc12306.fragment.TicketFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RadioButton rbMy,rbOrder,rbTicket;
    private RadioGroup mGroup;
    private ViewPager mPage;
    List<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化View
        initView();
        //初始化ViewPager
        initViewPager();
//***************************************test
    }
    private void initView(){
        mGroup = (RadioGroup)findViewById(R.id.radioGroup);
        mPage= (ViewPager) findViewById(R.id.viewPager);
        rbMy = (RadioButton)findViewById(R.id.rbMy);
        rbOrder = (RadioButton)findViewById(R.id.rbOrder);
        rbTicket = (RadioButton)findViewById(R.id.rbTicket);
        mGroup.setOnCheckedChangeListener(new MyCheckedChangeListener());
    }

    private void initViewPager(){
        MyFragment myFragment=new MyFragment();
        OrderFragment orderFragment=new OrderFragment();
        TicketFragment ticketFragment=new TicketFragment();
        fragments=new ArrayList<>();
        fragments.add(orderFragment);
        fragments.add(ticketFragment);
        fragments.add(myFragment);
        mPage.setAdapter(new MyFragmentAdapter(getSupportFragmentManager(),fragments));
        mPage.setCurrentItem(0);
        mPage.addOnPageChangeListener(new MyPageChangeListener());

    }
    private class MyCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case  R.id.rbOrder:
                    mPage.setCurrentItem(0,false);
                    break;
                case  R.id.rbTicket:
                    mPage.setCurrentItem(1,false);
                    break;
                case  R.id.rbMy:
                    mPage.setCurrentItem(2,false);
                    break;
            }
        }
    }
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case  0:
                    mGroup.check(R.id.rbOrder);
                    break;
                case  1:
                    mGroup.check(R.id.rbTicket);

                    break;
                case  2:
                    mGroup.check(R.id.rbMy);

                    break;
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

}
