package com.cheng.lkc12306.ticket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.cheng.lkc12306.R;

public class SearchResultActivity extends AppCompatActivity {
private TextView tvDayBefore,tvDayAfter,tvTicket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        initView();
    }
    private void initView(){
        tvDayBefore = (TextView)findViewById(R.id.tvDayBefore);
        tvDayAfter = (TextView)findViewById(R.id.tvDayAfter);
        tvTicket = (TextView)findViewById(R.id.tvTicket);


    }
}