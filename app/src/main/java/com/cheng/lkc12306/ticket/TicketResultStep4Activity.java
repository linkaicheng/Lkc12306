package com.cheng.lkc12306.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cheng.lkc12306.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketResultStep4Activity extends AppCompatActivity {
    private TextView tvOrderId
            ,tvPayAfter
            ,tvPayNow;
    private ListView lvStep4;
    private SimpleAdapter adapter;
    List<Map<String ,Object>> data;
    //code 用于生成唯一的订单号
    private static long code;
    //用于生成唯一的座位
    private static int seatNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step4);
        initView();
    }
    private void initView(){
        tvOrderId = (TextView)findViewById(R.id.tvOrderId);
        tvPayAfter = (TextView)findViewById(R.id.tvPayAfter);
        tvPayNow = (TextView)findViewById(R.id.tvPayNow);
        lvStep4 = (ListView)findViewById(R.id.lvStep4);
        Intent intent=getIntent();
        String trainNo=intent.getStringExtra("trainNo");
        String startDate=intent.getStringExtra("startTrainDate");
        List<String> names= (List<String>) intent.getSerializableExtra("names");
        long orderId=nextCode();
        tvOrderId.setText("订单提交成功，您的订单编号为："+orderId);
data=new ArrayList<>();
        for(String name:names){
            Map<String,Object> row =new HashMap<>();
            row.put("name",name);
            row.put("trainNo",trainNo);
            row.put("startDate",startDate);
            int seat=nextSeat();
            row.put("seat","6车"+seat+"号");
            data.add(row);

        }
        //设置适配器
        adapter = new SimpleAdapter(this, data, R.layout.item_ticket_result_step4,
                new String[]{"name", "trainNo", "startDate","seat"}, new int[]{R.id.tvStep4Name
                , R.id.tvStep4TrainNo
                , R.id.tvStep4StartDate
                ,R.id.tvStep4Seat});
        lvStep4.setAdapter(adapter);
    }
//生成订单编号
    public static synchronized long nextCode() {
        code++;
        String str = new SimpleDateFormat("yyyyMM").format(new Date());
        long m = Long.parseLong((str)) * 10000;
        m += code;
        return m;
    }
//生成唯一的座位
    public static synchronized int nextSeat() {
            seatNum++;
        return seatNum;
    }

}
