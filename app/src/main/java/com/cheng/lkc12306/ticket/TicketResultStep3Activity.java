package com.cheng.lkc12306.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.Seat;
import com.cheng.lkc12306.bean.Step3ViewHolder;
import com.cheng.lkc12306.bean.Train;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TicketResultStep3Activity extends AppCompatActivity {
    private TextView tvStep3StationFrom, tvStep3StartTime, tvStep3SeatNameAndNum, tvStep3TrainNO, tvStep3StartDate, tvStep3Stationto, tvStep3ArrivalTime, tvStep3Price, tvStep3AddPassenger, tvStep3PriceSum, tvStep3Submit;
    private ListView lvStep3;
    private List<Map<String, Object>> data = null;
    private Adapter adapter = null;
    final  int REQUESTCODE=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step3);
        initView();
    }

    //初始化控件，并设置监听
    private void initView() {
        lvStep3 = (ListView) findViewById(R.id.lvStep3);
        tvStep3Submit = (TextView) findViewById(R.id.tvStep3Submit);
        tvStep3PriceSum = (TextView) findViewById(R.id.tvStep3PriceSum);
        tvStep3AddPassenger = (TextView) findViewById(R.id.tvStep3AddPassenger);
        tvStep3Price = (TextView) findViewById(R.id.tvStep3Price);
        tvStep3ArrivalTime = (TextView) findViewById(R.id.tvStep3ArrivalTime);
        tvStep3StationFrom = (TextView) findViewById(R.id.tvStep3StationFrom);
        tvStep3StartTime = (TextView) findViewById(R.id.tvStep3StartTime);
        tvStep3SeatNameAndNum = (TextView) findViewById(R.id.tvStep3SeatNameAndNum);
        tvStep3TrainNO = (TextView) findViewById(R.id.tvStep3TrainNO);
        tvStep3StartDate = (TextView) findViewById(R.id.tvStep3StartDate);
        tvStep3Stationto = (TextView) findViewById(R.id.tvStep3Stationto);
        //获取上一界面传过来的对象
        Intent intent = getIntent();
        Train train = (Train) intent.getSerializableExtra("train");
        String seatName = intent.getStringExtra("seatName");
        Seat seat = train.getSeats().get(seatName);
        //将列车信息显示到界面上
        tvStep3StationFrom.setText(train.getStartStationName());
        tvStep3StartTime.setText(train.getStartTime());
        tvStep3SeatNameAndNum.setText(seat.getSeatName() + "(" + seat.getSeatNum() + ")");
        tvStep3TrainNO.setText(train.getTrainNo());
        //历经多少天
        int durationDate = Integer.parseInt(train.getDurationTime().split("小")[0]) / 24;
        tvStep3StartDate.setText(train.getStartTrainDate() + "(" + durationDate + "日" + ")");
        tvStep3Stationto.setText(train.getToStationName());
        tvStep3ArrivalTime.setText(train.getArriveTime());
        tvStep3Price.setText("¥" + seat.getSeatPrice());
        tvStep3PriceSum.setText("订单总额：¥" + seat.getSeatPrice());
        //添加联系人设置监听
        tvStep3AddPassenger.setOnClickListener(new TvStep3AddPassengerListener());
        //提交设置监听
        tvStep3Submit.setOnClickListener(new TvStep3SubmitListener());
//设置适配器，BaseAdapter的方式
        //初始化数据源
        data = new ArrayList<Map<String, Object>>();
        adapter = new Adapter(data);
        lvStep3.setAdapter(adapter);


    }

    //自定义适配器
    private class Adapter extends BaseAdapter {
        List<Map<String, Object>> data;

        public Adapter(List<Map<String, Object>> data) {
            this.data = data;
        }

        public Adapter() {
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //自定义的ViewHolder来优化适配器
            Step3ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new Step3ViewHolder();
                convertView = View.inflate(TicketResultStep3Activity.this, R.layout.item_ticket_result_step3
                        , null);
                viewHolder.imCancel = (ImageView) convertView.findViewById(R.id.imCancel);
                viewHolder.tvStep3ContactIdCard = (TextView) convertView.findViewById(R.id.tvStep3ContactIdCard);
                viewHolder.tvStep3ContactName = (TextView) convertView.findViewById(R.id.tvStep3ContactName);
                viewHolder.tvStep3ContactTel = (TextView) convertView.findViewById(R.id.tvStep3ContactTel);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (Step3ViewHolder) convertView.getTag();
            }
            viewHolder.tvStep3ContactTel.setText("电话:" + data.get(position).get("contactTel"));
            viewHolder.tvStep3ContactName.setText((String) data.get(position).get("contactName"));
            viewHolder.tvStep3ContactIdCard.setText("身份证:" + data.get(position).get("contactIdCard"));
            viewHolder.imCancel.setImageResource(R.mipmap.cancel_25);
            viewHolder.imCancel.setOnClickListener(new ImcancelListener());

            return convertView;
        }

        //取消联系人
        private class ImcancelListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {

            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }
    }

    //提交订单
    private class TvStep3SubmitListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

    //添加联系人
    private class TvStep3AddPassengerListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(TicketResultStep3Activity.this,TicketResultStep3AddPassengerActivity.class);
            startActivityForResult(intent,REQUESTCODE);
        }

    }
//处理添加联系人返回的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUESTCODE&&resultCode==RESULT_OK){
            if(data!=null){

            }
        }
    }
}
