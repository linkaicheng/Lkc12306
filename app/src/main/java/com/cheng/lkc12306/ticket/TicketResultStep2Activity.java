package com.cheng.lkc12306.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.cheng.lkc12306.R;

public class TicketResultStep2Activity extends AppCompatActivity {
    //日期，站点标题
    private TextView tvTicketResultStep2DateTitle, tvTicketResultStep2StationTitle;
    //前一天，后一天
    private TextView tvTicketResultStep2Before, tvTicketResultStep2After;
    //listview
    private ListView lvTicketResultStep2;
    //出发站点
    private String stationFrom = null;
    //目的站点
    private String stationTo = null;
    //列车编号
    private String trainNo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step2);
        initView();
    }

    //初始或控件，并设置监听
    private void initView() {
        tvTicketResultStep2StationTitle = (TextView) findViewById(R.id.tvTicketResultStep2StationTitle);
        tvTicketResultStep2DateTitle = (TextView) findViewById(R.id.tvTicketResultStep2DateTitle);
        tvTicketResultStep2Before = (TextView) findViewById(R.id.tvTicketResultStep2Before);
        tvTicketResultStep2After = (TextView) findViewById(R.id.tvTicketResultStep2After);
        lvTicketResultStep2 = (ListView) findViewById(R.id.lvTicketResultStep2);

        //获取上一界面TicketResultStep1Activity带过来的数据
        Intent intent = getIntent();
        stationFrom = intent.getStringExtra("fromStationName");
        stationTo = intent.getStringExtra("toStationName");
        trainNo = intent.getStringExtra("trainNo");
        String startTrainDate = intent.getStringExtra("startTrainDate");
        //显示出发地目的地，出发日期
        tvTicketResultStep2StationTitle.setText(stationFrom + "-" + stationTo);
        tvTicketResultStep2DateTitle.setText(startTrainDate);
        //前一天，后一天设置监听
        tvTicketResultStep2Before.setOnClickListener(new TvTicketBeAndAfListener());
        tvTicketResultStep2After.setOnClickListener(new TvTicketBeAndAfListener());

    }

    //前一天，后一天的点击监听
    private class TvTicketBeAndAfListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tvTicketResultStep2Before:

                    break;
                case R.id.tvTicketResultStep2After:

                    break;
            }
        }
    }
}
