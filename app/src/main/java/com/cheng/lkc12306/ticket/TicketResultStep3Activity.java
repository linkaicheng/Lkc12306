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
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.Seat;
import com.cheng.lkc12306.bean.Step3ViewHolder;
import com.cheng.lkc12306.bean.Train;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TicketResultStep3Activity extends AppCompatActivity {
    private TextView tvStep3StationFrom, tvStep3StartTime, tvStep3SeatNameAndNum, tvStep3TrainNO, tvStep3StartDate, tvStep3Stationto, tvStep3ArrivalTime, tvStep3Price, tvStep3AddPassenger, tvStep3PriceSum, tvStep3Submit;
    private ListView lvStep3;
    private List<Map<String, Object>> passengers = null;
    private Adapter adapter = null;
    final  int REQUESTCODE=1;
    //票价
    private float price;
    //总票价
    private float priceSum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step3);
        initView();
    }

    /**
     *   初始化控件，并设置监听
     */
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
        price=seat.getSeatPrice();
        tvStep3Price.setText("¥" + price);
        tvStep3PriceSum.setText("订单总额：¥0.0");
        //添加联系人设置监听
        tvStep3AddPassenger.setOnClickListener(new TvStep3AddPassengerListener());
        //提交设置监听
        tvStep3Submit.setOnClickListener(new TvStep3SubmitListener());


    }

    /**
     *  自定义适配器
     */
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
            viewHolder.tvStep3ContactTel.setText((String)data.get(position).get("tel"));
            viewHolder.tvStep3ContactName.setText((String) data.get(position).get("name"));
            viewHolder.tvStep3ContactIdCard.setText((String)data.get(position).get("idCard"));
            viewHolder.imCancel.setImageResource(R.mipmap.cancel_25);
            //获取乘车人的类型，用以计算票价
            String passengerType=((String) data.get(position).get("name")).split("\\(")[1].split("\\)")[0];
            viewHolder.imCancel.setOnClickListener(new ImcancelListener(passengerType,position));
            //票价计算，儿童学生半价
            if(passengerType.equals("儿童")||passengerType.equals("学生")){
                priceSum=priceSum+price/2;
            }else{
                priceSum=priceSum+price;
            }
             //显示订单总额
            tvStep3PriceSum.setText("订单总额：¥"+priceSum);
            return convertView;
        }

        //取消联系人
        private class ImcancelListener implements View.OnClickListener {
            private int position;
            private String passengerType;
            public  ImcancelListener(String passengerType,int position){
                this.position=position;
                this.passengerType=passengerType;
            }
            @Override
            public void onClick(View v) {
                passengers.remove(position);
                priceSum=0;
                //如果乘客为空，不会执行到adapter的getView()方法，
                // 因此不会执行里面的计算总额和显示总额的操作
                if(passengers.size()==0){
                    tvStep3PriceSum.setText("订单总额：¥"+priceSum);
                }else{
                    adapter.notifyDataSetChanged();
                }
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

    /**
     * 提交订单,将列车编号，出发日期，和乘车人姓名传给下一个界面
     */
    private class TvStep3SubmitListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //如果没有添加乘车人，给出提示
            if(passengers==null||passengers.size()==0){
                Toast.makeText(TicketResultStep3Activity.this, "请添加乘车人", Toast.LENGTH_SHORT).show();
                return;//返回，不再执行后续代码
            }

            Intent intent=new Intent(TicketResultStep3Activity.this,TicketResultStep4Activity.class);
            //同张订单，列车编号和出发日期一样
            intent.putExtra("trainNo",tvStep3TrainNO.getText().toString());
            String startDate=tvStep3StartDate.getText().toString().split("\\(")[0];
            intent.putExtra("startTrainDate",startDate);
            //存放乘车人的姓名
            List<String> names=new ArrayList<>();
            String name=null;
            for(int i=0;i<passengers.size();i++){
                name= ((String) passengers.get(i).get("name")).split("\\(")[0];
                names.add(name);
            }
            intent.putExtra("names", (Serializable) names);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 添加联系人
     */
    private class TvStep3AddPassengerListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(TicketResultStep3Activity.this,TicketResultStep3AddPassengerActivity.class);
            startActivityForResult(intent,REQUESTCODE);
        }
    }

    /**
     * //处理添加联系人返回的数据
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUESTCODE&&resultCode==RESULT_OK){
            if(data!=null){
                //每次从添加联系人界面回来，订单总额归零，重新计算
                priceSum=0;
                //设置适配器，BaseAdapter的方式
                passengers= (List<Map<String, Object>>) data.getSerializableExtra("passengers");
                adapter = new Adapter(passengers);
                lvStep3.setAdapter(adapter);
            }
        }
    }
}
